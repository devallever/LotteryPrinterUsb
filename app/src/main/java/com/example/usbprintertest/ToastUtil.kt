package com.example.usbprintertest

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast

@SuppressLint("StaticFieldLeak")
object ToastUtil {

    private var mContext: Context? = null

    fun initToast(context: Context) {
        mContext = context
    }
    fun show(msg: String) {
        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show()
    }
}