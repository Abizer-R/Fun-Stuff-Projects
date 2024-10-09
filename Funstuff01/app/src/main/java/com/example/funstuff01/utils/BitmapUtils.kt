package com.example.funstuff01.utils

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

object BitmapUtils {

    fun resizeAndCompressBitmapWebP(imageFile: File, maxWidth: Int = 1080, minWidth: Int = 320): Bitmap? {
        // Decode the image file to a Bitmap
        val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath) ?: return null

        // Calculate the new dimensions while maintaining the aspect ratio
        val aspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
        val newWidth = when {
            bitmap.width > maxWidth -> maxWidth
            bitmap.width < minWidth -> minWidth
            else -> bitmap.width
        }
        val newHeight = (newWidth / aspectRatio).toInt()

        // Resize the bitmap
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, false)
    }

    fun saveBitmapToFileWebP(bitmap: Bitmap, file: File, quality: Int = 85) {
        FileOutputStream(file).use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.WEBP, quality, outputStream)
        }
    }

    fun saveBitmapToFile(
        bitmap: Bitmap,
        file: File,
        quality: Int = 85,
        compressFormat: CompressFormat
    ) {
        FileOutputStream(file).use { outputStream ->
            bitmap.compress(compressFormat, quality, outputStream)
        }
    }


//    @SuppressLint("CheckResult")
//    fun compressImage(
//        contentResolver: ContentResolver,
//        uriFromPath: Uri
//    ): Triple<ByteArray, Int, Int> {
//
//        val b = BitmapFactory.decodeStream(contentResolver.openInputStream(uriFromPath))
//        val stream = ByteArrayOutputStream()
//        var newImage: Bitmap?
//        val orgWidth = b.width
//        val orgHeight = b.height
//        val Ratio = orgWidth / orgHeight
//
//        Log.d(TAG, "Old Ratio = $Ratio Width = $orgWidth Height = $orgHeight")
//
//        newImage = when {
//            orgWidth > DEFFUL_GREATIST_IMAGE_WIDTH -> {
//                widthIsBig(b)
//            }
//
//            orgWidth < DEFFUL_SMALLEST_IMAGE_WIDTH -> {
//                widthIsSmall(b)
//            }
//
//            else -> {
//                b
//            }
//        }
//
//        val imageRotation: Int = getImageRotation(uriFromPath, contentResolver)
//
//        if (imageRotation != 0) {
//            newImage = newImage?.let { getBitmapRotatedByDegree(it, imageRotation) }
//        }
//
//        newImage?.compress(Bitmap.CompressFormat.JPEG, 100, stream)
//
//        return Triple(stream.toByteArray(), newImage?.width ?: 0, newImage?.height ?: 0)
//    }
}