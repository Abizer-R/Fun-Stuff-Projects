package com.example.funstuff01.utils.views

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.example.funstuff01.R
import com.example.funstuff01.databinding.ProgressDialogViewBinding
import com.example.funstuff01.utils.AppUtils

class CustomProgressDialog constructor(val backgroundColor: Int = R.color.progress_dialog_bg) {

    lateinit var dialog: CustomDialog


    fun show(activity: Activity, title: CharSequence?): Dialog {
        val viewBinding = ProgressDialogViewBinding.inflate(activity.layoutInflater)
        viewBinding.textViewProgressDescription.apply {
            title?.let { text = it }
            setTextColor(Color.WHITE)
        }
        viewBinding.cardViewBackground.setCardBackgroundColor(
            (ContextCompat.getColor(activity, backgroundColor))
        )
        setColorFilter(
            viewBinding.progressBar.indeterminateDrawable,
            ResourcesCompat.getColor(activity.resources, R.color.colorAccent, null)
        )


        dialog = CustomDialog(activity)
        dialog.setContentView(viewBinding.root)
        dialog.show()

        return dialog
    }

    @Suppress("DEPRECATION")
    private fun setColorFilter(drawable: Drawable, color: Int) {
        if (AppUtils.androidQAndAbove) {
            drawable.colorFilter = BlendModeColorFilter(color, BlendMode.SRC_ATOP)
        } else {
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
        }
    }

    class CustomDialog(context: Context) : Dialog(context, R.style.CustomDialogTheme) {
        init {
            window?.decorView?.rootView?.setBackgroundResource(R.color.transparency_20)
            window?.decorView?.setOnApplyWindowInsetsListener { _, insets ->
                insets.consumeSystemWindowInsets()
            }
            setCancelable(false)
        }
    }
}