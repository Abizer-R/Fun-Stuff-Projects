package com.example.backgroundaudio.audioList

import android.content.Context
import com.example.backgroundaudio.audioList.adapter.AudioItemModel
import com.example.backgroundaudio.utils.AudioProcessorUtils
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class PlayerHandler @Inject constructor(
    @ActivityContext private val context: Context
) {

    private var player: ExoPlayer? = null
    private var audioItem: AudioItemModel? = null

    val isPlaying:  Boolean
        get() = player?.isPlaying == true
    val playbackState: Int?
        get() = player?.playbackState

    init {
        player = ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ONE
        }
    }

    fun addPlayerListener(playerListener: Player.Listener) {
        player?.addListener(playerListener)
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
    }

}