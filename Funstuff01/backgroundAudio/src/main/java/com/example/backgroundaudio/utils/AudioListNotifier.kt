package com.example.backgroundaudio.utils

sealed class AudioListNotifier {
    object NotifyDataChanged: AudioListNotifier()
    data class NotifyItemRangeAdded(val positionStart: Int, val itemsAdded: Int): AudioListNotifier()
    data class NotifyItemRangeRemoved(val positionStart: Int, val itemsRemoved: Int): AudioListNotifier()
    data class NotifyItemAdded(val position: Int): AudioListNotifier()
    data class NotifyItemRemoved(val position: Int): AudioListNotifier()
    data class NotifyItemChanged(val position: Int, val payload: Any? = null): AudioListNotifier()
    data class NotifyChildDataChanged(val position: Int): AudioListNotifier()
    object Nothing: AudioListNotifier()
}