package com.example.myapplication.helper

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.example.myapplication.R
import java.lang.ref.WeakReference

object ToastHelper {

    private const val DEFAULT_LAYOUT: Int = R.layout.toast_default_done_icon
    private var mToastRef: WeakReference<Toast?>? = null

    fun showSingleToast(context: Context?, message: String, duration: Int) {
        context ?: return

        mToastRef?.get()?.let {
            it.cancel()
        }
        val toast: Toast = makeToast(context, message, duration)
        mToastRef = WeakReference(toast)
        toast.show()
    }

    private fun makeToast(
        context: Context,
        message: String,
        duration: Int
    ): Toast {
        val toast = Toast(context)
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0)
        toast.duration = duration
        val layout = LayoutInflater.from(context).inflate(DEFAULT_LAYOUT, null)
        val text = layout.findViewById<View>(R.id.toast_message) as TextView
        text.text = message
        toast.view = layout
        return toast
    }

}