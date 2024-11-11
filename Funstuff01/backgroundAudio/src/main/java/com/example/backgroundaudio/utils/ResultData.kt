package com.example.backgroundaudio.utils

import androidx.annotation.Keep

@Keep
sealed class ResultData<out T> {
    data class Loading(val nothing: Nothing? = null) : ResultData<Nothing>()
    data class Success<out T>(val data: T) : ResultData<T>()
    data class Failed(val exception: Throwable? = null, val errorCode: String? = null, val message: String? = null, val errorBody: String? = null) :
        ResultData<Nothing>()
}