package com.anilokcun.uwmediapicker.model

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UwMediaPickerMediaModel(
	var mediaPath: String,
	val mediaType: UwMediaPickerMediaType,
	var mediaContentUri: Uri? = null
) : Parcelable