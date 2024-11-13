package com.example.backgroundaudio.audioList.fragment.parent

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commitNow
import androidx.lifecycle.lifecycleScope
import com.example.backgroundaudio.R
import com.example.backgroundaudio.audioList.SharedAudioViewModel
import com.example.backgroundaudio.audioList.bottomsheet.SelectAudioBottomSheet
import com.example.backgroundaudio.audioList.fragment.child.AudioListFragment
import com.example.backgroundaudio.databinding.FragmentAudioListParentBinding
import com.example.backgroundaudio.uploadAudio.bottomsheet.PickAudioFileBottomSheet
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AudioListParentFragment : Fragment() {

    companion object {

        fun newInstance() = AudioListParentFragment().apply {
            arguments = bundleOf()
        }
    }

    private var _binding: FragmentAudioListParentBinding? = null
    private val binding: FragmentAudioListParentBinding
        get() = requireNotNull(_binding)

    private val sharedAudioViewModel: SharedAudioViewModel by activityViewModels()

    private var tabsAdapter: AudioTabsAdapter? = null
    private var listener: SelectAudioCallbackListener? = null
    private val searchQueryFlow = MutableSharedFlow<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAudioListParentBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()
        setObservers()
    }

    private fun setupViews() {
        with(binding) {
            setupToolbar()
            setupViewPager()
            icDone.setOnClickListener {
                listener?.onAudioSelected()
            }

            icUploadMusic.setOnClickListener {
                showPickAudioBS()
            }

            searchEditText.doOnTextChanged { text, start, before, count ->
                lifecycleScope.launch {
                    searchQueryFlow.emit(text.toString())
                }
            }
        }
    }

    private fun setObservers() {
        lifecycleScope.launch {
            searchQueryFlow.debounce(1000L).collectLatest { searchQuery ->
                sharedAudioViewModel.updateSearchQuery(searchQuery)
            }
        }
    }

    private fun setupToolbar() {
        with(binding.toolbar) {
            navigationIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_arrow_back)
            navigationIcon?.setTint(ContextCompat.getColor(requireContext(), R.color.colorOnBackground))
            setNavigationOnClickListener {
                activity?.onBackPressedDispatcher?.onBackPressed()
            }
        }
    }

    private fun setupViewPager() {

        val fragmentList = arrayListOf<Fragment>()
        tabsAdapter = AudioTabsAdapter(fragmentList, childFragmentManager, lifecycle)
        binding.viewPager.adapter = tabsAdapter

        fragmentList.add(AudioListFragment.newInstance())
        fragmentList.add(AudioListFragment.newInstance())
        tabsAdapter?.notifyItemRangeInserted(0, fragmentList.size)
        binding.viewPager.offscreenPageLimit = fragmentList.size

        TabLayoutMediator(
            binding.tabLayout, binding.viewPager
        ) { tab: TabLayout.Tab, position: Int ->
            val tabName = when (position) {
                0 ->    getString(R.string.for_you)
                else -> getString(R.string.your_music)
            }
            tab.text = tabName
        }.attach()
    }

    private fun showPickAudioBS() {
        val pickAudioBottomSheet = PickAudioFileBottomSheet.newInstance()
        pickAudioBottomSheet.show(
            childFragmentManager,
            SelectAudioBottomSheet::class.java.simpleName
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val parentFragment = parentFragment
        val activity = activity
        if (parentFragment is SelectAudioCallbackListener) {
            listener = parentFragment
        } else if (activity is SelectAudioCallbackListener) {
            listener = activity
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface SelectAudioCallbackListener {
        fun onAudioSelected()
    }
}