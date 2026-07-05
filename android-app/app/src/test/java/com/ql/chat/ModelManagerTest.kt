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
    fun `modelExists returns false when download in progress`() {
        val modelFile = File(tempDir, ModelManager.MODEL_FILENAME)
        val tempFile = File(tempDir, ModelManager.MODEL_FILENAME + ".downloading")
        modelFile.createNewFile()
        tempFile.createNewFile()
        val manager = ModelManager(tempDir)
        assertFalse(manager.modelExists())
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

    @Test
    fun `getDownloadState returns NOT_STARTED when no files`() {
        val manager = ModelManager(tempDir)
        assertEquals(ModelManager.DownloadState.NOT_STARTED, manager.getDownloadState())
    }

    @Test
    fun `getDownloadState returns COMPLETE when model exists`() {
        val modelFile = File(tempDir, ModelManager.MODEL_FILENAME)
        modelFile.createNewFile()
        val manager = ModelManager(tempDir)
        assertEquals(ModelManager.DownloadState.COMPLETE, manager.getDownloadState())
    }

    @Test
    fun `getDownloadState returns IN_PROGRESS when temp file exists`() {
        val tempFile = File(tempDir, ModelManager.MODEL_FILENAME + ".downloading")
        tempFile.createNewFile()
        val manager = ModelManager(tempDir)
        assertEquals(ModelManager.DownloadState.IN_PROGRESS, manager.getDownloadState())
    }

    @Test
    fun `getModelSizeMB returns 0 when model not present`() {
        val manager = ModelManager(tempDir)
        assertEquals(0, manager.getModelSizeMB())
    }

    @Test
    fun `getModelSizeMB returns correct size`() {
        val modelFile = File(tempDir, ModelManager.MODEL_FILENAME)
        modelFile.writeBytes(ByteArray(1024 * 1024 * 10)) // 10 MB
        val manager = ModelManager(tempDir)
        assertEquals(10, manager.getModelSizeMB())
    }

    @Test
    fun `deleteModel removes model file`() {
        val modelFile = File(tempDir, ModelManager.MODEL_FILENAME)
        modelFile.createNewFile()
        val manager = ModelManager(tempDir)
        assertTrue(manager.deleteModel())
        assertFalse(modelFile.exists())
    }
}
