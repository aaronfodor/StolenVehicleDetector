package com.arpadfodor.stolenvehicledetector.android.app.view.utils

import android.content.Context
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import com.arpadfodor.stolenvehicledetector.android.app.R

/**
 * Positive Button of the app
 */
class AppPositiveButton : AppButton {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        this.background = ContextCompat.getDrawable(context, R.drawable.app_positive_button)
    }

}