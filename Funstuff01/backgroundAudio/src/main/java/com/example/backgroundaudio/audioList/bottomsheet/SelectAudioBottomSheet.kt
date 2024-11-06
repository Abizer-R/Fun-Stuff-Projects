package com.example.backgroundaudio.audioList.bottomsheet

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.commitNow
import com.example.backgroundaudio.audioList.fragment.SelectAudioFragment
import com.example.backgroundaudio.audioList.fragment.SelectAudioFragment.Companion.ALLOW_AUDIO_FROM_DEVICE_STORAGE
import com.example.backgroundaudio.databinding.BottomSheetSelectAudioBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SelectAudioBottomSheet : BottomSheetDialogFragment(),
    SelectAudioFragment.SelectAudioFragmentCallbackListener
{

    companion object {

        fun newInstance(
            allowAudioFromDeviceStorage: Boolean = true
        ) = SelectAudioBottomSheet().apply {
            arguments = bundleOf(
                ALLOW_AUDIO_FROM_DEVICE_STORAGE to allowAudioFromDeviceStorage
            )
        }
    }

    private var _binding: BottomSheetSelectAudioBinding? = null
    private val binding: BottomSheetSelectAudioBinding
        get() = requireNotNull(_binding)

    private var listener: SelectAudioBSListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetSelectAudioBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpBottomSheet()
        attachFragment()

    }

    private fun setUpBottomSheet() {
        val dialog = dialog as BottomSheetDialog
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        dialog.behavior.isDraggable = false
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(false)
    }

    private fun attachFragment() {
        val allowAudioFromDeviceStorage = arguments?.getBoolean(ALLOW_AUDIO_FROM_DEVICE_STORAGE) ?: false
        childFragmentManager.commitNow {
            replace(
                binding.fragmentContainer.id,
                SelectAudioFragment.newInstance(allowAudioFromDeviceStorage)
            )
        }
    }

    override fun onAudioSelected() {
        listener?.onAudioSelected()
        dismissAllowingStateLoss()
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
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface SelectAudioBSListener {
        fun onAudioSelected()
    }
}