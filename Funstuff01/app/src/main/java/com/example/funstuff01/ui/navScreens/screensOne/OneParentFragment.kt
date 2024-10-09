package com.example.funstuff01.ui.navScreens.screensOne


import android.graphics.Bitmap
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import com.example.funstuff01.R
import com.example.funstuff01.ui.base.BaseFragment
import com.example.funstuff01.databinding.FragmentOneParentBinding
import com.example.funstuff01.ui.textToImage.TextToImageActivity
import com.example.funstuff01.utils.BitmapUtils
import com.example.funstuff01.utils.NotificationUtil
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
                GeneratePdfUtils.generateShareCertificate(
                    context = requireContext(),
                    optionHolder = "Abizer Rampurawala",
                    shareCount = 50,
                    chatWiseId = "abizer34rdsf345",
                    registeredMobile = "+919755388971",
                    onSuccess = { fileUri ->
                        NotificationUtil.createDownloadCompleteNotification(
                            context = requireContext(),
                            title = getString(R.string.download_completed),
                            body = getString(R.string.click_to_open),
                            dataType = "application/pdf",
                            fileUri = fileUri
                        )
                        toast(getString(R.string.download_successful))
                    },
                    onFailure = {
                        toast(it ?: getString(R.string.download_failed))
                    }
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


//            val outputFileWebp = File(requireContext().filesDir, "compressed_image.webp")
//            BitmapUtils.saveBitmapToFileWebP(it, outputFileWebp, quality = 100)
//            FileUtils.saveFileToAppFolder(
//                requireContext(),
//                outputFileWebp,
//                onSuccess = { context?.toast("success") },
//                onFailure = { context?.toast("FAILED!!!") }
//            )
//
//            val outputFilePng = File(requireContext().filesDir, "compressed_image.png")
//            BitmapUtils.saveBitmapToFile(it, outputFilePng, quality = 100, Bitmap.CompressFormat.PNG)
//            FileUtils.saveFileToAppFolder(
//                requireContext(),
//                outputFilePng,
//                onSuccess = { context?.toast("success") },
//                onFailure = { context?.toast("FAILED!!!") }
//            )

            val outputFileJpg = File(requireContext().filesDir, "compressed_image.jpg")
            BitmapUtils.saveBitmapToFile(it, outputFileJpg, quality = 100, Bitmap.CompressFormat.JPEG)
            FileUtils.saveFileToAppFolder(
                requireContext(),
                outputFileJpg,
                onSuccess = { context?.toast("success") },
                onFailure = { context?.toast("FAILED!!!") }
            )

        } ?: run {
            context?.toast("FAILED!!!")
        }


    }
}