package com.ql.chat

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ChatMessage(
    val role: String,
    val content: String
)

class ChatViewModel : ViewModel() {

    private val _messages = mutableStateListOf<ChatMessage>()
    val messages: List<ChatMessage> = _messages

    val isLoading = mutableStateOf(false)

    private var engine: LlamaEngine? = null
    private var modelManager: ModelManager? = null

    fun initEngine(modelPath: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val eng = LlamaEngine()
            eng.init(modelPath)
            engine = eng
        }
    }

    fun sendMessage(content: String) {
        if (content.isBlank() || isLoading.value) return

        _messages.add(ChatMessage("user", content))
        isLoading.value = true

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val eng = engine
                if (eng == null) {
                    withContext(Dispatchers.Main) {
                        _messages.add(ChatMessage("assistant", "Engine not initialized"))
                        isLoading.value = false
                    }
                    return@launch
                }

                val response = eng.generate(content)
                withContext(Dispatchers.Main) {
                    _messages.add(ChatMessage("assistant", response))
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _messages.add(ChatMessage("assistant", "Error: ${e.message}"))
                }
            } finally {
                withContext(Dispatchers.Main) {
                    isLoading.value = false
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        engine?.free()
    }
}
