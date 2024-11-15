package com.example.backgroundaudio.utils.uploadAudio

import androidx.annotation.StringRes
import com.example.backgroundaudio.R
import com.example.backgroundaudio.utils.uploadAudio.UploadMusicValidationError.INVALID_MUSIC_TITLE
import com.example.backgroundaudio.utils.uploadAudio.UploadMusicValidationError.COVER_IMAGE_NOT_SELECTED
import java.io.File

object UploadMusicValidator {

    fun isValid(musicTitle: String?, coverImagePath: String?): UploadMusicValidationState {
        if (isMusicTitleValid(musicTitle).not()) {
            return UploadMusicValidationState.Invalid(INVALID_MUSIC_TITLE)
        }

        if (isCoverImagePathValid(coverImagePath).not()) {
            return UploadMusicValidationState.Invalid(COVER_IMAGE_NOT_SELECTED)
        }

        return UploadMusicValidationState.Valid
    }

    private fun isMusicTitleValid(musicTitle: String?): Boolean {
        return MusicTitleValidator.isValid(musicTitle) is MusicTitleValidationState.Valid
    }

    private fun isCoverImagePathValid(coverImagePath: String?): Boolean {
        return !coverImagePath.isNullOrBlank() && File(coverImagePath).exists()
    }
}

sealed class UploadMusicValidationState {
    object Valid: UploadMusicValidationState()
    data class Invalid(
        val error: UploadMusicValidationError,
        @StringRes val errorMsgResId: Int = error.getErrorStringResource(),
    ): UploadMusicValidationState()
}

enum class UploadMusicValidationError {
    INVALID_MUSIC_TITLE,
    COVER_IMAGE_NOT_SELECTED,
}

@StringRes
fun UploadMusicValidationError.getErrorStringResource(): Int {
    return when (this) {
        INVALID_MUSIC_TITLE -> R.string.please_enter_valid_music_title
        COVER_IMAGE_NOT_SELECTED -> R.string.please_select_cover_image
    }
}