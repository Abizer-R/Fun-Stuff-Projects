package com.example.backgroundaudio.audioList.bottomsheet

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.commitNow
import com.example.backgroundaudio.audioList.fragment.parent.AudioListParentFragment
import com.example.backgroundaudio.audioList.fragment.parent.AudioListParentFragment.SelectAudioCallbackListener
import com.example.backgroundaudio.databinding.BottomSheetSelectAudioBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SelectAudioBottomSheet : BottomSheetDialogFragment(),
    SelectAudioCallbackListener
{

    companion object {

        fun newInstance() = SelectAudioBottomSheet().apply {
            arguments = bundleOf()
        }
    }

    private var _binding: BottomSheetSelectAudioBinding? = null
    private val binding: BottomSheetSelectAudioBinding
        get() = requireNotNull(_binding)

    private var listener: SelectAudioBSListener? = null

    private val backPressCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            dismissAllowingStateLoss()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetSelectAudioBinding.inflate(layoutInflater)
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
        dialog.behavior.isDraggable = false
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(false)
    }

    private fun attachFragment() {
        childFragmentManager.commitNow {
            replace(
                binding.fragmentContainer.id,
                AudioListParentFragment.newInstance()
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