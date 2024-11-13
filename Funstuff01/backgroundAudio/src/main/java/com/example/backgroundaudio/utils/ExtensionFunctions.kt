package com.example.backgroundaudio.utils

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.PointF
import android.net.Uri
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Parcelable
import android.util.DisplayMetrics
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.abs

inline fun <reified T : Parcelable> Bundle.parcelableArrayList(key: String): ArrayList<T>? = when {
    SDK_INT >= 33 -> getParcelableArrayList(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelableArrayList(key)
}

inline fun <reified T : Parcelable> Intent.parcelableArrayList(key: String): ArrayList<T>? = when {
    SDK_INT >= 33 -> getParcelableArrayListExtra(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelableArrayListExtra(key)
}

fun Fragment.toast(message: String) = activity?.runOnUiThread{
    context?.toast(message)
}

fun Context.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun RecyclerView.smoothSnapToStart(
    position: Int = 0,
    snapMode: Int = LinearSmoothScroller.SNAP_TO_START,
    fromShorts: Boolean = false
) {
    val layoutManager = layoutManager as LinearLayoutManager
    val firstVisible = layoutManager.findFirstVisibleItemPosition()
    val diff = abs(firstVisible - position)

    val smoothScroller = object : LinearSmoothScroller(this.context) {
        override fun getVerticalSnapPreference(): Int = snapMode
        override fun getHorizontalSnapPreference(): Int = snapMode
        override fun computeScrollVectorForPosition(targetPosition: Int): PointF? {
            return super.computeScrollVectorForPosition(targetPosition)
        }

        override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics?): Float {
            // smaller = faster
            val MILLISECONDS_PER_INCH = when(diff) {
                in 0..10 -> if(fromShorts) 25f else 100f
                in 11..50 -> if(fromShorts) 5f else 25f
                else -> 5f
            }
            return MILLISECONDS_PER_INCH / displayMetrics!!.densityDpi
        }
    }
    smoothScroller.targetPosition = position
    layoutManager.startSmoothScroll(smoothScroller)
}

fun Context.saveMediaToFile(uri: Uri, presetFileName: String? = null): String {
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
            subDirectory = "images"
            fileName = "image_" + (presetFileName ?: System.currentTimeMillis()) + "." + fileExtensions
        }
        "video" -> {
            subDirectory = "videos"
            fileName = "video_" + (presetFileName ?: System.currentTimeMillis()) + "." + fileExtensions
        }
        "audio" -> {
            subDirectory = "documents"
            fileName = "document_" + (presetFileName ?: System.currentTimeMillis()) + "." + fileExtensions
        }
        else -> {
            subDirectory = "files"
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