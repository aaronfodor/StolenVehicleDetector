package com.arpadfodor.stolenvehicledetector.android.app.view.utils

import android.content.res.Resources
import android.view.View
import android.widget.TextView
import com.arpadfodor.stolenvehicledetector.android.app.R
import com.google.android.material.snackbar.Snackbar

object AppSnackBarBuilder {

    private fun buildAppSnackBar(view: View, text: String, duration: Int, drawable: Int): Snackbar{

        val snackbar = Snackbar.make(view, text, duration)
        val snackbarLayout = snackbar.view

        val textView = snackbarLayout.findViewById<View>(com.google.android.material.R.id.snackbar_text) as TextView
        textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, drawable, 0)
        textView.compoundDrawablePadding = 0

        return snackbar

    }

    fun buildInfoSnackBar(res: Resources, view: View, text: String, duration: Int): Snackbar{
        val snackbar = buildAppSnackBar(view, text, duration, 0)
        snackbar.setBackgroundTint(res.getColor(R.color.colorAppSnackBarBackground))
        snackbar.setTextColor(res.getColor(R.color.colorText))
        return snackbar

    }

    fun buildSuccessSnackBar(res: Resources, view: View, text: String, duration: Int): Snackbar{
        val snackbar = buildAppSnackBar(view, text, duration, R.drawable.icon_tick)
        snackbar.setBackgroundTint(res.getColor(R.color.colorSuccessSnackBarBackground))
        snackbar.setTextColor(res.getColor(R.color.colorText))
        return snackbar
    }

    fun buildAlertSnackBar(res: Resources, view: View, text: String, duration: Int): Snackbar{
        val snackbar = buildAppSnackBar(view, text, duration, R.drawable.icon_cross)
        snackbar.setBackgroundTint(res.getColor(R.color.colorAlertSnackBarBackground))
        snackbar.setTextColor(res.getColor(R.color.colorText))
        return snackbar
    }

}