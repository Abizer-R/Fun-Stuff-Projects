package com.example.backgroundaudio.audioList

import androidx.lifecycle.ViewModel
import com.example.backgroundaudio.audioList.adapter.AudioItemModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SharedBackgroundAudioViewModel
@Inject constructor(
) : ViewModel() {

    private var _selectedAudioItem: AudioItemModel? = null
    val selectedAudioItem get() = _selectedAudioItem


    fun updateSelectedItem(audioItem: AudioItemModel?) {
        _selectedAudioItem = audioItem
    }
}
