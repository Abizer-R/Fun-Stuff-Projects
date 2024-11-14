package com.anilokcun.uwmediapicker.helper

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.net.toUri
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Author     	:	Anıl Furkan Ökçün
 * Author mail	:	anilfurkanokcun@gmail.com
 * Create Date	:	30.08.2018
 */

/** Extension for simplify create basic Toast Message with String resource */
internal fun Context.toastStringRes(resId: Int, duration: Int = Toast.LENGTH_SHORT): Toast {
	return Toast.makeText(this, this.getString(resId), duration).apply { show() }
}

/** Converts strings to uri */
internal fun String.toUri(): Uri = Uri.parse("file://$this")

/** Logs Error */
internal fun String?.logError() {
	Log.e("UwMediaPicker", this.toString())
}


fun Context.saveMediaToFile(
	uri: Uri,
	presetSubDirectory: String? = null,
	presetFileName: String? = null
): String {

	val subDirectory: String
	val fileName: String

	var mimeType: String? = ""
	var fileExtensions: String? = ""
	if (uri.scheme.equals(ContentResolver.SCHEME_CONTENT)) {
		mimeType = contentResolver.getType(uri)
		fileExtensions = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
	} else {
		fileExtensions = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
		mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtensions)
	}

	when(mimeType) {
		"image" -> {
			subDirectory = presetSubDirectory ?: "images"
			fileName = "image_" + (presetFileName ?: System.currentTimeMillis()) + "." + fileExtensions
		}
		"video" -> {
			subDirectory = presetSubDirectory ?: "videos"
			fileName = "video_" + (presetFileName ?: System.currentTimeMillis()) + "." + fileExtensions
		}
		"audio" -> {
			subDirectory = presetSubDirectory ?: "documents"
			fileName = "document_" + (presetFileName ?: System.currentTimeMillis()) + "." + fileExtensions
		}
		else -> {
			subDirectory = presetSubDirectory ?: "files"
			fileName = "file_" + (presetFileName ?: System.currentTimeMillis()) + "." + fileExtensions
		}
	}

	val directory = File(filesDir.path + File.separator + subDirectory)

	val fileToSave = File(directory, fileName)

	if (!directory.exists()) {
		directory.mkdirs()
	}

	if (!fileToSave.exists()) {
		fileToSave.createNewFile()
	} else {
		fileToSave.delete()
		fileToSave.createNewFile()
	}

	var bis: BufferedInputStream? = null
	var bos: BufferedOutputStream? = null
	try {
		bis = BufferedInputStream(
			contentResolver
				.openInputStream(uri)
		)
		bos = BufferedOutputStream(
			FileOutputStream(
				fileToSave.path,
				false
			)
		)
		val buffer = ByteArray(1024)
		bis.read(buffer)
		do {
			bos.write(buffer)
		} while (bis.read(buffer) != -1)
	} catch (ioe: IOException) {
		ioe.printStackTrace()
	} finally {
		try {
			bis?.close()
			bos?.close()
		} catch (e: IOException) {
			e.printStackTrace()
		}
	}

	return fileToSave.path
}

fun Context.saveMediaToFile(path: String): String {
	val uri = File(path).toUri()

	val subDirectory: String
	val fileName: String

	val fileExtensions = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
	val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtensions)

	when {
		mimeType.toString().contains("image") -> {
			subDirectory = "images"
			fileName = "image_" + System.currentTimeMillis() + "." + fileExtensions
		}
		mimeType.toString().contains("video") -> {
			subDirectory = "videos"
			fileName = "video_" + System.currentTimeMillis() + "." + fileExtensions
		}
		mimeType.toString().contains("audio") -> {
			subDirectory = "documents"
			fileName = "document_" + System.currentTimeMillis() + "." + fileExtensions
		}
		else -> {
			subDirectory = "files"
			fileName = "file_" + System.currentTimeMillis() + "." + fileExtensions
		}
	}

	val directory = File(filesDir.path + File.separator + subDirectory)

	val fileToSave = File(directory, fileName)

	if (!directory.exists()) {
		directory.mkdirs()
	}

	if (!fileToSave.exists()) {
		fileToSave.createNewFile()
	}

	var bis: BufferedInputStream? = null
	var bos: BufferedOutputStream? = null
	try {
		bis = BufferedInputStream(
			contentResolver
				.openInputStream(uri)
		)
		bos = BufferedOutputStream(
			FileOutputStream(
				fileToSave.path,
				false
			)
		)
		val buffer = ByteArray(1024)
		bis.read(buffer)
		do {
			bos.write(buffer)
		} while (bis.read(buffer) != -1)
	} catch (ioe: IOException) {
		ioe.printStackTrace()
	} finally {
		try {
			bis?.close()
			bos?.close()
		} catch (e: IOException) {
			e.printStackTrace()
		}
	}

	return fileToSave.path
}