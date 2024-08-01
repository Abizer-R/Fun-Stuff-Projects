package com.example.funstuff01.ui.navScreens.screensOne


import android.view.View
import androidx.navigation.fragment.findNavController
import com.example.funstuff01.R
import com.example.funstuff01.ui.base.BaseFragment
import com.example.funstuff01.databinding.FragmentOneParentBinding
import com.example.funstuff01.ui.textToImage.TextToImageActivity
import com.example.funstuff01.utils.file.GeneratePdfUtils

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

//                val text = "Hello, this is a sample test"
//                GeneratePdfUtils.generatePdf(requireContext(), text)
                GeneratePdfUtils.generatePdf3(
                    requireContext(),
                    userName = "Abizer Rampurawala",
                    shareCount = 50,
                    chatWiseUserId = "abizer_r",
                    userPhoneNumber = "+919755388971"

                )

//                TextToImageActivity.startActivity(requireContext())
            }
        }
    }

}