package com.zoomrx.zipmanager

import com.getcapacitor.*
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

@NativePlugin
class ZipManager : Plugin() {

    private val bufferSize = 8192

    @PluginMethod
    fun unzip(call: PluginCall) {
        val filesPath = call.getArray("files")
        val destination = call.getString("destination")
        val isAsset = call.getBoolean("isAsset")
        val ret = JSObject()
        val statusObj = JSObject()
        ret.put("status", statusObj)
        for (filePath in filesPath.toList<String>()) {
            val zipInputStream = constructZipInputStream(filePath, isAsset)
            val status = if(extract(zipInputStream, destination)) "success" else "failed"
            statusObj.put(filePath, status)
        }
        call.success(ret)
    }

    @PluginMethod
    fun compress(call: PluginCall) {
        val filesPath = call.getArray("files")
        val destination = call.getString("destination")
        val ret = JSObject()
        compressFiles(filesPath, destination)
        ret.put("status", "success")
        call.success(ret)
    }

    private fun compressFiles(filesPath: JSArray, destination: String) {
        val outputStream = FileOutputStream(destination)
        val zipOutputStream = ZipOutputStream(outputStream)
        filesPath.toList<String>().forEach { filePath ->
            var absolutePath = filePath
            if(filePath.endsWith('/')) {
                absolutePath = filePath.substring(0, filePath.length - 1)
            }
            val file = File(absolutePath)
            val basePath = filePath.substring(0, absolutePath.lastIndexOf('/') + 1)
            val relativeFilePath = filePath.substring(absolutePath.lastIndexOf('/') + 1)
            compressRecursively(zipOutputStream, file, basePath, relativeFilePath)
        }
        zipOutputStream.close()
    }

    private fun compressRecursively(zipOutputStream: ZipOutputStream, file: File, basePath: String, relativePath: String) {
        if (file.isDirectory) {
            zipOutputStream.putNextEntry(ZipEntry("$relativePath/"))
            file.listFiles()?.iterator()?.forEach { childFile ->
                compressRecursively(zipOutputStream, childFile, basePath, relativePath + "/${childFile.name}")
            }
        } else {
            zipOutputStream.putNextEntry(ZipEntry(relativePath))
            val fileInputStream = FileInputStream(File(basePath + relativePath))
            val byteArray = ByteArray(bufferSize)
            var length: Int
            while (fileInputStream.read(byteArray).also { length = it } != -1) {
                zipOutputStream.write(byteArray, 0, length)
            }
            fileInputStream.close()
        }
        zipOutputStream.closeEntry()
    }

    private fun constructZipInputStream(filepath: String, isAsset: Boolean): ZipInputStream {
        return if (isAsset) {
            ZipInputStream(context.assets.open(filepath))
        } else {
            ZipInputStream(File(filepath).inputStream())
        }
    }

    private fun extract(zipInputStream: ZipInputStream, location: String): Boolean {
        try {
            var zipEntry: ZipEntry
            while (zipInputStream.nextEntry.also { zipEntry = it } != null) {
                val innerFileName = location + zipEntry.name
                val innerFile = File(innerFileName)
                if (zipEntry.isDirectory) {
                    innerFile.mkdirs()
                } else {
                    if (innerFileName.lastIndexOf('/') != -1) {
                        val srcFileDir = File(innerFileName.substring(0, innerFileName.lastIndexOf('/')))
                        srcFileDir.mkdirs()
                    }
                    val outputStream = FileOutputStream(innerFile)
                    val bufferedOutputStream = BufferedOutputStream(
                            outputStream, bufferSize)
                    val buffer = ByteArray(bufferSize)
                    var count: Int
                    while (zipInputStream.read(buffer).also { count = it } != -1) {
                        bufferedOutputStream.write(buffer, 0, count)
                    }
                    bufferedOutputStream.flush()
                    bufferedOutputStream.close()
                    outputStream.close()
                }
                zipInputStream.closeEntry()
            }
            zipInputStream.close()
            return true
        } catch (e: IOException) {
        }
        return false
    }
}