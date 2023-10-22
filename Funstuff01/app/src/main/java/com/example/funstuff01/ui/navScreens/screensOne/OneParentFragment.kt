package com.example.funstuff01.ui.navScreens.screensOne


import android.view.View
import androidx.navigation.fragment.findNavController
import com.example.funstuff01.R
import com.example.funstuff01.ui.base.BaseFragment
import com.example.funstuff01.databinding.FragmentOneParentBinding
import com.example.funstuff01.ui.textToImage.TextToImageActivity

class OneParentFragment : BaseFragment<FragmentOneParentBinding>(
    FragmentOneParentBinding::inflate
) {

    override fun initUserInterface(view: View?) {

        with(binding) {
            btnGotoChild.setOnClickListener {
                findNavController().navigate(
                    R.id.action_oneParentFragment_to_oneChildFragment
                )
            }

            btnTextToImage.setOnClickListener {
                TextToImageActivity.startActivity(requireContext())
            }
        }
    }

}