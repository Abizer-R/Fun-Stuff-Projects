package com.example.backgroundaudio.audioList.fragment.child

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.backgroundaudio.audioList.adapter.AudioAdapter.Companion.UPDATE_LOADING_STATE
import com.example.backgroundaudio.audioList.adapter.AudioAdapter.Companion.UPDATE_PLAY_PAUSE
import com.example.backgroundaudio.audioList.adapter.AudioAdapter.Companion.UPDATE_SELECTED_STATE
import com.example.backgroundaudio.audioList.adapter.AudioItemModel
import com.example.backgroundaudio.utils.AudioListNotifier
import com.example.backgroundaudio.utils.DummyAudioList
import com.example.backgroundaudio.utils.DummyAudioList.MusicListResponse
import com.example.backgroundaudio.utils.ResultData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import kotlin.random.Random

@OptIn(ObsoleteCoroutinesApi::class)
@HiltViewModel
class AudioListViewModel
@Inject constructor(
) : ViewModel() {

    private val _audioList = arrayListOf<AudioItemModel>()
    val audioList: List<AudioItemModel> get() = _audioList

    private val _audioListResponseLiveData =
        MutableLiveData<ResultData<MusicListResponse>>()
    val audioListResponseLiveData: LiveData<ResultData<MusicListResponse>> get() = _audioListResponseLiveData

    val _audioListNotifier = BroadcastChannel<AudioListNotifier>(Channel.BUFFERED)
    var currentPlaying: Int = -1

    var selectedAudioItemIndex: Int = -1
        private set
    val selectedAudioItem: AudioItemModel?
        get() = audioList.getOrNull(selectedAudioItemIndex)

    var initiallySelectedAudioItem: AudioItemModel? = null

    private var audioListApiJob: Job? = null

    private var pageCount = 1
    private val pageSize = 5
    val isLastPage = AtomicBoolean(false)
    val isLoading = AtomicBoolean(false)
    private var searchQuery: String? = null

    fun updateSearchQuery(searchQuery: String?) {
        this.searchQuery = searchQuery
        pageCount = 1
        isLastPage.set(false)
    }

    fun fetchAudioList(
        searchQuery: String? = this.searchQuery
    ) {
        audioListApiJob?.cancel()
        audioListApiJob = viewModelScope.launch {

            if (pageCount == 1) {
                _audioList.clear()
                _audioListNotifier.send(
                    AudioListNotifier.NotifyDataChanged
                )
            }

            DummyAudioList.getMusicList(
                page = pageCount,
                limit = pageSize,
                keyword = searchQuery
            ).onEach { result ->
                when (result) {
                    is ResultData.Loading -> {
                        if (audioList.isNotEmpty()) {
                            // we will show default progressbar for first time
                            // and then local loadingView in recyclerview
                            addLoader()
                        }
                        isLoading.set(true)
                    }

                    is ResultData.Failed -> {
                        removeLoaderIfAdded()
                        isLoading.set(false)
                    }

                    is ResultData.Success -> {
                        Log.i("TEST_list", "ResultData.Success: listSize BEFORE = ${audioList.size}")
                        removeLoaderIfAdded()
//                        removeLoaderIfAdded().apply { join() }
                        pageCount++

                        val response = result.data
                        isLoading.set(false)
                        isLastPage.set(response.isLastPage)

                        val startIndex = audioList.size
                        _audioList.addAll(response.musicList)
                        updateInitiallySelectedItem()
                        Log.v("TEST_list", "ResultData.Success: listSize AFTER = ${audioList.size}")
                        _audioListNotifier.send(
                            AudioListNotifier.NotifyItemRangeAdded(
                                startIndex, response.musicList.size
                            )
                        )
                    }
                }
                _audioListResponseLiveData.postValue(result)
            }.collect()
        }
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


    private suspend fun addLoader() {
        _audioList.add(
            AudioItemModel(
                index = System.currentTimeMillis().toString(),
                isLoader = true
            )
        )
        Log.v("TEST_list", "addLoader(): index = ${audioList.lastIndex}")
        _audioListNotifier.send(
            AudioListNotifier.NotifyItemAdded(audioList.lastIndex)
        )
    }

    private suspend fun removeLoaderIfAdded() {
        // launching separate viewModelScope might cause inconsistency exception
        audioList.reversed().filter {
            it.isLoader
        }.forEach {
            val index = audioList.indexOf(it)
            if (index != -1) {
                Log.v("TEST_list", "removeLoaderIfAdded(): removeAt = ${index}")
                _audioList.removeAt(index)
                _audioListNotifier.send(
                    AudioListNotifier.NotifyItemRemoved(index)
                )
            }
        }
    }

    fun resetItems() = viewModelScope.launch {
        _audioList.clear()
        currentPlaying = -1
        pageCount = 1
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
