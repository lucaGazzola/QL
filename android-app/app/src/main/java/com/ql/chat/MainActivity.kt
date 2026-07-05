package com.ql.chat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ql.chat.ui.ChatScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.DecimalFormat

class MainActivity : ComponentActivity() {

    private val viewModel: ChatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val modelsDir = File(filesDir, "models")
        val modelManager = ModelManager(modelsDir)

        setContent {
            val scope = rememberCoroutineScope()
            var isDownloading by remember { mutableStateOf(false) }
            var downloadProgress by remember { mutableIntStateOf(0) }
            var bytesDownloaded by remember { mutableLongStateOf(0L) }
            var totalBytes by remember { mutableLongStateOf(0L) }
            var downloadError by remember { mutableStateOf<String?>(null) }
            var isLoadingModel by remember { mutableStateOf(false) }

            fun startDownload() {
                downloadError = null
                scope.launch {
                    isDownloading = true
                    try {
                        withContext(Dispatchers.IO) {
                            modelManager.downloadModel { progress, downloaded, total ->
                                downloadProgress = progress
                                bytesDownloaded = downloaded
                                totalBytes = total
                            }
                        }
                        // Model downloaded, now load it
                        isLoadingModel = true
                        withContext(Dispatchers.IO) {
                            viewModel.initEngine(modelManager.getModelPath())
                        }
                    } catch (e: Exception) {
                        downloadError = e.message
                    } finally {
                        isDownloading = false
                        isLoadingModel = false
                    }
                }
            }

            LaunchedEffect(Unit) {
                when (modelManager.getDownloadState()) {
                    ModelManager.DownloadState.COMPLETE -> {
                        isLoadingModel = true
                        try {
                            withContext(Dispatchers.IO) {
                                viewModel.initEngine(modelManager.getModelPath())
                            }
                        } catch (e: Exception) {
                            downloadError = "Failed to load model: ${e.message}"
                        } finally {
                            isLoadingModel = false
                        }
                    }
                    ModelManager.DownloadState.IN_PROGRESS -> {
                        startDownload()
                    }
                    ModelManager.DownloadState.NOT_STARTED -> {
                        startDownload()
                    }
                }
            }

            MaterialTheme {
                Surface {
                    when {
                        isDownloading -> {
                            DownloadScreen(
                                progress = downloadProgress,
                                bytesDownloaded = bytesDownloaded,
                                totalBytes = totalBytes
                            )
                        }
                        isLoadingModel -> {
                            LoadingModelScreen()
                        }
                        downloadError != null -> {
                            ErrorScreen(
                                message = downloadError ?: "Unknown error",
                                onRetry = { startDownload() }
                            )
                        }
                        else -> {
                            ChatScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}

fun formatBytes(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val kb = bytes / 1024.0
    if (kb < 1024) return "${DecimalFormat("#.##").format(kb)} KB"
    val mb = kb / 1024.0
    if (mb < 1024) return "${DecimalFormat("#.##").format(mb)} MB"
    val gb = mb / 1024.0
    return "${DecimalFormat("#.##").format(gb)} GB"
}

@Composable
fun DownloadScreen(progress: Int, bytesDownloaded: Long, totalBytes: Long) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Downloading Model",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "This may take several minutes on first launch",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        LinearProgressIndicator(
            progress = { progress / 100f },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "$progress%",
            style = MaterialTheme.typography.titleLarge
        )
        if (totalBytes > 0) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${formatBytes(bytesDownloaded)} / ${formatBytes(totalBytes)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Please keep the app open during download",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun LoadingModelScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Loading model...",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "This may take a moment",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ErrorScreen(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Download Failed",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRetry) {
            Text("Retry Download")
        }
    }
}
