package com.example.funstuff01.ui.mediaPreview

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.view.isVisible
import com.example.funstuff01.databinding.ActivityMediaPreviewBinding
import com.example.funstuff01.ui.base.BaseActivity
import com.example.funstuff01.utils.hideStatusBar
import com.example.funstuff01.utils.showStatusBar
import java.io.File

class MediaPreviewActivity : BaseActivity<ActivityMediaPreviewBinding>(ActivityMediaPreviewBinding::inflate) {

    private var mediaPath: String? = null
    private var isImage: Boolean = false

    override fun initUserInterface() {

        mediaPath = intent?.extras?.getString(MEDIA_PATH_EXTRA)
        isImage = intent?.extras?.getBoolean(IS_IMAGE_EXTRA) ?: false

        setPreviewMedia()
    }

    private fun setPreviewMedia() {
        if(mediaPath.isNullOrBlank()) {
            return
        }

        if(isImage) {
            val file = File(mediaPath!!)
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            binding.imageView.apply {
                isVisible = true
                setImageBitmap(bitmap)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        this.hideStatusBar()
    }

    override fun onPause() {
        super.onPause()
        this.showStatusBar()
    }


    companion object {
        const val MEDIA_PATH_EXTRA = "MEDIA_PATH_EXTRA"
        const val IS_IMAGE_EXTRA = "IS_IMAGE_EXTRA"

        fun startActivity(context: Context, mediaPath: String, isImage: Boolean) {
            val intent = Intent(context, MediaPreviewActivity::class.java).apply {
                putExtra(MEDIA_PATH_EXTRA, mediaPath)
                putExtra(IS_IMAGE_EXTRA, isImage)
            }
            context.startActivity(intent)
        }
    }
}