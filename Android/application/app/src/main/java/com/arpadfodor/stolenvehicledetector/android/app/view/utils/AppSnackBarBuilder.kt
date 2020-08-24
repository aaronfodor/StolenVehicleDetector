package com.arpadfodor.stolenvehicledetector.android.app.view.utils

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.arpadfodor.stolenvehicledetector.android.app.R
import com.google.android.material.snackbar.Snackbar

object AppSnackBarBuilder {

    private fun buildAppSnackBar(view: View, text: String, duration: Int): Snackbar{

        val snackBar = Snackbar.make(view, text, duration)
        val snackBarLayout = snackBar.view

        val textView = snackBarLayout.findViewById<View>(R.id.snackbar_text) as TextView
        // to set drawable
        textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        textView.compoundDrawablePadding = 0

        return snackBar

    }

    fun buildInfoSnackBar(context: Context, view: View, text: String, duration: Int): Snackbar{
        val snackBar = buildAppSnackBar(view, text, duration)
        snackBar.setBackgroundTint(ContextCompat.getColor(context, R.color.colorAppSnackBarBackground))
        snackBar.setTextColor(ContextCompat.getColor(context, R.color.colorAppSnackBarLightText))
        return snackBar
    }

    fun buildSuccessSnackBar(context: Context, view: View, text: String, duration: Int): Snackbar{
        val snackBar = buildAppSnackBar(view, text, duration)
        snackBar.setBackgroundTint(ContextCompat.getColor(context, R.color.colorSuccessSnackBarBackground))
        snackBar.setTextColor(ContextCompat.getColor(context, R.color.colorAppSnackBarLightText))
        return snackBar
    }

    fun buildAlertSnackBar(context: Context, view: View, text: String, duration: Int): Snackbar{
        val snackBar = buildAppSnackBar(view, text, duration)
        snackBar.setBackgroundTint(ContextCompat.getColor(context, R.color.colorAlertSnackBarBackground))
        snackBar.setTextColor(ContextCompat.getColor(context, R.color.colorAppSnackBarLightText))
        return snackBar
    }

}