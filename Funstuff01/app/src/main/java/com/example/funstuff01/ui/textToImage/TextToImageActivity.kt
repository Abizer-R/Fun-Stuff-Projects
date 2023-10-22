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
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.funstuff01.ui.base.BaseActivity
import com.example.funstuff01.databinding.ActivityTextToImageBinding
import com.example.funstuff01.ui.textToImage.adapter.ColorsAdapter
import com.example.funstuff01.utils.AppUtils
import com.example.funstuff01.utils.AppUtils.getPathForStorage
import com.example.funstuff01.utils.TextToImageUtils
import com.example.funstuff01.utils.hideStatusBar
import com.example.funstuff01.utils.showStatusBar
import com.example.funstuff01.utils.toast
import com.example.funstuff01.utils.views.CustomProgressDialog
import kotlinx.coroutines.launch

class TextToImageActivity : BaseActivity<ActivityTextToImageBinding>(ActivityTextToImageBinding::inflate),
        ColorsAdapter.ColorsClickListener, SeekBar.OnSeekBarChangeListener {

    private val mainBgList = mutableListOf<Int>()
    private val btnBgList = mutableListOf<Int>()
    private var currentBg = 0
    private var isSaving: Boolean = false

    private var colorsAdapter: ColorsAdapter? = null
    private var customProgressDialog: CustomProgressDialog? = null

    override fun initUserInterface() {


        with(binding) {

            colorsAdapter = ColorsAdapter(this@TextToImageActivity)
            rvColors.apply {
                layoutManager = LinearLayoutManager(this@TextToImageActivity, RecyclerView.HORIZONTAL, false)
                adapter = colorsAdapter
            }

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
                saveTextAsImage { imagePath ->
                    Log.e("TESTING2", "outputImage Path = $imagePath", )

                    // TODO open image/video preview screen


                }
            }

            btnBack.setOnClickListener {
                onBackPressed()
            }
        }

        colorsAdapter?.submitList(TextToImageUtils.getColorResourceList())
        mainBgList.addAll(TextToImageUtils.getMainBgList())
        btnBgList.addAll(TextToImageUtils.getBtnBgList())
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

    override fun onResume() {
        super.onResume()
        this.hideStatusBar()
    }

    override fun onPause() {
        super.onPause()
        this.showStatusBar()
    }
    companion object {

        fun startActivity(context: Context) {
            val intent = Intent(context, TextToImageActivity::class.java).apply {
            }
            context.startActivity(intent)
        }
    }
}
