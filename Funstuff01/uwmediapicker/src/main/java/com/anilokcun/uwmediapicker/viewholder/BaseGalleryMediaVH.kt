package com.anilokcun.uwmediapicker.viewholder

import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.anilokcun.uwmediapicker.R
import com.anilokcun.uwmediapicker.helper.toUri
import com.anilokcun.uwmediapicker.listener.OnRVItemClickListener
import com.anilokcun.uwmediapicker.model.BaseGalleryMediaModel
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

/**
 * Author     	:	Anıl Furkan Ökçün
 * Author mail	:	anilfurkanokcun@gmail.com
 * Create Date	:	30.08.2018
 */

internal open class BaseGalleryMediaVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
	
	open fun bind(item: BaseGalleryMediaModel, galleryGridSize: Int, onMediaClickListener: OnRVItemClickListener, imgThumbnail: ImageView, imgSelected: ImageView?) {
		// Thumbnail
		imgThumbnail.layoutParams.height = galleryGridSize
		imgThumbnail.layoutParams.width = galleryGridSize
		
		// Selected Icon
		imgSelected?.isActivated = item.selected

		// Item Click Event
		itemView.setOnClickListener {
			onMediaClickListener.onClick(adapterPosition)
		}

	}
}