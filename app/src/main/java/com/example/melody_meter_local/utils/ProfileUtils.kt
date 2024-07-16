package com.example.melody_meter_local.utils

import android.content.Context
import android.widget.Toast

object ProfileUtils {
    fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
