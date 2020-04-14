package com.arpadfodor.android.stolencardetector.view.utils

import android.content.res.Resources
import android.view.View
import com.arpadfodor.android.stolencardetector.R
import com.google.android.material.snackbar.Snackbar

object AppSnackBarBuilder {

    fun buildAppSnackBar(res: Resources, view: View, text: String, duration: Int): Snackbar{
        val snackbar = Snackbar.make(view, text, duration)
        snackbar.setBackgroundTint(res.getColor(R.color.colorAppSnackBarBackground))
        snackbar.setTextColor(res.getColor(R.color.colorText))
        return snackbar
    }

    fun buildSuccessSnackBar(res: Resources, view: View, text: String, duration: Int): Snackbar{
        val snackbar = Snackbar.make(view, text, duration)
        snackbar.setBackgroundTint(res.getColor(R.color.colorSuccessSnackBarBackground))
        snackbar.setTextColor(res.getColor(R.color.colorText))
        return snackbar
    }

    fun buildAlertSnackBar(res: Resources, view: View, text: String, duration: Int): Snackbar{
        val snackbar = Snackbar.make(view, text, duration)
        snackbar.setBackgroundTint(res.getColor(R.color.colorAlertSnackBarBackground))
        snackbar.setTextColor(res.getColor(R.color.colorText))
        return snackbar
    }

}