package com.example.backgroundaudio.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

object AudioPermissionUtils {

    enum class GalleryAccessLevel {
        FULL_ACCESS, PARTIAL_ACCESS, NO_ACCESS
    }

    fun getGalleryAccessLevel(context: Context): GalleryAccessLevel {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            (checkPermission(context, Manifest.permission.READ_MEDIA_IMAGES)
                    && checkPermission(context, Manifest.permission.READ_MEDIA_VIDEO))
        ) {
            // Full access on Android 13 (API level 33) or higher
            return GalleryAccessLevel.FULL_ACCESS

        } else if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
            checkPermission(context, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
        ) {
            // Partial access on Android 14 (API level 34) or higher
            return GalleryAccessLevel.PARTIAL_ACCESS

        }  else if (checkPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            // Full access up to Android 12 (API level 32)
            return GalleryAccessLevel.FULL_ACCESS

        } else {
            // Access denied
            return GalleryAccessLevel.NO_ACCESS
        }
    }

    fun getGalleryPermissionsArray(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO
            )
        } else if (isAndroidQAndAbove()) {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        } else {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
    }

    private fun checkPermission(context: Context, permString: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context, permString
        ) == PackageManager.PERMISSION_GRANTED
    }


    fun isAndroidTiramisuAndAbove(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    }

    fun isAndroidQAndAbove(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    }

    fun isAndroidRAndAbove(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
    }

    fun isAndroidOAndAbove(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
    }
}