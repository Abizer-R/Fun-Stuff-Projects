package com.example.backgroundaudio

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.backgroundaudio.utils.AudioListNotifier
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ObsoleteCoroutinesApi::class)
@HiltViewModel
class SharedBackgroundAudioViewModel
@Inject constructor(
) : ViewModel() {

    private val _audioList = arrayListOf<AudioItemModel>()
    val audioList: List<AudioItemModel> get() = _audioList

    val _audioListNotifier = BroadcastChannel<AudioListNotifier>(Channel.BUFFERED)
    var currentPlaying: Int = -1
    var shouldPlayOnResume: Boolean = false

    var selectedAudioItemIndex: Int = -1
    private var _selectedAudioItem: AudioItemModel? = null
    val selectedAudioItem get() = _selectedAudioItem

    private var _allowAudioFromDeviceStorage = true
    val allowAudioFromDeviceStorage get() = _allowAudioFromDeviceStorage


    fun updateAllowAudioFromStorage(shouldAllow: Boolean) {
        _allowAudioFromDeviceStorage = shouldAllow
    }

    fun setAudioList(newList: List<AudioItemModel>) {
        _audioList.clear()
        if (allowAudioFromDeviceStorage) {
            _audioList.add(
                AudioItemModel(
                    "", "", "", "", "",
                    itemType = AudioItemType.UPLOAD_FROM_DEVICE
                )
            )
        }
        _audioList.addAll(newList)
        val selectedItemIdx = audioList.indexOfFirst { it.url == selectedAudioItem?.url }
        if (selectedItemIdx != -1) {
            _audioList[selectedItemIdx].isSelected = true
            _selectedAudioItem = audioList[selectedItemIdx]
            selectedAudioItemIndex = selectedItemIdx
        }
    }

    fun resetItemsUI() {
        // resetting currentPlaying UI
        val currPlayingIdx = audioList.indexOfFirst { it.isPlayingCurrently }
        if (currPlayingIdx != -1) {
            _audioList[currPlayingIdx].isPlayingCurrently = false
        }
        // resetting loading UI
        val loadingIdx = audioList.indexOfFirst { it.isLoading }
        if (loadingIdx != -1) {
            _audioList[loadingIdx].isLoading = false
        }
        currentPlaying = -1
    }

    fun updateAudioFromStorage(audioName: String, uri: Uri) {
        _audioList[0].apply {
            name = audioName
            storageUri = uri
        }
        updateSelectedAudio(0)
    }

    fun updateSelectedAudio(position: Int) {
        if (position < 0 || position >= audioList.size) {
            return
        }
        if (selectedAudioItemIndex == position) {
            updateItemSelection(position = position, isSelected = false)
            selectedAudioItemIndex = -1
            return
        }
        if (selectedAudioItemIndex != -1) {
            // unselect the currently selected audio item
            updateItemSelection(position = selectedAudioItemIndex, isSelected = false)
        }
        // select the new audio item
        selectedAudioItemIndex = position
        updateItemSelection(position = selectedAudioItemIndex, isSelected = true)
    }

    private fun updateItemSelection(position: Int, isSelected: Boolean) = viewModelScope.launch {
        if (position < 0 || position >= audioList.size) {
            return@launch
        }
        selectedAudioItemIndex = position
        _audioList[position].isSelected = isSelected
        _audioListNotifier.send(
            AudioListNotifier.NotifyItemChanged(
                position, SelectAudioBottomSheet.UPDATE_SELECTED_STATE
            )
        )
        _selectedAudioItem = if (isSelected) audioList[selectedAudioItemIndex] else null
    }

    fun bringSelectedItemToTop() = viewModelScope.launch {
        if (selectedAudioItemIndex < 0 || selectedAudioItemIndex >= _audioList.size || selectedAudioItem == null) {
            return@launch
        }
        _audioList.removeAt(selectedAudioItemIndex)
        val newIndex = if (selectedAudioItem?.itemType == AudioItemType.UPLOAD_FROM_DEVICE) {
            0
        } else if (allowAudioFromDeviceStorage) 1 else 0
        _audioList.add(newIndex, _selectedAudioItem!!)
        selectedAudioItemIndex = newIndex
        _audioListNotifier.send(
            AudioListNotifier.NotifyDataChanged
        )
    }

    fun updatePlayPause(position: Int = currentPlaying, isPlaying: Boolean) = viewModelScope.launch {
        if (position < 0 || position >= audioList.size) {
            return@launch
        }
        _audioList[position].isPlayingCurrently = isPlaying
        _audioListNotifier.send(
            AudioListNotifier.NotifyItemChanged(
                position, SelectAudioBottomSheet.UPDATE_PLAY_PAUSE
            )
        )
    }

    fun updateProgress(position: Int = currentPlaying, isLoading: Boolean) = viewModelScope.launch {
        if (position < 0 || position >= audioList.size) {
            return@launch
        }
        _audioList[position].isLoading = isLoading
        _audioListNotifier.send(
            AudioListNotifier.NotifyItemChanged(
                position, SelectAudioBottomSheet.UPDATE_LOADING_STATE
            )
        )
    }

}
