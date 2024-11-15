package com.example.backgroundaudio.uploadAudio.fragment

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieDrawable
import com.airbnb.lottie.RenderMode
import com.anilokcun.uwmediapicker.UwMediaPicker
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.example.backgroundaudio.R
import com.example.backgroundaudio.databinding.FragmentUploadAudioBinding
import com.example.backgroundaudio.utils.AudioPermissionUtils
import com.example.backgroundaudio.utils.AudioPermissionUtils.GalleryAccessLevel
import com.example.backgroundaudio.utils.AudioProcessorUtils
import com.example.backgroundaudio.utils.AudioProcessorUtils.millisecondToTime
import com.example.backgroundaudio.utils.toast
import com.example.backgroundaudio.utils.uploadAudio.MusicTitleValidationState
import com.example.backgroundaudio.utils.uploadAudio.MusicTitleValidator
import com.example.backgroundaudio.utils.uploadAudio.UploadMusicValidationState
import com.example.backgroundaudio.utils.uploadAudio.UploadMusicValidator
import com.example.backgroundaudio.utils.uploadAudio.getErrorStringResource
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
    private var coverImagePath: String = ""

    private lateinit var galleryPermissionsLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var cropImage: ActivityResultLauncher<CropImageContractOptions>

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
            initLaunchers()
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

    private fun initLaunchers() {
        galleryPermissionsLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                val galleryAccessLevel = AudioPermissionUtils.getGalleryAccessLevel(requireContext())
                when (galleryAccessLevel) {
                    GalleryAccessLevel.PARTIAL_ACCESS,
                    GalleryAccessLevel.FULL_ACCESS -> {
                        openGallery()
                    }
                    GalleryAccessLevel.NO_ACCESS -> {
                        context?.toast(getString(R.string.gallery_permission_is_needed_for_this_action))
                    }
                }
            }

        cropImage = registerForActivityResult(CropImageContract()) { result ->
            when {
                result.isSuccessful -> {
                    val croppedImagePath = result.getUriFilePath(requireContext())
                    handleCroppedImage(croppedImagePath)
                }

                result is com.canhub.cropper.CropImage.CancelledResult -> { }

                else -> {
                    toast(getString(R.string.something_went_wrong_msg))
                }
            }
        }
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

            imgCustomCover.setOnClickListener {
                galleryPermissionsLauncher.launch(AudioPermissionUtils.getGalleryPermissionsArray())
            }

            etMusicTitle.doOnTextChanged { text, _, _, _ ->
                val titleString = (text ?: "").toString()
                tvAudioTitle.text = titleString
                val validationState = MusicTitleValidator.isValid(titleString)
                if (validationState is MusicTitleValidationState.Invalid) {
                    etMusicTitle.error = getString(validationState.errorMsgResId)
                } else {
                    etMusicTitle.error = null
                }
                updateUploadButtonState()
            }

            etMusicTitle.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    // Trim leading and trailing whitespace when focus is lost
                    etMusicTitle.setText(etMusicTitle.text.toString().trim())
                }
            }

            btnUpload.setOnClickListener {
                handleUploadButtonClick()
            }

            llContainer.setOnTouchListener { v, event ->
                if (v !is EditText) {
                    etMusicTitle.clearFocus()
                }
                return@setOnTouchListener true
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

    private fun openGallery() {
        UwMediaPicker
            .with(this@UploadAudioFragment)
            .setGalleryMode(UwMediaPicker.GalleryMode.ImageGallery)
            .setGridColumnCount(3)
            .setMaxSelectableMediaCount(1)
            .setLightStatusBar(false)
            .setCancelCallback { }
            .launch { mediaList ->
                if (mediaList.isNullOrEmpty()) {
                    return@launch
                }
                handleSelectedImage(mediaList[0].mediaPath)
            }
    }

    private fun handleSelectedImage(imagePath: String) {
        if (imagePath.isBlank() || File(imagePath).exists().not()) {
            context?.toast(getString(R.string.error_in_selected_image))
            return
        }
        coverImagePath = imagePath
        val imageFile = File(coverImagePath)
        startImageCropper(imageUri = Uri.fromFile(imageFile))
    }

    private fun startImageCropper(imageUri: Uri) {
        cropImage.launch(
            CropImageContractOptions(
                uri = imageUri,
                cropImageOptions = com.canhub.cropper.CropImageOptions().apply {
                    activityBackgroundColor =
                        ContextCompat.getColor(requireContext(), R.color.backgroundColor)
                    aspectRatioX = 1
                    aspectRatioY = 1
                    fixAspectRatio = true
                },
            ),
        )
    }

    private fun handleCroppedImage(croppedImagePath: String?) {
        if (croppedImagePath.isNullOrBlank() || File(croppedImagePath).exists().not()) {
            context?.toast(getString(R.string.could_not_find_cropped_image_file))
            return
        }
        coverImagePath = croppedImagePath
        val croppedImageUri = Uri.fromFile(File(coverImagePath))
        binding.imgCustomCover.setImageURI(croppedImageUri)
        binding.audioIcon.setImageURI(croppedImageUri)
        updateUploadButtonState()
    }

    private fun updateUploadButtonState() {
        val shouldEnabled = shouldEnableUploadButton()
        binding.btnUpload.backgroundTintList = ContextCompat.getColorStateList(
            requireContext(),
            if (shouldEnabled) R.color.colorAccent else R.color.disabledColor
        )
    }

    private fun shouldEnableUploadButton(): Boolean {
        val musicTitle = binding.tvAudioTitle.text.toString().trim()
        val isMusicTitleValid = MusicTitleValidator.isValid(musicTitle) is MusicTitleValidationState.Valid
        return coverImagePath.isNotBlank() && coverImagePath.isNotEmpty()
                && isMusicTitleValid
    }

    private fun handleUploadButtonClick() {
        binding.etMusicTitle.clearFocus()
        val musicTitle = binding.tvAudioTitle.text.toString().trim()
        val isMusicTitleValid = MusicTitleValidator.isValid(musicTitle) is MusicTitleValidationState.Valid
        if (isMusicTitleValid.not()) {
            context?.toast(getString(R.string.please_enter_valid_music_title))
            return
        }
        val validationState = UploadMusicValidator.isValid(musicTitle, coverImagePath)
        if (validationState is UploadMusicValidationState.Invalid) {
            toast(getString(validationState.errorMsgResId))
            return
        }
        // TODO: add uploading logic
        toast("Upload Music Allowed")
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

    private fun pausePlayer() = activity?.runOnUiThread {
        exoPlayerAudio?.pause()
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

    override fun onPause() {
        super.onPause()
        pausePlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
    }

}