package com.example.funstuff01.ui.navScreens.screensTwo


import android.view.View
import androidx.navigation.fragment.findNavController
import com.example.funstuff01.R
import com.example.funstuff01.ui.base.BaseFragment
import com.example.funstuff01.databinding.FragmentTwoParentBinding

class TwoParentFragment : BaseFragment<FragmentTwoParentBinding>(
    FragmentTwoParentBinding::inflate
) {

    override fun initUserInterface(view: View?) {

        with(binding) {
            btnGotoChild.setOnClickListener {
                findNavController().navigate(
                    R.id.action_twoParentFragment_to_twoChildFragment
                )
            }
        }
    }

}