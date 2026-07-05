package com.ql.chat

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ChatViewModelTest {
    private lateinit var viewModel: ChatViewModel

    @Before
    fun setup() {
        viewModel = ChatViewModel()
    }

    @Test
    fun `initial state has empty messages`() {
        assertTrue(viewModel.messages.isEmpty())
    }

    @Test
    fun `initial state is not loading`() {
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `sendMessage adds user message`() {
        viewModel.sendMessage("Hello")
        assertEquals(1, viewModel.messages.size)
        assertEquals("Hello", viewModel.messages[0].content)
        assertEquals("user", viewModel.messages[0].role)
    }

    @Test
    fun `messages are immutable list`() {
        viewModel.sendMessage("Hello")
        val size1 = viewModel.messages.size
        viewModel.sendMessage("World")
        val size2 = viewModel.messages.size
        assertTrue(size2 > size1)
    }
}
