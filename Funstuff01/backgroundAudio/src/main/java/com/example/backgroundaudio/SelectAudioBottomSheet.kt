package com.example.backgroundaudio

import android.app.Activity
import android.app.Dialog
import android.content.ClipData
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Resources
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.backgroundaudio.databinding.SelectAudioBottomSheetBinding
import com.example.backgroundaudio.utils.AudioListNotifier
import com.example.backgroundaudio.utils.AudioProcessorUtils
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch

class SelectAudioBottomSheet : BottomSheetDialogFragment(),
    AudioAdapter.AudioClickListener {

    companion object {

        const val UPDATE_PLAY_PAUSE = "UPDATE_PLAY_PAUSE"
        const val UPDATE_LOADING_STATE = "UPDATE_LOADING_STATE"
        const val UPDATE_SELECTED_STATE = "UPDATE_SELECTED_STATE"
        const val AUDIO_LIST_EXTRA = "AUDIO_LIST_EXTRA"
        const val ALLOW_AUDIO_FROM_DEVICE_STORAGE = "ALLOW_AUDIO_FROM_DEVICE_STORAGE"

        fun newInstance(
            allowAudioFromDeviceStorage: Boolean = true
        ) = SelectAudioBottomSheet().apply {
            arguments = bundleOf(
                ALLOW_AUDIO_FROM_DEVICE_STORAGE to allowAudioFromDeviceStorage
            )
        }
    }

    private lateinit var dialog: BottomSheetDialog
    private lateinit var binding: SelectAudioBottomSheetBinding

    private val sharedAudioViewModel: SharedBackgroundAudioViewModel by activityViewModels()

    private var allowAudioFromDeviceStorage: Boolean = true

    private var listener: SelectAudioBSListener? = null
    private lateinit var audioAdapter: AudioAdapter
    private var exoPlayer: SimpleExoPlayer? = null

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            super.onPlaybackStateChanged(playbackState)

            if(sharedAudioViewModel.currentPlaying == -1) {
                return
            }
            when (playbackState) {
                Player.STATE_BUFFERING -> {
                    sharedAudioViewModel.updateProgress(isLoading = true)
                }

                Player.STATE_IDLE -> {
                    sharedAudioViewModel.updatePlayPause(isPlaying = false)
                    sharedAudioViewModel.updateProgress(isLoading = false)
                    sharedAudioViewModel.currentPlaying = -1
                    context?.let {
                        Toast.makeText(it, R.string.something_went_wrong_msg, Toast.LENGTH_SHORT).show()
                    }
                }

                else -> {
                    sharedAudioViewModel.updatePlayPause(isPlaying = exoPlayer!!.isPlaying)
                    sharedAudioViewModel.updateProgress(isLoading = false)
                }
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            super.onPlayerError(error)

            if(sharedAudioViewModel.currentPlaying < 0 || sharedAudioViewModel.currentPlaying >= sharedAudioViewModel.audioList.size) {
                return
            }
            sharedAudioViewModel.updateProgress(isLoading = false)
            exoPlayer?.let {
                it.prepare()
                it.play()
            }
        }
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = SelectAudioBottomSheetBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // setting up bottomSheet
        arguments?.getBoolean(ALLOW_AUDIO_FROM_DEVICE_STORAGE)?.let {
            allowAudioFromDeviceStorage = it
        }

        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
//        val expandedHeight = getDisplayHeight() - getStatusBarHeight()
//        val peekHeight = (expandedHeight * 0.75).toInt()
//        dialog.behavior.peekHeight = peekHeight
        dialog.behavior.isDraggable = false

        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(false)

        exoPlayer = SimpleExoPlayer.Builder(requireContext())
            .build().apply {
//                repeatMode = Player.REPEAT_MODE_ONE
                addListener(playerListener)
            }

        sharedAudioViewModel.resetItemsUI()
        setUpViews()
        setObservers()
    }

    private fun setUpViews() {
        with(binding) {
            audioAdapter = AudioAdapter(this@SelectAudioBottomSheet)
            rvAudios.adapter = audioAdapter

            icDone.setOnClickListener {
                listener?.onAudioSelected()
                dismissAllowingStateLoss()
            }
        }
        audioAdapter.submitList(sharedAudioViewModel.audioList)
    }

    @OptIn(ObsoleteCoroutinesApi::class)
    private fun setObservers() {
        with (sharedAudioViewModel) {
            lifecycleScope.launch {
                _audioListNotifier.consumeEach {
                    audioAdapter.submitList(sharedAudioViewModel.audioList)
                    when (it) {
                        is AudioListNotifier.NotifyItemChanged -> {
                            audioAdapter.notifyItemChanged(it.position, it.payload)
                        }
                        is AudioListNotifier.NotifyDataChanged -> {
                            audioAdapter.notifyDataSetChanged()
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    private fun getDisplayHeight(): Int = Resources.getSystem().displayMetrics.heightPixels

    private fun getStatusBarHeight(): Int {
        val rectangle = Rect()
        val window = activity?.window ?: return 0
        window.decorView.getWindowVisibleDisplayFrame(rectangle)
        return rectangle.top
    }

    private val audioPicker = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        val intentData = it.data
        if (it.resultCode == Activity.RESULT_OK && intentData != null) {

            val audioUri = intentData.data
            if (audioUri == null) {
                Toast.makeText(requireContext(), R.string.file_not_found, Toast.LENGTH_SHORT).show()
                return@registerForActivityResult
            }
            val contentRes = requireContext().contentResolver
            val audioName = AudioProcessorUtils.getAudioName(audioUri!!, contentRes)
            val audioSizeInBytes = AudioProcessorUtils.getAudioSize(audioUri, contentRes)
            val sizeInMb = audioSizeInBytes.toFloat() / (1000 * 1000)
            val audioDuration = AudioProcessorUtils.getDurationMillis(requireContext(), audioUri)
            val durationInSeconds = audioDuration / 1000

            handleSelectedAudioFromStorage(audioUri, audioName, sizeInMb, durationInSeconds)
        }
    }

    override fun onUploadFromStorage() {
        val audioPickerIntent = Intent().apply {
            action = Intent.ACTION_OPEN_DOCUMENT
            addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            type = "audio/*"
        }
        audioPicker.launch(audioPickerIntent)
    }

    private fun handleSelectedAudioFromStorage(
        uri: Uri,
        name: String,
        sizeInMb: Float,
        durationInSeconds: Long
    ) {
        if (sizeInMb > 2.0) {
            Toast.makeText(requireContext(), R.string.audio_size_must_not_be_more_than_2_mb, Toast.LENGTH_SHORT).show()
            return
        }
        if (durationInSeconds > 120) {
            Toast.makeText(requireContext(), R.string.audio_duration_must_not_be_more_than_2_minutes, Toast.LENGTH_SHORT).show()
            return
        }

        val dialogBuilder = MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme).apply {
            setIcon(R.drawable.ic_music_card)
            setTitle(name)
            setMessage(getString(R.string.select_audio_as_background))
            setCancelable(true)
        }

        dialogBuilder.setPositiveButton(getString(R.string.yes)) { dialogInterface, _ ->
            sharedAudioViewModel.updateAudioFromStorage(name, uri)
            dialogInterface?.dismiss()
        }

        dialogBuilder.setNegativeButton(getString(R.string.no)) { dialogInterface, _ ->
            dialogInterface.dismiss()
        }

        dialogBuilder.show()
    }

    override fun onPlayPauseClicked(position: Int, audioItem: AudioItemModel) {
        exoPlayer?.let { player ->

            if(sharedAudioViewModel.currentPlaying == position) {
                if(player.playbackState == Player.STATE_BUFFERING) {
                    return
                }
                if(player.isPlaying) {
                    pauseAt(position)
                } else {
                    playAt(position)
                }

            } else {
                if(player.playbackState == Player.STATE_BUFFERING) {
                    sharedAudioViewModel.updatePlayPause(isPlaying = false)
                    sharedAudioViewModel.updateProgress(isLoading = false)
                    // need to make "currentSelected" -1
                    // otherwise, the playerListener will show a misguiding toast when playbackState will be STATE_IDLE
                    sharedAudioViewModel.currentPlaying = -1
                    player.stop()

                } else if (player.isPlaying) {
                    sharedAudioViewModel.updatePlayPause(isPlaying = false)
                }
                sharedAudioViewModel.currentPlaying = position
                initMediaItem(audioItem)
            }
        }
    }

    override fun onAudioItemClicked(position: Int, audioItem: AudioItemModel) {
        if (position < 0 || position >= sharedAudioViewModel.audioList.size) {
            return
        }
        if (audioItem.isSelected) {
            updateSelection(position, audioItem)
        } else {
            MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme).apply {
                setIcon(R.drawable.ic_music_card)
                setTitle(audioItem.name)
                setMessage(getString(R.string.select_audio_as_background))
                setCancelable(true)
                setPositiveButton(getString(R.string.yes)) { dialogInterface, _ ->
                    updateSelection(position, audioItem)
                    dialogInterface?.dismiss()
                }
                setNegativeButton(getString(R.string.no)) { dialogInterface, _ ->
                    dialogInterface.dismiss()
                }
            }.show()
        }
    }

    private fun pauseAt(position: Int) {
        if (position < 0 || position >= sharedAudioViewModel.audioList.size) {
            return
        }
        exoPlayer?.let {
            it.pause()
            sharedAudioViewModel.updatePlayPause(position, false)
        }
    }

    private fun playAt(position: Int) {
        if (position < 0 || position >= sharedAudioViewModel.audioList.size) {
            return
        }
        exoPlayer?.let {
            it.play()
            sharedAudioViewModel.updatePlayPause(position, true)
            sharedAudioViewModel.currentPlaying = position
        }
    }

    private fun updateSelection(position: Int, audioItem: AudioItemModel) {
        if (sharedAudioViewModel.currentPlaying == 0 && sharedAudioViewModel.allowAudioFromDeviceStorage) {
            sharedAudioViewModel.currentPlaying = -1
            pauseAt(0)
        }
        sharedAudioViewModel.updateSelectedAudio(position)
    }

    private fun initMediaItem(audioItem: AudioItemModel) {
        exoPlayer?.let { player ->
            player.setMediaItem(AudioProcessorUtils.getMediaItem(audioItem))
            player.prepare()
            player.play()
        }
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)

        val parentFragment = parentFragment
        val activity = activity
        if (parentFragment is SelectAudioBSListener) {
            listener = parentFragment
        } else if (activity is SelectAudioBSListener) {
            listener = activity
        }

        // If there is an already selected audio, then bring it to the top
        if (sharedAudioViewModel.selectedAudioItem != null) {
            sharedAudioViewModel.bringSelectedItemToTop()
        }
    }

    override fun onDetach() {
        super.onDetach()

        listener = null
        sharedAudioViewModel.updatePlayPause(isPlaying = false)
        sharedAudioViewModel.updateProgress(isLoading = false)
    }

    override fun onPause() {
        super.onPause()
        if (exoPlayer?.isPlaying == true) {
            sharedAudioViewModel.shouldPlayOnResume = true
            pauseAt(sharedAudioViewModel.currentPlaying)
        }
    }

    override fun onResume() {
        super.onResume()
        if (sharedAudioViewModel.shouldPlayOnResume) {
            playAt(sharedAudioViewModel.currentPlaying)
            sharedAudioViewModel.shouldPlayOnResume = false
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        releasePlayer()
    }

    private fun releasePlayer() {
        sharedAudioViewModel.currentPlaying = -1
        exoPlayer?.let {
            if(it.isPlaying)
                it.stop()
            it.release()
        }
    }

    interface SelectAudioBSListener {
        fun onAudioSelected()
    }
}