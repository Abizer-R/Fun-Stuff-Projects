package com.example.backgroundaudio.audioList

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.backgroundaudio.audioList.adapter.AudioItemModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SharedAudioViewModel
@Inject constructor(
) : ViewModel() {

    private var _selectedAudioItem: AudioItemModel? = null
    val selectedAudioItem get() = _selectedAudioItem

    val searchQuery: String?
        get() = searchQueryLiveData.value

    private val _searchQueryLiveData = MutableLiveData<String?>()
    val searchQueryLiveData: LiveData<String?> get() = _searchQueryLiveData

    fun updateSearchQuery(query: String?) {
        _searchQueryLiveData.postValue(query)
    }


    fun updateSelectedItem(audioItem: AudioItemModel?) {
        _selectedAudioItem = audioItem
    }
}
