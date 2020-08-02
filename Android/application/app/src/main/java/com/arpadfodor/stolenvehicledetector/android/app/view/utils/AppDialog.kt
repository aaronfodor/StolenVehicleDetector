package com.arpadfodor.stolenvehicledetector.android.app.view.utils

import androidx.appcompat.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.provider.MediaStore
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.arpadfodor.stolenvehicledetector.android.app.R
import com.arpadfodor.stolenvehicledetector.android.app.model.MediaHandler

/**
 * Dialog class of the app
 *
 * @param    context            Context of the parent where the dialog is shown
 * @param    title              Title of the dialog
 * @param    description        Description of the dialog
 * @param    imageResourceCode  Image resource code shown on the dialog
 */
class AppDialog(context: Context, title: String, description: String, imageResourceCode: Int) : AlertDialog(context) {

    /**
     * Positive and negative Buttons of the dialog
     */
    private var buttonPositive: AppPositiveButton
    private var buttonNegative: AppButton

    init {

        this.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        this.window?.attributes?.windowAnimations = R.style.DialogAnimation

        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.app_dialog, null)
        setView(view)

        val imageViewIcon = view.findViewById<ImageView>(R.id.ivAppDialog)

        val image = ContextCompat.getDrawable(context, imageResourceCode)
        imageViewIcon.setImageDrawable(image)

        val textViewTitle = view.findViewById<TextView>(R.id.tvCustomDialogTitle)
        textViewTitle.text = title

        val textViewDescription = view.findViewById<TextView>(R.id.tvAppDialogDescription)
        textViewDescription.text = description

        buttonPositive = view.findViewById(R.id.btnPositiveAppDialog)
        buttonPositive.setOnClickListener {
            this.dismiss()
        }

        buttonNegative = view.findViewById(R.id.btnNegativeAppDialog)
        buttonNegative.setOnClickListener {
            this.dismiss()
        }

    }

    /**
     * Sets the positive Button on click listener
     *
     * @param    func        Lambda to execute when the positive Button is pressed
     */
    fun setPositiveButton(func: () -> Unit){
        buttonPositive.setOnClickListener {
            this.dismiss()
            func()
        }
    }

    /**
     * Sets the negative Button on click listener
     *
     * @param    func        Lambda to execute when the negative Button is pressed
     */
    fun setNegativeButton(func: () -> Unit){
        buttonNegative.setOnClickListener {
            this.dismiss()
            func()
        }
    }

}