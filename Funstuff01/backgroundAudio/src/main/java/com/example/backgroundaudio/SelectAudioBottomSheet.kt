package com.example.backgroundaudio

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.backgroundaudio.databinding.SelectAudioBottomSheetBinding
import com.example.backgroundaudio.utils.AudioListNotifier
import com.example.backgroundaudio.utils.AudioProcessorUtils
import com.example.backgroundaudio.utils.toast
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SelectAudioBottomSheet : BottomSheetDialogFragment(),
    AudioAdapter.AudioClickListener {

    companion object {

        const val UPDATE_PLAYING_STATE = "UPDATE_PLAYING_STATE"
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
    private var playerHandler: PlayerHandler? = null

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            super.onPlaybackStateChanged(playbackState)
            when (playbackState) {
                Player.STATE_BUFFERING -> {
                    sharedAudioViewModel.updateLoadingState(true)
                }

                Player.STATE_IDLE -> {
                    if (sharedAudioViewModel.currentPlaying != -1) {
                        sharedAudioViewModel.resetCurrentItemState()
                    }
                }

                else -> {
                    sharedAudioViewModel.updatePlayingState(playerHandler?.isPlaying)
                    sharedAudioViewModel.updateLoadingState(false)
                }
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            super.onPlayerError(error)
            sharedAudioViewModel.resetItemsUI()
            toast(getString(R.string.error_while_playing))
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
        dialog.behavior.isDraggable = false
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(false)

        playerHandler = PlayerHandler()
        playerHandler?.initialize(requireContext(), playerListener)

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
        with(sharedAudioViewModel) {
            lifecycleScope.launch {
                _audioListNotifier.consumeEach { listNotifier ->
                    audioAdapter.submitList(sharedAudioViewModel.audioList)
                    when (listNotifier) {
                        is AudioListNotifier.NotifyItemChanged -> {
                            audioAdapter.notifyItemChanged(
                                listNotifier.position,
                                listNotifier.payload
                            )
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

    private val audioPicker = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        val intentData = it.data
        if (it.resultCode == Activity.RESULT_OK && intentData != null) {

            val audioUri = intentData.data
            if (audioUri == null) {
                toast(getString(R.string.file_not_found))
                return@registerForActivityResult
            }
            val contentRes = requireContext().contentResolver
            val audioName = AudioProcessorUtils.getAudioName(audioUri, contentRes)
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
        sharedAudioViewModel.audioPickerLaunched = true
        audioPicker.launch(audioPickerIntent)
    }

    private fun handleSelectedAudioFromStorage(
        uri: Uri,
        name: String,
        sizeInMb: Float,
        durationInSeconds: Long
    ) {
        if (sizeInMb > 2.0) {
            toast(getString(R.string.audio_size_must_not_be_more_than_2_mb))
            return
        }
        if (durationInSeconds > 120) {
            toast(getString(R.string.audio_duration_must_not_be_more_than_2_minutes))
            return
        }

        val dialogBuilder =
            MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme).apply {
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
        if (sharedAudioViewModel.currentPlaying == position) {
            if (playerHandler?.playbackState == Player.STATE_BUFFERING) return
            if (playerHandler?.isPlaying == true) pauseAt(position)
            else playAt(position)
            return
        }

        // clicked item is different than current playing item
        // stop the prev item
        sharedAudioViewModel.resetCurrentItemState()
        playerHandler?.stop()
        // set the new item
        sharedAudioViewModel.currentPlaying = position
        playerHandler?.initMediaItem(audioItem)
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
        playerHandler?.pause()
        sharedAudioViewModel.updatePlayingState(false, position)
    }

    private fun playAt(position: Int) {
        if (position < 0 || position >= sharedAudioViewModel.audioList.size) {
            return
        }
        sharedAudioViewModel.updatePlayingState(true, position)
        sharedAudioViewModel.currentPlaying = position
    }

    private fun updateSelection(position: Int, audioItem: AudioItemModel) {
        if (sharedAudioViewModel.currentPlaying == 0 && sharedAudioViewModel.allowAudioFromDeviceStorage) {
            sharedAudioViewModel.currentPlaying = -1
            pauseAt(0)
        }
        sharedAudioViewModel.updateSelectedAudio(position)
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

        sharedAudioViewModel.resetCurrentItemState()
        listener = null
    }

    override fun onPause() {
        super.onPause()
        if (playerHandler?.isPlaying == true) {
            sharedAudioViewModel.shouldPlayOnResume = true
            pauseAt(sharedAudioViewModel.currentPlaying)
        }
    }

    override fun onResume() {
        super.onResume()
        if (sharedAudioViewModel.shouldPlayOnResume && sharedAudioViewModel.audioPickerLaunched.not()) {
            playAt(sharedAudioViewModel.currentPlaying)
        }
        sharedAudioViewModel.shouldPlayOnResume = false
        sharedAudioViewModel.audioPickerLaunched = false
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        releasePlayer()
    }

    private fun releasePlayer() {
        sharedAudioViewModel.currentPlaying = -1
        playerHandler?.release()
    }

    interface SelectAudioBSListener {
        fun onAudioSelected()
    }
}