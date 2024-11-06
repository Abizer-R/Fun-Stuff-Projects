package com.example.backgroundaudio

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AudioItemModel(
    val author: String,
    val index: String,
    var name: String,
    val url: String,
    val icon: String,
    var storageUri: Uri? = null,
    val itemType: AudioItemType = AudioItemType.AUDIO_FILE,
    var isPlayingCurrently: Boolean = false,
    var isLoading: Boolean = false,
    var isSelected: Boolean = false
) : Parcelable

enum class AudioItemType {
    AUDIO_FILE, UPLOAD_FROM_DEVICE
}