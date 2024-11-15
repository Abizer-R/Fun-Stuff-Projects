package com.example.backgroundaudio.utils.uploadAudio

import androidx.annotation.StringRes
import com.example.backgroundaudio.R
import com.example.backgroundaudio.utils.uploadAudio.MusicTitleValidationError.INVALID_CHARACTERS
import com.example.backgroundaudio.utils.uploadAudio.MusicTitleValidationError.INVALID_FIRST_CHARACTER
import com.example.backgroundaudio.utils.uploadAudio.MusicTitleValidationError.LESS_THAN_6_CHARACTERS

object MusicTitleValidator {

    fun isValid(input: String?): MusicTitleValidationState {
        val mInput = input ?: return MusicTitleValidationState.Invalid(LESS_THAN_6_CHARACTERS)
        if (isFirstCharacterValid(mInput).not()) {
            return MusicTitleValidationState.Invalid(INVALID_FIRST_CHARACTER)
        }
        if (mInput.length < 6) {
            return MusicTitleValidationState.Invalid(LESS_THAN_6_CHARACTERS)
        }

        val regex = "^[a-zA-Z0-9._\\s-]+$".toRegex()
        if (regex.matches(mInput)) {
            return MusicTitleValidationState.Valid
        } else {
            return MusicTitleValidationState.Invalid(INVALID_CHARACTERS)
        }
    }

    private fun isFirstCharacterValid(input: String): Boolean {
        val firstChar: String = "" + input.getOrNull(0)
        val regexFirstCharacter = "^[a-zA-Z0-9_]+$".toRegex()  // only start with alphabet, number or underscore
        return regexFirstCharacter.matches(firstChar)
    }
}

sealed class MusicTitleValidationState {
    object Valid: MusicTitleValidationState()
    data class Invalid(
        val error: MusicTitleValidationError,
        @StringRes val errorMsgResId: Int = error.getErrorStringResource(),
    ): MusicTitleValidationState()
}

enum class MusicTitleValidationError {
    INVALID_FIRST_CHARACTER,
    LESS_THAN_6_CHARACTERS,
    INVALID_CHARACTERS,
}

@StringRes
fun MusicTitleValidationError.getErrorStringResource(): Int {
    return when (this) {
        INVALID_FIRST_CHARACTER -> R.string.music_title_first_character_error
        LESS_THAN_6_CHARACTERS -> R.string.music_title_length_error
        INVALID_CHARACTERS -> R.string.invalid_music_title_instruction
    }
}