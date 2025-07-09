// 경로: com/example/myot/util/DialogUtils.kt

package com.example.myot.util

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import com.example.myot.R

object DialogUtils {
    fun createLoadingDialog(context: Context): AlertDialog {
        val builder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.loading_dialog, null)
        builder.setView(view)
        builder.setCancelable(false)
        return builder.create()
    }
}
