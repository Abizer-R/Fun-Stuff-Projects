package com.example.backgroundaudio.audioList.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.backgroundaudio.R
import com.example.backgroundaudio.audioList.PlayerHandler
import com.example.backgroundaudio.audioList.SharedBackgroundAudioViewModel
import com.example.backgroundaudio.audioList.adapter.AudioAdapter
import com.example.backgroundaudio.audioList.adapter.AudioItemModel
import com.example.backgroundaudio.audioList.bottomsheet.SelectAudioBottomSheet.SelectAudioBSListener
import com.example.backgroundaudio.databinding.FragmentSelectAudioBinding
import com.example.backgroundaudio.utils.AudioListNotifier
import com.example.backgroundaudio.utils.AudioProcessorUtils
import com.example.backgroundaudio.utils.toast
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class SelectAudioFragment: Fragment(),
    AudioAdapter.AudioClickListener
{


    companion object {

        const val ALLOW_AUDIO_FROM_DEVICE_STORAGE = "ALLOW_AUDIO_FROM_DEVICE_STORAGE"

        fun newInstance(
            allowAudioFromDeviceStorage: Boolean = true
        ) = SelectAudioFragment().apply {
            arguments = bundleOf(
                ALLOW_AUDIO_FROM_DEVICE_STORAGE to allowAudioFromDeviceStorage
            )
        }
    }

    private var _binding: FragmentSelectAudioBinding? = null
    private val binding get() = requireNotNull(_binding)
    private var listener: SelectAudioFragmentCallbackListener? = null

    private val sharedAudioViewModel: SharedBackgroundAudioViewModel by activityViewModels()

    private var allowAudioFromDeviceStorage: Boolean = true
    private lateinit var audioAdapter: AudioAdapter

    @Inject
    lateinit var playerHandler: PlayerHandler


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
                    sharedAudioViewModel.updatePlayingState(playerHandler.isPlaying)
                    sharedAudioViewModel.updateLoadingState(false)
                }
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            super.onPlayerError(error)
            sharedAudioViewModel.resetItems()
            toast(getString(R.string.error_while_playing))
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        allowAudioFromDeviceStorage = arguments?.getBoolean(ALLOW_AUDIO_FROM_DEVICE_STORAGE) ?: false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSelectAudioBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUserInterface()
    }

    private fun initUserInterface() {
        playerHandler.addPlayerListener(playerListener)
        sharedAudioViewModel.resetItems()

        // If there is an already selected audio, then bring it to the top
        if (sharedAudioViewModel.selectedAudioItem != null) {
            sharedAudioViewModel.bringSelectedItemToTop()
        }

        setUpViews()
        setObservers()
    }


    private fun setUpViews() {
        with(binding) {
            audioAdapter = AudioAdapter(this@SelectAudioFragment)
            rvAudios.adapter = audioAdapter

            icDone.setOnClickListener {
                listener?.onAudioSelected()
            }
        }
        audioAdapter.submitList(sharedAudioViewModel.audioList)
    }

    @OptIn(ObsoleteCoroutinesApi::class)
    private fun setObservers() {
        lifecycleScope.launch {
            sharedAudioViewModel._audioListNotifier.consumeEach { listNotifier ->
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

    private val audioPicker = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == Activity.RESULT_OK) {
            handleSelectedAudioFromStorage(it.data?.data)
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

    private fun handleSelectedAudioFromStorage(audioUri: Uri?) {
        if (audioUri == null) {
            toast(getString(R.string.file_not_found))
            return
        }

        val contentRes = requireContext().contentResolver
        val audioName = AudioProcessorUtils.getAudioName(audioUri, contentRes)
        val audioSizeInBytes = AudioProcessorUtils.getAudioSize(audioUri, contentRes)
        val sizeInMb = audioSizeInBytes.toFloat() / (1000 * 1000)
        val audioDuration = AudioProcessorUtils.getDurationMillis(requireContext(), audioUri)
        val durationInSeconds = audioDuration / 1000

        if (sizeInMb > 2.0) {
            toast(getString(R.string.audio_size_must_not_be_more_than_2_mb))
            return
        }
        if (durationInSeconds > 120) {
            toast(getString(R.string.audio_duration_must_not_be_more_than_2_minutes))
            return
        }

        MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
            .setIcon(R.drawable.ic_music_card)
            .setTitle(audioName)
            .setMessage(getString(R.string.select_audio_as_background))
            .setCancelable(true)
            .setPositiveButton(getString(R.string.yes)) { dialogInterface, _ ->
                sharedAudioViewModel.updateAudioFromStorage(audioName, audioUri)
                dialogInterface?.dismiss()
            }
            .setNegativeButton(getString(R.string.no)) { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .show()
    }

    override fun onPlayPauseClicked(position: Int, audioItem: AudioItemModel) {
        if (sharedAudioViewModel.currentPlaying == position) {
            if (playerHandler.playbackState == Player.STATE_BUFFERING) return
            if (playerHandler.isPlaying == true) pauseAt(position)
            else playAt(position)
            return
        }

        // clicked item is different than current playing item
        // stop the prev item
        sharedAudioViewModel.resetCurrentItemState()
        playerHandler.stop()
        // set the new item
        sharedAudioViewModel.currentPlaying = position
        playerHandler.initMediaItem(audioItem)
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
        playerHandler.pause()
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

    override fun onPause() {
        super.onPause()
        if (playerHandler.isPlaying) {
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

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val parentFragment = parentFragment
        val activity = activity
        if (parentFragment is SelectAudioFragmentCallbackListener) {
            listener = parentFragment
        } else if (activity is SelectAudioFragmentCallbackListener) {
            listener = activity
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        playerHandler.release()
        sharedAudioViewModel.resetCurrentItemState()
    }



    interface SelectAudioFragmentCallbackListener {
        fun onAudioSelected()
    }
}