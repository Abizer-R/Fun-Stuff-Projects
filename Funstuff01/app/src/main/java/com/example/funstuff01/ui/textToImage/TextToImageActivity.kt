package com.example.funstuff01.ui.textToImage

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.backgroundaudio.AudioItemModel
import com.example.backgroundaudio.AudioItemType
import com.example.backgroundaudio.SelectAudioBottomSheet
import com.example.backgroundaudio.SharedBackgroundAudioViewModel
import com.example.backgroundaudio.utils.DummyAudioList
import com.example.backgroundaudio.utils.AudioProcessorUtils
import com.example.funstuff01.R
import com.example.funstuff01.ui.base.BaseActivity
import com.example.funstuff01.databinding.ActivityTextToImageBinding
import com.example.funstuff01.ui.mediaPreview.MediaPreviewActivity
import com.example.funstuff01.ui.textToImage.adapter.ColorsAdapter
import com.example.funstuff01.utils.AppUtils
import com.example.funstuff01.utils.AppUtils.getPathForStorage
import com.example.funstuff01.utils.TextToImageUtils
import com.example.funstuff01.utils.file.saveMediaToFile
import com.example.funstuff01.utils.setOnSingleClickListener
import com.example.funstuff01.utils.toast
import com.example.funstuff01.utils.views.CustomProgressDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TextToImageActivity : BaseActivity<ActivityTextToImageBinding>(ActivityTextToImageBinding::inflate),
        ColorsAdapter.ColorsClickListener, SeekBar.OnSeekBarChangeListener,
    SelectAudioBottomSheet.SelectAudioBSListener
{

    private val mainBgList = mutableListOf<Int>()
    private val btnBgList = mutableListOf<Int>()
    private var currentBg = 0
    private var isSaving: Boolean = false

    private var colorsAdapter: ColorsAdapter? = null
    private var customProgressDialog: CustomProgressDialog? = null

    private val sharedAudioViewModel: SharedBackgroundAudioViewModel by viewModels()

    override fun initUserInterface() {

        presetAudioList()

        with(binding) {

            colorsAdapter = ColorsAdapter(this@TextToImageActivity)
            rvColors.apply {
                layoutManager = LinearLayoutManager(this@TextToImageActivity, RecyclerView.HORIZONTAL, false)
                adapter = colorsAdapter
            }

            etText.setText("Default Dummy Text")

            seekbar.setOnSeekBarChangeListener(this@TextToImageActivity)

            etText.setOnFocusChangeListener { _, hasFocus ->
                handleEditTextFocusChanged(hasFocus)
            }

            btnDone.setOnClickListener {
                clearFocusAndHideKeyboard()
            }

            btnChangeBg.setOnClickListener {
                currentBg = (currentBg + 1) % mainBgList.size
                clRoot.background = ContextCompat.getDrawable(this@TextToImageActivity, mainBgList[currentBg])
                btnChangeBg.background = ContextCompat.getDrawable(this@TextToImageActivity, btnBgList[currentBg])
            }

            btnPost.setOnClickListener {
                handlePostClicked()
            }

            btnBack.setOnClickListener {
                onBackPressed()
            }

            cvAddMusic.setOnSingleClickListener {
                showSelectAudioBS()
            }
        }

        colorsAdapter?.submitList(TextToImageUtils.getColorResourceList())
        mainBgList.addAll(TextToImageUtils.getMainBgList())
        btnBgList.addAll(TextToImageUtils.getBtnBgList())
    }

    private fun presetAudioList() {
        sharedAudioViewModel.setAudioList(DummyAudioList.musicList)
    }

    private fun handlePostClicked() {
        sharedAudioViewModel.selectedAudioItem?.let {
            addAudioToImageAndPost(it)
            return
        }

        saveTextAsImage { imagePath ->
            Log.e("TESTING2", "outputImage Path = $imagePath", )
            showPreview(imagePath, true)
        }
    }

    private fun showPreview(mediaPath: String, isImage: Boolean) {
        MediaPreviewActivity.startActivity(
            this@TextToImageActivity, mediaPath = mediaPath, isImage = isImage
        )
    }

    private fun addAudioToImageAndPost(audioItem: AudioItemModel) {
        saveTextAsImage { imagePath ->
            showProgressDialog()
            lifecycleScope.launch{
                if (audioItem.itemType == AudioItemType.AUDIO_FILE) {
                    val audioPath =
                        AppUtils.getPathForStorage(this@TextToImageActivity, "post_audio.mp3")
                    AppUtils.downloadFile(
                        audioItem.url,
                        audioPath,
                        onComplete = {
                            convertAudioAndCombine(audioPath, imagePath)
                        },
                        onFailed = {
                            hideProgressDialog()
                            showErrorToast(getString(R.string.something_went_wrong))
                        }
                    )
                } else if (audioItem.storageUri != null) {
                    val audioPath = this@TextToImageActivity.saveMediaToFile(audioItem.storageUri!!, "post_audio.mp3")
                    convertAudioAndCombine(audioPath, imagePath)
                }
            }

        }
    }

    private fun convertAudioAndCombine(audioFilePath: String, imagePath: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val aacAudioPath =
                AppUtils.getPathForStorage(this@TextToImageActivity, "post_audio.aac")

            val isSuccess = AudioProcessorUtils.convertAudioToAAC(
                audioFilePath, aacAudioPath
            )

            if(isSuccess.not()) {
                hideProgressDialog()
                showErrorToast(getString(R.string.something_went_wrong))
            } else {
                combineAudioAndVideo(aacAudioPath, imagePath)
            }

        }
    }

    private fun combineAudioAndVideo(audioFilePath: String, imagePath: String) {
        showProgressDialog()
        lifecycleScope.launch {
            val outputVideoPath = AppUtils.getPathForStorage(
                this@TextToImageActivity,
                "video_" + System.currentTimeMillis() + ".mp4"
            )
            val imageDetails = AudioProcessorUtils.getImageDetails(imagePath)
            val imageWidth = imageDetails.first
            val imageHeight = imageDetails.second
            if(imageWidth == -1 || imageHeight == -1) {
                hideProgressDialog()
                Toast.makeText(this@TextToImageActivity, "invalid image", Toast.LENGTH_SHORT).show()
                return@launch

            }

            val isSuccess = AudioProcessorUtils.combineAudioAndImage(
                audioPath = audioFilePath,
                imagePath = imagePath,
                imageWidth = imageWidth,
                imageHeight = imageHeight,
                outputFilePath = outputVideoPath
            )

            withContext(Dispatchers.Main) {
                hideProgressDialog()
                if(isSuccess.not()) {
                    showErrorToast(getString(R.string.something_went_wrong))
                } else {
                    showPreview(outputVideoPath, false)
                }
            }
        }
    }


    private fun handleEditTextFocusChanged(hasFocus: Boolean) {
        with(binding) {
            if (isSaving.not()) {
                btnDone.isVisible = hasFocus
                seekbar.isVisible = hasFocus
                bgFade.isVisible = hasFocus
                flTextColor.isVisible = hasFocus
                rvColors.isVisible = hasFocus
                btnPost.isVisible = hasFocus.not()
                btnBack.isVisible = hasFocus.not()
                btnChangeBg.isVisible = hasFocus.not()
            }
        }
    }

    private fun clearFocusAndHideKeyboard() {
        val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        if (currentFocus is EditText) {
            imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
            binding.etText.clearFocus()
        }
    }

    override fun onItemClick(@ColorRes colorRes: Int) {
        with(binding) {
            etText.setTextColor(
                    ContextCompat.getColor(this@TextToImageActivity, colorRes)
            )
            tvColor.setTextColor(
                    ContextCompat.getColor(this@TextToImageActivity, colorRes)
            )
        }
    }

    private fun showSelectAudioBS() {
        if(binding.etText.text.isNullOrBlank()) {
            this@TextToImageActivity.toast(getString(R.string.please_write_something))
            return
        }
        if (!AppUtils.isNetworkAvailable(this@TextToImageActivity)) {
            this@TextToImageActivity.toast(getString(R.string.check_internet_message))
            return
        }

        val selectAudioBottomSheet = SelectAudioBottomSheet.newInstance()
        selectAudioBottomSheet.show(
            supportFragmentManager,
            SelectAudioBottomSheet::class.java.simpleName
        )
    }



    private fun saveTextAsImage(onSaved: (path: String) -> Unit) {
        if (binding.etText.text.isNullOrBlank()) {
            showErrorToast("Please Write Something")
            return
        }
        if (!AppUtils.isNetworkAvailable(this@TextToImageActivity)) {
            showErrorToast("Check your internet connection")
            return
        }

        isSaving = true
        with(binding) {
            listOf(bgFade, toolbar, btnChangeBg, rvColors).forEach {
                it.visibility = View.GONE
            }
            etText.isCursorVisible = false
        }

        lifecycleScope.launch {
            val path = getPathForStorage(this@TextToImageActivity, "text_to_image.jpg")
            val isSavedSuccessfully = TextToImageUtils.saveImage(binding.root, path)
            if (isSavedSuccessfully.not()) {
                showErrorToast("Something went wrong")
                finish()

            }
            onSaved(path)
        }
    }


    private fun showProgressDialog() {
        this.runOnUiThread {
            if (customProgressDialog == null)
                customProgressDialog = CustomProgressDialog()
            customProgressDialog?.show(this, "Please wait")
        }
    }

    private fun hideProgressDialog() {
        this.runOnUiThread {
            customProgressDialog?.dialog?.dismiss()
        }
    }

    override fun onAudioSelected() {
        binding.ivAudioSelected.isVisible =
            sharedAudioViewModel.selectedAudioItem != null
    }

    private fun showErrorToast(msg: String) {
        this.runOnUiThread {
            this.toast(msg)
        }
    }


    override fun onProgressChanged(p0: SeekBar?, seekbarVal: Int, p2: Boolean) {
        binding.etText.setTextSize(TypedValue.COMPLEX_UNIT_SP, (seekbarVal + 12).toFloat())
    }

    override fun onStartTrackingTouch(p0: SeekBar?) {}
    override fun onStopTrackingTouch(p0: SeekBar?) {}


    companion object {

        fun startActivity(context: Context) {
            val intent = Intent(context, TextToImageActivity::class.java).apply {
            }
            context.startActivity(intent)
        }
    }
}
