package com.example.backgroundaudio.uploadAudio.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.backgroundaudio.R
import com.example.backgroundaudio.databinding.FragmentPickAudioBinding
import com.example.backgroundaudio.utils.AudioProcessorUtils
import com.example.backgroundaudio.utils.saveMediaToFile
import com.example.backgroundaudio.utils.toast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@AndroidEntryPoint
class PickAudioFragment : Fragment() {

    companion object {

        fun newInstance() = PickAudioFragment().apply {
            arguments = bundleOf()
        }
    }

    private var _binding: FragmentPickAudioBinding? = null
    private val binding: FragmentPickAudioBinding
        get() = requireNotNull(_binding)

    private var listener: PickAudioFragmentCallback? = null
    private lateinit var audioPicker: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPickAudioBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initLaunchers()
        setupViews()
    }

    private fun initLaunchers() {
        audioPicker = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            val intentData = it.data
            if (it.resultCode == Activity.RESULT_OK && intentData != null) {
                handleSelectedAudioFromStorage(intentData.data)
            }
        }
    }

    private fun setupViews() {
        with(binding) {

            listOf(layoutPickMusic, btnBrowseMusicFile).forEach { view ->
                view.setOnClickListener {
                    launchAudioPicker()
                }
            }
        }
    }

    private fun launchAudioPicker() {
        val audioPickerIntent = Intent().apply {
            action = Intent.ACTION_OPEN_DOCUMENT
            addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            type = "audio/*"
        }
        audioPicker.launch(audioPickerIntent)
    }

    private fun handleSelectedAudioFromStorage(audioUri: Uri?) {
        if (audioUri == null) {
            toast(getString(R.string.no_audio_selected))
            return
        }
        val contentRes = requireContext().contentResolver
        val audioNameWithExtension = AudioProcessorUtils.getAudioNameWithExtension(audioUri, contentRes)
        val audioName = audioNameWithExtension.substringBefore('.')
        val audioSizeInBytes = AudioProcessorUtils.getAudioSize(audioUri, contentRes)
        val sizeInMb = audioSizeInBytes.toFloat() / (1000 * 1000)
        val audioDuration = AudioProcessorUtils.getDurationMillis(requireContext(), audioUri)
        val durationInSeconds = audioDuration / 1000


        if (sizeInMb > 2.0) {
            Toast.makeText(requireContext(), R.string.audio_size_must_not_be_more_than_2_mb, Toast.LENGTH_SHORT).show()
            return
        }
        if (durationInSeconds > 120) {
            Toast.makeText(requireContext(), R.string.audio_duration_must_not_be_more_than_2_minutes, Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val audioPath = context?.saveMediaToFile(
                uri = audioUri,
                presetFileName = audioName
            ) ?: ""
            withContext(Dispatchers.Main) {
                if (audioPath.isBlank() || !File(audioPath).exists()) {
                    context?.toast(getString(R.string.error_while_processing_file))
                } else {
                    listener?.onAudioFileSelected(
                        audioPath = audioPath,
                        audioName = audioName
                    )
                }
            }

        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val parentFragment = parentFragment
        val activity = activity
        if (parentFragment is PickAudioFragmentCallback) {
            listener = parentFragment
        } else if (activity is PickAudioFragmentCallback) {
            listener = activity
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface PickAudioFragmentCallback {
        fun onAudioFileSelected(audioPath: String, audioName: String)
    }
}