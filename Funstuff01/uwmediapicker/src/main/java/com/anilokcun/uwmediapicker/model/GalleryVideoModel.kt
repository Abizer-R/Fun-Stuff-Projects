package com.anilokcun.uwmediapicker.model

import android.net.Uri

/**
 * Author     	:	Anıl Furkan Ökçün
 * Author mail	:	anilfurkanokcun@gmail.com
 * Create Date	:	30.08.2018
 */

internal data class GalleryVideoModel(
	override var mediaPath: String?,
	override var selected: Boolean,
	val videoDuration: Long,
	val videoSize: String?,
	override val mediaContentUri: Uri? = null,
	override val itemType: Int = BaseGalleryModel.TYPE_GALLERY_VIDEO
) : BaseGalleryModel, BaseGalleryMediaModel