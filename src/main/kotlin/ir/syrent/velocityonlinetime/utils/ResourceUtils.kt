package ir.syrent.velocityonlinetime.utils

import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream


object ResourceUtils {

    private fun getResource(resourceFileName: String): InputStream? {
        val classLoader = Thread.currentThread().contextClassLoader
        return classLoader.getResourceAsStream(resourceFileName)
    }

    fun copyResource(resourceFileName: String, file: File): File {
        val inputStream = getResource(resourceFileName)
        try {
            val outputStream = FileOutputStream(file)
            IOUtils.copy(inputStream, outputStream)
            inputStream?.close()
            outputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return file
    }
}