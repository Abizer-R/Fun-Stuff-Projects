package com.example.funstuff01.screensThree


import android.view.View
import androidx.navigation.fragment.findNavController
import com.example.funstuff01.R
import com.example.funstuff01.base.BaseFragment
import com.example.funstuff01.databinding.FragmentThreeParentBinding

class ThreeParentFragment : BaseFragment<FragmentThreeParentBinding>(
    FragmentThreeParentBinding::inflate
) {

    override fun initUserInterface(view: View?) {

        with(binding) {
            btnGotoChild.setOnClickListener {
                findNavController().navigate(
                    R.id.action_threeParentFragment_to_threeChildFragment
                )
            }
        }
    }

}