package com.example.funstuff01.utils

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Environment
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL

object AppUtils {

    val androidOreoAndAbove: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

    val androidPieAndAbove: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P

    val androidQAndAbove: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

    val androidRAndAbove: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R

    val androidSAndAbove: Boolean
        get() = Build.VERSION.SDK_INT >= 31


    fun isNetworkAvailable(context: Context?): Boolean {
        if (context == null) return false
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                try {
                    val capabilities =
                        connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
                    if (capabilities != null) {
                        when {
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                                return true
                            }

                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                                return true
                            }

                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                                return true
                            }
                        }
                    }
                } catch (e: Exception) {
                    //not handling
                }
            }

            else -> {
                val activeNetworkInfo = connectivityManager.activeNetworkInfo
                if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
                    return true
                }
            }
        }
        return false
    }

    fun getPathForStorage(context: Context, fileName: String): String {
        return File(
            ContextCompat.getExternalFilesDirs(
                context,
                Environment.DIRECTORY_PICTURES
            )[0], fileName
        ).path
    }

    suspend fun downloadFile(
        url: String,
        path: String,
        onComplete: () -> Unit,
        onFailed: () -> Unit
    ) {
        withContext(Dispatchers.IO) {
            var isSuccessful = true
            try {
                URL(url).openStream().use { input ->
                    File(path).outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                isSuccessful = false
            }
            if (isSuccessful) onComplete()
            else onFailed()
        }
    }
}