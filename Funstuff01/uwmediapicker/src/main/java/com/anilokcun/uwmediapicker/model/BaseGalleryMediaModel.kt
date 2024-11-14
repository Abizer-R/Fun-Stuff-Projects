package com.anilokcun.uwmediapicker.model

import android.net.Uri

/**
 * Author     	:	Anıl Furkan Ökçün
 * Author mail	:	anilfurkanokcun@gmail.com
 * Create Date	:	30.08.2018
 */

internal interface BaseGalleryMediaModel : BaseGalleryModel {
	var mediaPath: String?
	val mediaContentUri: Uri?
	var selected: Boolean
}