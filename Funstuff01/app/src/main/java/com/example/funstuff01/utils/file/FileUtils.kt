package com.example.funstuff01.utils.file

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import java.io.File
import java.io.FileInputStream

object FileUtils {

    fun saveFileToAppFolder(
        context: Context,
        file: File,
        onSuccess: () -> Unit,
        onFailure: (errorMsg: String?) -> Unit
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveFileToAppFolderAndroid(context, file, onSuccess, onFailure)
        } else {
            saveFileToAppFolderAndroidLegacy(context, file, onSuccess, onFailure)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun saveFileToAppFolderAndroid(
        context: Context,
        file: File,
        onSuccess: () -> Unit,
        onFailure: (errorMsg: String?) -> Unit
    ) {

        val filename = System.currentTimeMillis().toString() + "." + file.extension
        val mimeType = getMimeTypeFromFile(file)
        val relativePath = getRelativePathFromMimeTypeNew(context, mimeType)


        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
        }

        val contentUri: Uri = when {
            mimeType.startsWith("image/") -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            mimeType.startsWith("video/") -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            else -> MediaStore.Files.getContentUri("external")
        }

        val uri = context.contentResolver.insert(contentUri, contentValues)
        if (uri != null) {
            copyFileToUri(context.contentResolver, file, uri)
            onSuccess()
        } else {
            onFailure(null)
        }
    }

    fun saveFileToAppFolderAndroidLegacy(
        context: Context,
        file: File,
        onSuccess: () -> Unit,
        onFailure: (errorMsg: String?) -> Unit
    ) {
        val filename = System.currentTimeMillis().toString() + "." + file.extension
        val mimeType = getMimeTypeFromFile(file)
        val relativePath = getRelativePathFromMimeTypeNew(context, mimeType)
        val externalStorageDir = File(getExternalStorageRootLegacy(), "$relativePath/$filename")

        externalStorageDir.parentFile?.mkdirs()

        try {
            copyFile(file, externalStorageDir)
        } catch (e: Exception) {
            onFailure(e.message)
        }

        // Make the file visible in the gallery
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DATA, externalStorageDir.absolutePath)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
        }
        val uri = context.contentResolver.insert(MediaStore.Files.getContentUri("external"), values)


        if (uri != null) {
            copyFileToUri(context.contentResolver, file, uri)
            onSuccess()
        } else {
            onFailure(null)
        }
    }

    private fun getExternalStorageRootLegacy(): File {
        return Environment.getExternalStorageDirectory()
    }

    private fun getMimeTypeFromFile(file: File): String {
        return when (file.extension.lowercase()) {
            "jpg", "jpeg", "png" -> "image/${file.extension.lowercase()}"
            "mp4" -> "video/mp4"
            else -> "application/octet-stream"
        }
    }

    private fun getRelativePathFromMimeTypeNew(context: Context, mimeType: String): String {
        return when {
            mimeType.startsWith("image/") -> Environment.DIRECTORY_PICTURES + "/FunStuff Images"
            mimeType.startsWith("video/") -> Environment.DIRECTORY_MOVIES + "/FunStuff Videos"
            else -> Environment.DIRECTORY_DOCUMENTS + "/FunStuff Documents"
        }
    }

    private fun getRelativePathFromMimeType(context: Context, mimeType: String): String {
        return when {
            mimeType.startsWith("image/") -> getFunStuffImagesFolderPath(context)
            mimeType.startsWith("video/") -> getFunStuffVideosFolderPath(context)
            else -> getFunStuffDocumentsFolderPath(context)
        }
    }

    fun copyFileToUri(resolver: ContentResolver, file: File, uri: Uri) {
        resolver.openOutputStream(uri)?.use { outputStream ->
            FileInputStream(file).use { inputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    }

    fun copyFile(sourceFile: File, destFile: File) {
        FileInputStream(sourceFile).use { inputStream ->
            destFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    }

    fun getFunStuffImagesFolderPath(context: Context): String {
        return getAppMediaParentFolder(context).plus("/FunStuff Images")
    }

    fun getFunStuffVideosFolderPath(context: Context): String {
        return getAppMediaParentFolder(context).plus("/FunStuff Videos")
    }

    fun getFunStuffAudioFolderPath(context: Context): String {
        return getAppMediaParentFolder(context).plus("/FunStuff Audios")
    }

    fun getFunStuffDocumentsFolderPath(context: Context): String {
        return getAppMediaParentFolder(context).plus("/FunStuff Documents")
    }

    fun getAppMediaParentFolder(context: Context): String {
        return getAndroidMediaDirectoryPath(context).plus("/Media")
    }

    fun getAndroidMediaDirectoryPath(context: Context): String {
        return "Android/media/${context.packageName}/FunStuff"
    }
}