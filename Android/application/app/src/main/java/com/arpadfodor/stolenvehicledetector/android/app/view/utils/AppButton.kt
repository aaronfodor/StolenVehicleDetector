package com.arpadfodor.stolenvehicledetector.android.app.view.utils

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import com.arpadfodor.stolenvehicledetector.android.app.R

/**
 * Custom Button of the app - can be inherited from
 */
open class AppButton : AppCompatButton {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {

        this.background = ContextCompat.getDrawable(context, R.drawable.app_button)
        this.setTextColor(context.getColor(R.color.colorText))
        this.setPadding(15,25,15,25)
        this.gravity = Gravity.CENTER
        this.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        this.isAllCaps = true

        this.setOnClickListener {
        }

    }

    /**
     * Sets the Button on click listener
     *
     * @param    func        Lambda to execute when the Button is pressed
     */
    fun setOnClickEvent(func: () -> Unit){
        this.setOnClickListener {
            func()
        }
    }

}