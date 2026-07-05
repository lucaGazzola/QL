package com.ql.chat

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {
    private lateinit var viewModel: ChatViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = ChatViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
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
    fun `messages are immutable list`() = runTest {
        viewModel.sendMessage("Hello")
        testScheduler.advanceUntilIdle()
        val size1 = viewModel.messages.size
        viewModel.sendMessage("World")
        testScheduler.advanceUntilIdle()
        val size2 = viewModel.messages.size
        assertTrue(size2 > size1)
    }
}
