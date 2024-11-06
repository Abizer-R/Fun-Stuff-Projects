package com.example.backgroundaudio.utils

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import com.example.backgroundaudio.AudioItemModel
import com.example.backgroundaudio.AudioItemType
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.MediaMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


object AudioProcessorUtils {

    fun getMediaItemList(audioItemList: List<AudioItemModel>): List<MediaItem> {
        val mediaList = mutableListOf<MediaItem>()
        for(audioItem in audioItemList) {
            mediaList.add(getMediaItem(audioItem))
        }
        return mediaList
    }

    fun getMediaItem(audioItem: AudioItemModel): MediaItem {
        val mediaItemBuilder = MediaItem.Builder()
            .setUri(audioItem.url)
        if (audioItem.itemType == AudioItemType.UPLOAD_FROM_DEVICE) {
            mediaItemBuilder.setUri(audioItem.storageUri)
        } else {
            mediaItemBuilder.setUri(audioItem.url)
        }
        return mediaItemBuilder.setMediaMetadata(getMediaMetaData(audioItem))
            .build()
    }

    private fun getMediaMetaData(audioItem: AudioItemModel): MediaMetadata {
        return MediaMetadata.Builder()
            .setTitle(audioItem.name)
            .build()
    }

    suspend fun combineAudioAndImage(
        audioPath: String,
        imagePath: String,
        imageWidth: Int,
        imageHeight: Int,
        outputFilePath: String
    ): Boolean {
        return withContext(Dispatchers.IO) {

            // -loop => If set to 1, loop over the input. Default value is 0.
            // -c:a => set codec of audio, i.e. convert input audio to AAC
            // -b:a => set bitrate of audio
            // -c:v libx264 => video codec H.264
            // -shortest => end the video as soon as the audio is done.
            val command = mutableListOf(
                "-r", "1",
                "-loop", "1",
                "-i", imagePath,
                "-i", audioPath,
                "-acodec", "copy",
                "-c:v", "libx264",
                "-r", "1",
                "-shortest"
            )

            /**
             * We need even width and height
             * otherwise, codec H.264 won't work
             */
            val reqWidth = if(imageWidth.and(1) == 0) imageWidth else imageWidth - 1
            val reqHeight = if(imageHeight.and(1) == 0) imageHeight else imageHeight - 1

            command.add("-vf")
            command.add("scale=${reqWidth}:${reqHeight}")

            command.add(outputFilePath)

            val isSuccess = try {
                FFmpegKit.cancel()
                val session = FFmpegKit.executeWithArguments(command.toTypedArray())
                session?.returnCode?.value == ReturnCode.SUCCESS
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }

            return@withContext isSuccess
        }
    }

    suspend fun convertAudioToAAC(
        audioPath: String,
        outputFilePath: String
    ): Boolean {
        return withContext(Dispatchers.IO) {

            // -loop => If set to 1, loop over the input. Default value is 0.
            // -c:a => set codec of audio, i.e. convert input audio to AAC
            // -b:a => set bitrate of audio
            // -c:v libx264 => video codec H.264
            // -shortest => end the video as soon as the audio is done.
            val command = arrayOf(
                "-y",
                "-i", audioPath,
                "-codec:a", "aac",
                outputFilePath
            )

            val isSuccess = try {
                FFmpegKit.cancel()
                val session = FFmpegKit.executeWithArguments(command)
                session?.returnCode?.value == ReturnCode.SUCCESS
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }

            return@withContext isSuccess
        }
    }

    fun getImageDetails(uriFromPath: String): Pair<Int, Int> {
        val bitmap = BitmapFactory.decodeFile(uriFromPath)
        return Pair(
            bitmap.width,
            bitmap.height
        )
    }

    fun getImageHeight(context: Context, uriFromPath: String): Int {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, Uri.parse(uriFromPath))
            val height =
                Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_IMAGE_HEIGHT))
            retriever.release()

            height
        } catch (e: Exception) {
            retriever.release()
            -1
        }
    }

    fun getImageWidth(context: Context, uriFromPath: String): Int {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, Uri.parse(uriFromPath))
            val width =
                Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_IMAGE_WIDTH))
            retriever.release()
            width
        } catch (e: Exception) {
            retriever.release()
            -1
        }
    }

    fun getImageRotation(context: Context, uriFromPath: String): Int {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, Uri.parse(uriFromPath))
            val rotationData =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_IMAGE_ROTATION)
                    ?.toInt()
            retriever.release()
            rotationData ?: -1
        } catch (e: Exception) {
            retriever.release()
            -1
        }

    }

    fun getAudioName(
        uri: Uri,
        contentResolver: ContentResolver
    ): String {
        val cursor: Cursor? = contentResolver.query(
            uri, null, null, null, null, null)

        var displayName = "Unknown"
        cursor?.use {
            if (it.moveToFirst()) {
                displayName = it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                    ?: "Unknown"
            }
        }
        return displayName
    }

    fun getAudioSize(
        uri: Uri,
        contentResolver: ContentResolver
    ): Int {
        val cursor: Cursor? = contentResolver.query(
            uri, null, null, null, null, null)


        var size = 0
        cursor?.use {
            if (it.moveToFirst()) {
                val sizeIndex: Int = it.getColumnIndex(OpenableColumns.SIZE)
                size = if (!it.isNull(sizeIndex)) {
                    it.getInt(sizeIndex)
                } else {
                    0
                }
            }
        }
        return size
    }

    fun getDurationMillis(context: Context, audioUri: Uri): Long {
        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, audioUri)
            val time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val timeInMillisec = time!!.toLong()
            retriever.release()
            return timeInMillisec
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return 0
    }
}