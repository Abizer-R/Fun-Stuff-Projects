package com.example.backgroundaudio

import android.content.Context
import android.provider.MediaStore.Audio
import com.example.backgroundaudio.utils.AudioProcessorUtils
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player

class PlayerHandler {

    private var context: Context? = null
    private var player: ExoPlayer? = null
    private var audioItem: AudioItemModel? = null

    val isPlaying:  Boolean
        get() = player?.isPlaying == true
    val playbackState: Int?
        get() = player?.playbackState

    fun initialize(
        context: Context,
        playerListener: Player.Listener? = null
    ) {
        this.context = context
        player = ExoPlayer.Builder(context).build()
        player?.apply {
            repeatMode = Player.REPEAT_MODE_ONE
            playerListener?.let { addListener(playerListener) }
        }
    }

    fun initMediaItem(audioItem: AudioItemModel) {
        this.audioItem = audioItem
        player?.let { player ->
            player.setMediaItem(AudioProcessorUtils.getMediaItem(audioItem))
            player.prepare()
            player.play()
        }
    }

    fun playCurrent(shouldPrepare: Boolean = false) {
        if (shouldPrepare) player?.prepare()
        player?.playWhenReady = true
    }

    fun pause() {
        player?.pause()
    }

    fun stop() {
        player?.stop()
    }

    fun release() {
        player?.stop()
        player?.release()
    }

    fun destroy() {
        player?.stop()
        player?.release()
        player = null
        context = null
    }

}