package com.arpadfodor.stolenvehicledetector.android.app.view.utils

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.DisplayCutout
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import androidx.annotation.RequiresApi
import androidx.core.animation.addListener
import com.arpadfodor.stolenvehicledetector.android.app.R

/**
 * Simulate a button click, including a small delay while it is being pressed to trigger the
 * appropriate animations.
 */
fun ImageButton.simulateClick(delay: Long = resources.getInteger(R.integer.ANIMATION_FAST_MILLIS).toLong()) {

    performClick()
    isPressed = true
    invalidate()

    postDelayed({
        invalidate()
        isPressed = false
    }, delay)

}

/**
 * Pad this view with the insets provided by the device cutout (i.e. notch)
 */
@RequiresApi(Build.VERSION_CODES.P)
fun View.padWithDisplayCutout() {

    /**
     * Helper method that applies padding from cutout's safe insets
     */
    fun doPadding(cutout: DisplayCutout) = setPadding(
        cutout.safeInsetLeft,
        cutout.safeInsetTop,
        cutout.safeInsetRight,
        cutout.safeInsetBottom)

    // Apply padding using the display cutout designated "safe area"
    rootWindowInsets?.displayCutout?.let { doPadding(it) }

    // Set a listener for window insets since view.rootWindowInsets may not be ready yet
    setOnApplyWindowInsetsListener { _, insets ->
        insets.displayCutout?.let { doPadding(it) }
        insets
    }
}

fun View.disappearingAnimation(context: Context){
    val anim = AnimationUtils.loadAnimation(context, android.R.anim.fade_out)
    visibility = View.GONE
    animation = anim
    animation?.start()
}

fun View.appearingAnimation(context: Context){
    val anim = AnimationUtils.loadAnimation(context, android.R.anim.fade_in)
    visibility = View.VISIBLE
    animation = anim
    animation?.start()
}

fun View.overshootAppearingAnimation(context: Context){
    visibility = View.VISIBLE
    val view = this
    val animator = AnimatorInflater.loadAnimator(context, R.animator.overshoot_enter) as AnimatorSet
    animator.apply {
        setTarget(view)
        start()
    }
}

fun View.startInfiniteAnimation(context: Context){

    val view = this
    val animator = AnimatorInflater.loadAnimator(context, R.animator.highlight) as AnimatorSet
    animator.apply {
        this.addListener(
            onEnd = {
                it.startDelay = 1000
                start()
            }
        )
        setTarget(view)
        start()
    }

}

fun View.removeAnimation(){
    clearAnimation()
}

fun Drawable.toBitmap(): Bitmap {

    if (this is BitmapDrawable) {
        return this.bitmap
    }

    val bitmap = Bitmap.createBitmap(this.intrinsicWidth, this.intrinsicHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    this.setBounds(0, 0, canvas.width, canvas.height)
    this.draw(canvas)

    return bitmap

}