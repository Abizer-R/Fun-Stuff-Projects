package com.anilokcun.uwmediapicker.viewholder

import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.anilokcun.uwmediapicker.R
import com.anilokcun.uwmediapicker.listener.GalleryMediaOnLongClickListener
import com.anilokcun.uwmediapicker.listener.OnRVItemClickListener
import com.anilokcun.uwmediapicker.model.GalleryImageModel
import com.bumptech.glide.Glide

/**
 * Author     	:	Anıl Furkan Ökçün
 * Author mail	:	anilfurkanokcun@gmail.com
 * Create Date	:	30.08.2018
 */

internal class GalleryImageVH(itemView: View) : BaseGalleryMediaVH(itemView) {
	
	private val imgThumbnail = itemView.findViewById<ImageView>(R.id.item_gallery_image_img_thumbnail)
	private val imgSelected = itemView.findViewById<ImageView>(R.id.item_gallery_image_img_selected)
	
	fun bind(item: GalleryImageModel, galleryGridSize: Int, onMediaClickListener: OnRVItemClickListener, galleryMediaOnLongClickListener: GalleryMediaOnLongClickListener?) {
		// Bind item click listener, thumbnail and selected icon in the base class
		super.bind(item, galleryGridSize, onMediaClickListener, imgThumbnail, imgSelected)

		val placeHolderDrawable = ColorDrawable(ContextCompat.getColor(itemView.context, R.color.colorUwMediaPickerImagePlaceHolder))
		val urlToLoad = item.mediaContentUri ?: item.mediaPath

		Glide.with(itemView)
			.load(urlToLoad)
			.override(galleryGridSize, galleryGridSize)
			.placeholder(placeHolderDrawable)
			.error(placeHolderDrawable)
			.centerCrop()
			.into(imgThumbnail)

		// Item Long Click Event
		itemView.setOnLongClickListener {
			galleryMediaOnLongClickListener?.onLongClick(item.mediaPath)
			false
		}
	}
}