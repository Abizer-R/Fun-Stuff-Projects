package com.example.funstuff01.ui.mediaPreview

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.view.isVisible
import com.example.funstuff01.R
import com.example.funstuff01.databinding.ActivityMediaPreviewBinding
import com.example.funstuff01.ui.base.BaseActivity
import com.example.funstuff01.utils.hideStatusBar
import com.example.funstuff01.utils.showStatusBar
import com.example.funstuff01.utils.toast
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import java.io.File

class MediaPreviewActivity : BaseActivity<ActivityMediaPreviewBinding>(ActivityMediaPreviewBinding::inflate) {

    private var mediaPath: String? = null
    private var isImage: Boolean = false
    private var player: ExoPlayer? = null

    override fun initUserInterface() {

        mediaPath = intent?.extras?.getString(MEDIA_PATH_EXTRA)
        isImage = intent?.extras?.getBoolean(IS_IMAGE_EXTRA) ?: false

        setPreviewMedia()
    }

    private fun setPreviewMedia() {
        if(mediaPath.isNullOrBlank()) {
            toast(getString(R.string.media_path_is_null_or_blank))
            return
        }
        if (File(mediaPath!!).exists().not()) {
            toast(getString(R.string.file_not_found))
            return
        }

        if(isImage) {
            setupImage(mediaPath!!)
        } else {
            setupVideo(mediaPath!!)
        }
    }

    private fun setupImage(imageFilePath: String) {
        binding.imageView.isVisible = true
        binding.playerView.isVisible = false
        val file = File(imageFilePath)
        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
        binding.imageView.setImageBitmap(bitmap)
    }

    private fun setupVideo(videoFilePath: String) {
        binding.imageView.isVisible = false
        binding.playerView.isVisible = true

        val playerBuilder = ExoPlayer.Builder(this)
            .setRenderersFactory(
                DefaultRenderersFactory(this)
                    .setEnableDecoderFallback(true)
            )

        player = playerBuilder.build()
        player?.repeatMode = Player.REPEAT_MODE_ONE
        player?.playWhenReady = true
        player?.volume = 1f

        binding.playerView.player = player

        val videoUri = Uri.parse(videoFilePath)
        val mediaItem = MediaItem.Builder().setUri(videoUri).build()

        player?.apply {
            setMediaItem(mediaItem)
            prepare()
            addListener(object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) {
                    super.onPlayerError(error)
                    error.printStackTrace()
                    toast(getString(R.string.failed_to_play_video))
                }

                override fun onPlayerErrorChanged(error: PlaybackException?) {
                    super.onPlayerErrorChanged(error)
                    error?.printStackTrace()
                    toast(getString(R.string.failed_to_play_video))
                }
            })
        }
    }

    override fun onResume() {
        super.onResume()
        this.hideStatusBar()
        player?.play()
    }

    override fun onPause() {
        super.onPause()
        this.showStatusBar()
        player?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.stop()
        player?.release()
    }

    companion object {
        const val MEDIA_PATH_EXTRA = "MEDIA_PATH_EXTRA"
        const val IS_IMAGE_EXTRA = "IS_IMAGE_EXTRA"

        fun startActivity(context: Context, mediaPath: String, isImage: Boolean = true) {
            val intent = Intent(context, MediaPreviewActivity::class.java).apply {
                putExtra(MEDIA_PATH_EXTRA, mediaPath)
                putExtra(IS_IMAGE_EXTRA, isImage)
            }
            context.startActivity(intent)
        }
    }
}