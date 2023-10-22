package com.example.funstuff01.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.view.View
import com.example.funstuff01.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object TextToImageUtils {

    suspend fun saveImage(view: View, path: String): Boolean {
        return withContext(Dispatchers.IO) {
            val bitmap = getBitmapFromView(view)
                ?: return@withContext false

            return@withContext writeToFile(path, bitmap)
        }
    }

    fun getBitmapFromView(view: View) : Bitmap? {
        var bitmap: Bitmap? = null
        try {
            //Define a bitmap with the same size as the view
            bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            //Bind a canvas to it
            val canvas = Canvas(bitmap)
            //Get the view's background
            val bgDrawable = view.background
            if (bgDrawable != null) //has background drawable, then draw it on the canvas
                bgDrawable.draw(canvas) else  //does not have background drawable, then draw white background on the canvas
                canvas.drawColor(Color.WHITE)
            // draw the view on the canvas
            view.draw(canvas)
        } catch (e: Exception) {
            bitmap = null
            e.printStackTrace()
        }
        return bitmap
    }

    suspend fun writeToFile(path: String, bitmap: Bitmap, format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG, quality: Int = 85) : Boolean {
        var isSuccessful = true
        withContext(Dispatchers.IO) {
            try {
                FileOutputStream(File(path)).use { outputStream ->

                    bitmap.compress(format, quality, outputStream)
                    outputStream.flush()
                }
            } catch (e: Exception) {
                isSuccessful = false
                e.printStackTrace()
            }
        }

        return isSuccessful
    }

    fun getColorResourceList() : List<Int> {
        return listOf(
            R.color.black,
            R.color.white,
            R.color.colors_list_item_1,
            R.color.colors_list_item_2,
            R.color.colors_list_item_3,
            R.color.colors_list_item_4,
            R.color.colors_list_item_5,
            R.color.colors_list_item_6,
            R.color.colors_list_item_7,
            R.color.colors_list_item_8,
            R.color.colors_list_item_9,
            R.color.colors_list_item_10,
            R.color.colors_list_item_11,
            R.color.colors_list_item_12,
            R.color.colors_list_item_13
        )
    }

    fun getMainBgList() : List<Int> {
        return listOf(
            R.drawable.bg_gradient_1,
            R.drawable.bg_gradient_2,
            R.drawable.bg_gradient_3,
            R.drawable.bg_gradient_4,
            R.drawable.bg_gradient_5,
            R.drawable.bg_gradient_6,
            R.drawable.bg_gradient_7,
        )
    }

    fun getBtnBgList() : List<Int> {
        return listOf(
            R.drawable.bg_gradient_circle_1,
            R.drawable.bg_gradient_circle_2,
            R.drawable.bg_gradient_circle_3,
            R.drawable.bg_gradient_circle_4,
            R.drawable.bg_gradient_circle_5,
            R.drawable.bg_gradient_circle_6,
            R.drawable.bg_gradient_circle_7,
        )
    }
}
