package com.ql.chat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ql.chat.ui.ChatScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MainActivity : ComponentActivity() {

    private val viewModel: ChatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val modelsDir = File(filesDir, "models")
        val modelManager = ModelManager(modelsDir)

        setContent {
            val context = LocalContext.current
            val scope = rememberCoroutineScope()
            var isDownloading by remember { mutableStateOf(false) }
            var downloadProgress by remember { mutableIntStateOf(0) }
            var downloadError by remember { mutableStateOf<String?>(null) }

            LaunchedEffect(Unit) {
                if (!modelManager.modelExists()) {
                    isDownloading = true
                    try {
                        withContext(Dispatchers.IO) {
                            modelManager.downloadModel { progress ->
                                downloadProgress = progress
                            }
                        }
                        viewModel.initEngine(modelManager.getModelPath())
                    } catch (e: Exception) {
                        downloadError = e.message
                    } finally {
                        isDownloading = false
                    }
                } else {
                    viewModel.initEngine(modelManager.getModelPath())
                }
            }

            MaterialTheme {
                Surface {
                    when {
                        isDownloading -> {
                            DownloadScreen(progress = downloadProgress)
                        }
                        downloadError != null -> {
                            ErrorScreen(
                                message = downloadError ?: "Unknown error",
                                onRetry = {
                                    downloadError = null
                                    scope.launch {
                                        isDownloading = true
                                        try {
                                            withContext(Dispatchers.IO) {
                                                modelManager.downloadModel { progress ->
                                                    downloadProgress = progress
                                                }
                                            }
                                            viewModel.initEngine(modelManager.getModelPath())
                                        } catch (e: Exception) {
                                            downloadError = e.message
                                        } finally {
                                            isDownloading = false
                                        }
                                    }
                                }
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

@Composable
fun DownloadScreen(progress: Int) {
    androidx.compose.foundation.layout.Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
    ) {
        androidx.compose.material3.Text(
            text = "Downloading model...",
            style = androidx.compose.material3.MaterialTheme.typography.headlineSmall
        )
        androidx.compose.foundation.layout.Spacer(
            modifier = Modifier.height(16.dp)
        )
        androidx.compose.material3.LinearProgressIndicator(
            progress = { progress / 100f },
            modifier = Modifier.fillMaxWidth()
        )
        androidx.compose.foundation.layout.Spacer(
            modifier = Modifier.height(8.dp)
        )
        androidx.compose.material3.Text(text = "$progress%")
    }
}

@Composable
fun ErrorScreen(message: String, onRetry: () -> Unit) {
    androidx.compose.foundation.layout.Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
    ) {
        androidx.compose.material3.Text(
            text = "Download failed",
            style = androidx.compose.material3.MaterialTheme.typography.headlineSmall
        )
        androidx.compose.foundation.layout.Spacer(
            modifier = Modifier.height(8.dp)
        )
        androidx.compose.material3.Text(text = message)
        androidx.compose.foundation.layout.Spacer(
            modifier = Modifier.height(16.dp)
        )
        androidx.compose.material3.Button(onClick = onRetry) {
            androidx.compose.material3.Text("Retry")
        }
    }
}
