package com.example.backgroundaudio.audioList.fragment

import android.app.Activity
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
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.backgroundaudio.R
import com.example.backgroundaudio.audioList.PlayerHandler
import com.example.backgroundaudio.audioList.SharedAudioViewModel
import com.example.backgroundaudio.audioList.adapter.AudioAdapter
import com.example.backgroundaudio.audioList.adapter.AudioAdapter.Companion.UPDATE_SELECTED_STATE
import com.example.backgroundaudio.audioList.adapter.AudioItemModel
import com.example.backgroundaudio.databinding.FragmentAudioListBinding
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
import javax.inject.Inject

@AndroidEntryPoint
class AudioListFragment: Fragment(),
    AudioAdapter.AudioClickListener
{

    companion object {

        fun newInstance() = AudioListFragment().apply {
            arguments = bundleOf()
        }
    }

    private var _binding: FragmentAudioListBinding? = null
    private val binding get() = requireNotNull(_binding)

    private val viewModel: AudioListViewModel by viewModels()
    private val sharedAudioViewModel: SharedAudioViewModel by activityViewModels()

    private lateinit var audioAdapter: AudioAdapter

    @Inject
    lateinit var playerHandler: PlayerHandler


    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            super.onPlaybackStateChanged(playbackState)
            viewModel.updatePlayingState(playerHandler.isPlaying)
            viewModel.updateLoadingState(playbackState == Player.STATE_BUFFERING)
        }

        override fun onPlayerError(error: PlaybackException) {
            super.onPlayerError(error)
            viewModel.resetCurrentItemState()
            toast(getString(R.string.error_while_playing))
        }

        override fun onPlayerErrorChanged(error: PlaybackException?) {
            super.onPlayerErrorChanged(error)
            viewModel.resetCurrentItemState()
            toast(getString(R.string.error_while_playing))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAudioListBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUserInterface()
    }

    private fun initUserInterface() {
        playerHandler.addPlayerListener(playerListener)

        setUpViews()
        setObservers()

        updateInitialSelectedItem()
        viewModel.fetchAudioList()
    }

    private fun updateInitialSelectedItem() {
        viewModel.initiallySelectedAudioItem = sharedAudioViewModel.selectedAudioItem
    }


    private fun setUpViews() {
        with(binding) {
            audioAdapter = AudioAdapter(this@AudioListFragment)
            rvAudios.adapter = audioAdapter


        }
    }

    @OptIn(ObsoleteCoroutinesApi::class)
    private fun setObservers() {
        lifecycleScope.launch {
            viewModel._audioListNotifier.consumeEach { listNotifier ->
                audioAdapter.submitList(viewModel.audioList)
                when (listNotifier) {
                    is AudioListNotifier.NotifyItemChanged -> {
                        audioAdapter.notifyItemChanged(
                            listNotifier.position,
                            listNotifier.payload
                        )
                        if (listNotifier.payload == UPDATE_SELECTED_STATE) {
                            sharedAudioViewModel.updateSelectedItem(viewModel.selectedAudioItem)
                        }
                    }

                    is AudioListNotifier.NotifyDataChanged -> {
                        audioAdapter.notifyDataSetChanged()
                    }

                    is AudioListNotifier.NotifyItemRangeAdded -> {
                        audioAdapter.notifyItemRangeInserted(
                            listNotifier.positionStart,
                            listNotifier.itemsAdded
                        )
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
        viewModel.audioPickerLaunched = true
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
                viewModel.updateAudioFromStorage(audioName, audioUri)
                dialogInterface?.dismiss()
            }
            .setNegativeButton(getString(R.string.no)) { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .show()
    }

    override fun onPlayPauseClicked(position: Int, audioItem: AudioItemModel) {
        if (viewModel.currentPlaying == position) {
            if (playerHandler.playbackState == Player.STATE_BUFFERING) return
            if (playerHandler.isPlaying) pause()
            else play()
            return
        }

        // clicked item is different than current playing item.
        stop()  // First stop the prev item
        initPosition(position, audioItem)  // Now set the new item
    }

    override fun onAudioItemClicked(position: Int, audioItem: AudioItemModel) {
        if (position < 0 || position >= viewModel.audioList.size) {
            return
        }
        if (audioItem.isSelected) {
            unSelectItem()
        } else {
            MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme).apply {
                setIcon(R.drawable.ic_music_card)
                setTitle(audioItem.name)
                setMessage(getString(R.string.select_audio_as_background))
                setCancelable(true)
                setPositiveButton(getString(R.string.yes)) { dialogInterface, _ ->
                    selectItem(position)
                    dialogInterface?.dismiss()
                }
                setNegativeButton(getString(R.string.no)) { dialogInterface, _ ->
                    dialogInterface.dismiss()
                }
            }.show()
        }
    }

    private fun initPosition(position: Int, audioItem: AudioItemModel) {
        viewModel.currentPlaying = position
        playerHandler.initMediaItem(audioItem)
    }

    private fun stop() {
        playerHandler.stop()
        viewModel.resetCurrentItemState()
    }

    private fun pause() {
        playerHandler.pause()
        viewModel.updatePlayingState(false)
    }

    private fun play() {
        playerHandler.play()
        viewModel.updatePlayingState(true)
    }

    private fun selectItem(position: Int) {
        viewModel.selectItem(position)
    }

    private fun unSelectItem() {
        viewModel.unSelectItem()
    }

    override fun onPause() {
        super.onPause()
        if (playerHandler.isPlaying) {
            viewModel.shouldPlayOnResume = true
            pause()
        }
    }

    override fun onResume() {
        super.onResume()
        if (viewModel.shouldPlayOnResume && viewModel.audioPickerLaunched.not()) {
            play()
        }
        viewModel.shouldPlayOnResume = false
        viewModel.audioPickerLaunched = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        playerHandler.release()
        viewModel.resetCurrentItemState()
    }

}