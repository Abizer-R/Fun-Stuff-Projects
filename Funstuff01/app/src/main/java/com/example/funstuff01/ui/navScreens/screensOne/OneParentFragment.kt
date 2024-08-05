package com.example.funstuff01.ui.navScreens.screensOne


import android.view.View
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import com.example.funstuff01.R
import com.example.funstuff01.ui.base.BaseFragment
import com.example.funstuff01.databinding.FragmentOneParentBinding
import com.example.funstuff01.ui.textToImage.TextToImageActivity
import com.example.funstuff01.utils.BitmapUtils
import com.example.funstuff01.utils.file.FileUtils
import com.example.funstuff01.utils.file.GeneratePdfUtils
import com.example.funstuff01.utils.file.saveMediaToFile
import com.example.funstuff01.utils.toast
import java.io.File

class OneParentFragment : BaseFragment<FragmentOneParentBinding>(
    FragmentOneParentBinding::inflate
) {
    private val photoPicker = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri == null) {
            context?.toast("photo picker failed")
        } else {
            val imagePath = context?.saveMediaToFile(uri) ?: ""
            compressAndSave(imagePath)
        }
    }

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

            btnGeneratePdf.setOnClickListener {
                GeneratePdfUtils.generatePdf3(
                    requireContext(),
                    userName = "Abizer Rampurawala",
                    shareCount = 50,
                    chatWiseUserId = "abizer_r",
                    userPhoneNumber = "+919755388971"
                )
            }

            btnCompressImage.setOnClickListener {
                photoPicker.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }
        }
    }

    private fun compressAndSave(imgPath: String) {
        val imageFile = File(imgPath)

        // Resize and compress the bitmap using WebP
        val resizedCompressedBitmapWebP = BitmapUtils.resizeAndCompressBitmapWebP(imageFile)
        resizedCompressedBitmapWebP?.let {
            // Save the bitmap to a file
            val outputFile = File(requireContext().filesDir, "compressed_image.webp")
            BitmapUtils.saveBitmapToFileWebP(it, outputFile, quality = 100)
            FileUtils.saveFileToAppFolder(
                requireContext(),
                outputFile,
                onSuccess = {
                    context?.toast("success")
                },
                onFailure = {
                    context?.toast("FAILED!!!")
                }
            )

        } ?: run {
            context?.toast("FAILED!!!")
        }


    }
}