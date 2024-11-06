package com.example.funstuff01.utils

import android.view.View

/**
 * OnSingleClickListener class to handle double click issue
 * We have added 200 milli second delay to prevent accidental double click
 * Two constructor to handling lamda function for kotlin
 */
class OnSingleClickListener : View.OnClickListener {

    private val onClickListener: View.OnClickListener

    companion object {
        private const val DELAY_MILLIS = 600L
        private var previousClickTimeMillis = 0L
    }

    constructor(listener: View.OnClickListener) {
        onClickListener = listener
    }

    constructor(listener: (View) -> Unit) {
        onClickListener = View.OnClickListener { listener.invoke(it) }
    }

    override fun onClick(v: View?) {

        val currentTimeMillis = System.currentTimeMillis()

        if (currentTimeMillis < previousClickTimeMillis || currentTimeMillis >= previousClickTimeMillis + DELAY_MILLIS) {
            previousClickTimeMillis = currentTimeMillis
            onClickListener.onClick(v)
        }
    }
}