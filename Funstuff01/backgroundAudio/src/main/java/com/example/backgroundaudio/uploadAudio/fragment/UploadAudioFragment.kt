package com.example.backgroundaudio.uploadAudio.fragment

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieDrawable
import com.airbnb.lottie.RenderMode
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.backgroundaudio.R
import com.example.backgroundaudio.audioList.adapter.AudioItemModel
import com.example.backgroundaudio.databinding.FragmentUploadAudioBinding
import com.example.backgroundaudio.utils.AudioProcessorUtils
import com.example.backgroundaudio.utils.AudioProcessorUtils.millisecondToTime
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class UploadAudioFragment : Fragment() {

    companion object {

        const val AUDIO_FILE_PATH = "audio_file_path"
        const val AUDIO_FILE_NAME = "audio_file_name"

        fun newInstance(
            audioFilePath: String,
            audioFileName: String? = null
        ) = UploadAudioFragment().apply {
            arguments = bundleOf(
                AUDIO_FILE_PATH to audioFilePath,
                AUDIO_FILE_NAME to audioFileName
            )
        }
    }

    private var _binding: FragmentUploadAudioBinding? = null
    private val binding: FragmentUploadAudioBinding
        get() = requireNotNull(_binding)

    private var exoPlayerAudio: ExoPlayer? = null
    private var loadingDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            arguments?.putString(AUDIO_FILE_PATH, savedInstanceState.getString(AUDIO_FILE_PATH))
            arguments?.putString(AUDIO_FILE_NAME, savedInstanceState.getString(AUDIO_FILE_NAME))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUploadAudioBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val audioFilePath = arguments?.getString(AUDIO_FILE_PATH)

        if (audioFilePath.isNullOrBlank() || File(audioFilePath).exists().not()) {
            showErrorLayout(getString(R.string.audio_file_not_found))
        } else {
            setupAudioExoPlayer(audioFilePath)
            setupViews(audioFilePath)
            setObservers()
        }
    }

    private fun setupAudioExoPlayer(audioFilePath: String) {
        exoPlayerAudio = ExoPlayer.Builder(requireContext()).build()

        val mediaItem = MediaItem.fromUri(Uri.parse(audioFilePath))
        exoPlayerAudio?.setMediaItem(mediaItem)
        exoPlayerAudio?.prepare()

        exoPlayerAudio?.addListener(object : Player.Listener {

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)
                if (isPlaying) {
                    showPlayingState()
                } else {
                    showPauseState()
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                super.onPlayerError(error)
                showErrorLayout(errorMsg = getString(R.string.failed_to_play_audio) + "\n" + error.message)
                Log.e("TEST_exo", "onPlayerError: error = $error")
            }

            override fun onPlayerErrorChanged(error: PlaybackException?) {
                super.onPlayerErrorChanged(error)
                showErrorLayout(getString(R.string.failed_to_play_audio) + "\n" + error?.message)
                Log.e("TEST_exo", "onPlayerErrorChanged: error = $error")
            }
        })
    }

    private fun setupViews(audioFilePath: String) {
        val audioLengthMillis = AudioProcessorUtils.getDurationMillis(
            context = requireContext(),
            audioUri = Uri.fromFile(File(audioFilePath))
        )
        val audioFileName = arguments?.getString(AUDIO_FILE_NAME)
            ?: File(audioFilePath).nameWithoutExtension
        with(binding) {

            llContainer.isVisible = true
            errorLayout.clErrorLayout.isVisible = false

            val realName = "John Doe"   // TODO: get from preference manager
            tvUserName.text = realName
            tvAuthorName.text = realName

            tvAudioDuration.text = millisecondToTime(audioLengthMillis)

            tvAudioTitle.text = audioFileName
            etMusicTitle.setText(audioFileName)


            lottiePlayingAnim.apply {
                setRenderMode(RenderMode.HARDWARE)
                setAnimation(R.raw.sound_wave_one)
                repeatCount = LottieDrawable.INFINITE
                speed = 2F
            }

            imgPlayPause.setOnClickListener {
                togglePlayPause()
            }

            etMusicTitle.doOnTextChanged { text, _, _, _ ->
                tvAudioTitle.text = text
            }


            val profilePicUrl = "--" // TODO: get from preference manager
            Glide.with(requireContext())
                .load(profilePicUrl)
                .placeholder(R.drawable.new_profile_placeholder)
                .error(R.drawable.new_profile_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imgProfilePic)
        }
    }


    private fun setObservers() {
    }


    private fun showErrorLayout(errorMsg: String) {
        hideLoadingDialog()
        releasePlayer()
        binding.llContainer.isVisible = false
        binding.errorLayout.clErrorLayout.isVisible = true
        binding.errorLayout.btnRetry.isVisible = false
        binding.errorLayout.btnClose.isVisible = true
        binding.errorLayout.errorTitleTextView.text = errorMsg
        binding.errorLayout.btnClose.setOnClickListener {
            activity?.onBackPressedDispatcher?.onBackPressed()
        }
    }

    private fun showPlayingState() {
        binding.imgPlayPause.setImageResource(R.drawable.ic_pause_circle_outline)
        binding.audioIcon.isVisible = false
        binding.lottiePlayingAnim.isVisible = true
        binding.lottiePlayingAnim.playAnimation()
    }

    private fun showPauseState() {
        binding.imgPlayPause.setImageResource(R.drawable.ic_play_circle_outline)
        binding.audioIcon.isVisible = true
        binding.lottiePlayingAnim.isVisible = false
        binding.lottiePlayingAnim.pauseAnimation()
    }


    private fun togglePlayPause() = activity?.runOnUiThread {
        if (exoPlayerAudio?.isPlaying == true) {
            exoPlayerAudio?.pause()
        } else {
            exoPlayerAudio?.play()
        }
    }

    private fun releasePlayer() = activity?.runOnUiThread {
        exoPlayerAudio?.release()
    }


    private fun showLoadingDialog() = activity?.runOnUiThread {
        val mActivity = activity ?: return@runOnUiThread
        if (loadingDialog == null) {
            val builder = AlertDialog.Builder(mActivity)
            builder.setCancelable(false)
            loadingDialog = builder.create()
        }
        loadingDialog?.show()
    }

    private fun hideLoadingDialog() = activity?.runOnUiThread {
        loadingDialog?.dismiss()
        loadingDialog = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(AUDIO_FILE_PATH, arguments?.getString(AUDIO_FILE_PATH))
        outState.putString(AUDIO_FILE_NAME, arguments?.getString(AUDIO_FILE_NAME))
    }

    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
    }

}