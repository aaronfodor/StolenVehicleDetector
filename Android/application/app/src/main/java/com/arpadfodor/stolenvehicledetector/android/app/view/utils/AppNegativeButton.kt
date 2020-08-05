package com.arpadfodor.stolenvehicledetector.android.app.view.utils

import android.content.Context
import android.util.AttributeSet
import com.arpadfodor.stolenvehicledetector.android.app.R

/**
 * Negative Button of the app
 */
class AppNegativeButton : AppButton {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        this.background = context.getDrawable(R.drawable.app_negative_button)
    }

}