package com.anilokcun.uwmediapicker.model

import android.net.Uri

/**
 * Author     	:	Anıl Furkan Ökçün
 * Author mail	:	anilfurkanokcun@gmail.com
 * Create Date	:	30.08.2018
 */

internal data class GalleryImageModel(
	override var mediaPath: String?,
	override var selected: Boolean,
	override val mediaContentUri: Uri? = null,
	override val itemType: Int = BaseGalleryModel.TYPE_GALLERY_IMAGE
) : BaseGalleryModel, BaseGalleryMediaModel