package com.ql.chat

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File

class ModelManagerTest {
    private lateinit var tempDir: File

    @Before
    fun setup() {
        tempDir = File(System.getProperty("java.io.tmpdir"), "ql-test-${System.currentTimeMillis()}")
        tempDir.mkdirs()
    }

    @Test
    fun `modelExists returns false when model file missing`() {
        val manager = ModelManager(tempDir)
        assertFalse(manager.modelExists())
    }

    @Test
    fun `modelExists returns true when model file present`() {
        val modelFile = File(tempDir, ModelManager.MODEL_FILENAME)
        modelFile.createNewFile()
        val manager = ModelManager(tempDir)
        assertTrue(manager.modelExists())
    }

    @Test
    fun `getModelPath returns correct path`() {
        val manager = ModelManager(tempDir)
        val expected = File(tempDir, ModelManager.MODEL_FILENAME).absolutePath
        assertEquals(expected, manager.getModelPath())
    }

    @Test
    fun `getModelUrl returns HuggingFace URL`() {
        val manager = ModelManager(tempDir)
        assertTrue(manager.getModelUrl().contains("huggingface.co"))
        assertTrue(manager.getModelUrl().contains(ModelManager.MODEL_FILENAME))
    }
}
