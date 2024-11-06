package com.example.backgroundaudio

import android.content.Context
import com.example.backgroundaudio.utils.AudioProcessorUtils
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player

class PlayerHandler {

    private val audioList = arrayListOf<AudioItemModel>()
    private var context: Context? = null
    private var player: ExoPlayer? = null

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
        playerListener?.let {
            player?.addListener(playerListener)
        }
    }

    fun setAudioList(list: List<AudioItemModel>) {
        this.audioList.clear()
        this.audioList.addAll(list)
    }

    fun initMediaItem(audioItem: AudioItemModel) {
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