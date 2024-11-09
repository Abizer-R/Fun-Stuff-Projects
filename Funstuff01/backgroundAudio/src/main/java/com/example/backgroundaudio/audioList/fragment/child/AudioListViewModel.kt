package com.example.backgroundaudio.audioList.fragment.child

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.backgroundaudio.audioList.adapter.AudioAdapter.Companion.UPDATE_LOADING_STATE
import com.example.backgroundaudio.audioList.adapter.AudioAdapter.Companion.UPDATE_PLAY_PAUSE
import com.example.backgroundaudio.audioList.adapter.AudioAdapter.Companion.UPDATE_SELECTED_STATE
import com.example.backgroundaudio.audioList.adapter.AudioItemModel
import com.example.backgroundaudio.utils.AudioListNotifier
import com.example.backgroundaudio.utils.DummyAudioList
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ObsoleteCoroutinesApi::class)
@HiltViewModel
class AudioListViewModel
@Inject constructor(
) : ViewModel() {

    private val _audioList = arrayListOf<AudioItemModel>()
    val audioList: List<AudioItemModel> get() = _audioList

    val _audioListNotifier = BroadcastChannel<AudioListNotifier>(Channel.BUFFERED)
    var currentPlaying: Int = -1

    var selectedAudioItemIndex: Int = -1
        private set
    val selectedAudioItem: AudioItemModel?
        get() = audioList.getOrNull(selectedAudioItemIndex)

    var initiallySelectedAudioItem: AudioItemModel? = null

    fun fetchAudioList() = viewModelScope.launch {
        val newList = DummyAudioList.getMusicList()
        _audioList.addAll(newList)
        updateInitiallySelectedItem()
        _audioListNotifier.send(AudioListNotifier.NotifyDataChanged)
    }

    private fun updateInitiallySelectedItem() {
        val initiallySelectedItemIdx =
            audioList.indexOfFirst { it.url == initiallySelectedAudioItem?.url }
        if (initiallySelectedItemIdx in 0..audioList.lastIndex) {
            _audioList[initiallySelectedItemIdx].isSelected = true
            selectedAudioItemIndex = initiallySelectedItemIdx
            initiallySelectedAudioItem = null
        }
    }

    fun resetItems() = viewModelScope.launch {
        _audioList.clear()
        currentPlaying = -1
        _audioListNotifier.send(
            AudioListNotifier.NotifyDataChanged
        )
    }

    fun selectItem(audioItemModel: AudioItemModel?) {
        val position = audioList.indexOfFirst { it.url == audioItemModel?.url }
        selectItem(position)
    }

    fun selectItem(position: Int) = viewModelScope.launch {
        if (position !in 0..audioList.lastIndex)
            return@launch

        val unselectJob = unSelectItem()
        unselectJob.join()

        selectedAudioItemIndex = position

        _audioList[position].isSelected = true
        _audioListNotifier.send(
            AudioListNotifier.NotifyItemChanged(
                position, UPDATE_SELECTED_STATE
            )
        )
    }

    fun unSelectItem() = viewModelScope.launch {
        if (selectedAudioItemIndex !in 0..audioList.lastIndex)
            return@launch

        val prevSelectedPos = selectedAudioItemIndex
        selectedAudioItemIndex = -1

        _audioList[prevSelectedPos].isSelected = false
        _audioListNotifier.send(
            AudioListNotifier.NotifyItemChanged(
                prevSelectedPos, UPDATE_SELECTED_STATE
            )
        )
    }

    fun resetCurrentItemState() = viewModelScope.launch {
        updatePlayingState(false)
        updateLoadingState(false)
        currentPlaying = -1
    }

    fun updatePlayingState(
        isPlaying: Boolean?,
        position: Int = currentPlaying
    ) = viewModelScope.launch {
        if (position !in 0..audioList.lastIndex)
            return@launch
        _audioList[position].isPlayingCurrently = isPlaying == true
        _audioListNotifier.send(
            AudioListNotifier.NotifyItemChanged(
                position, UPDATE_PLAY_PAUSE
            )
        )
    }

    fun updateLoadingState(
        isLoading: Boolean,
        position: Int = currentPlaying
    ) = viewModelScope.launch {
        if (position !in 0..audioList.lastIndex)
            return@launch
        _audioList[position].isLoading = isLoading
        _audioListNotifier.send(
            AudioListNotifier.NotifyItemChanged(
                position, UPDATE_LOADING_STATE
            )
        )
    }
}
