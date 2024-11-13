package com.example.backgroundaudio.uploadAudio.bottomsheet

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.commitNow
import com.example.backgroundaudio.audioList.fragment.parent.AudioListParentFragment
import com.example.backgroundaudio.databinding.BottomSheetPickAudioFileBinding
import com.example.backgroundaudio.uploadAudio.fragment.PickAudioFragment
import com.example.backgroundaudio.uploadAudio.fragment.PickAudioFragment.PickAudioFragmentCallback
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PickAudioFileBottomSheet : BottomSheetDialogFragment(),
    PickAudioFragmentCallback
{

    companion object {

        fun newInstance() = PickAudioFileBottomSheet().apply {
            arguments = bundleOf()
        }
    }

    private var _binding: BottomSheetPickAudioFileBinding? = null
    private val binding: BottomSheetPickAudioFileBinding
        get() = requireNotNull(_binding)

    private var listener: PickAudioFragmentCallback? = null

    private val backPressCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            dismissAllowingStateLoss()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetPickAudioFileBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, backPressCallback)
        setUpBottomSheet()
        attachFragment()
    }

    private fun setUpBottomSheet() {
        val dialog = dialog as BottomSheetDialog
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        dialog.behavior.isDraggable = true
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
    }

    private fun attachFragment() {
        childFragmentManager.commitNow {
            replace(
                binding.fragmentContainer.id,
                PickAudioFragment.newInstance()
            )
        }
    }

    override fun onAudioFileSelected(audioPath: String, audioName: String) {
        listener?.onAudioFileSelected(audioPath, audioName)
        dismissAllowingStateLoss()
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
}