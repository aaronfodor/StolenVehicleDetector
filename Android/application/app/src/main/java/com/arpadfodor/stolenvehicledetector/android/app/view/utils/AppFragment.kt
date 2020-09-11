package com.arpadfodor.stolenvehicledetector.android.app.view.utils

import androidx.fragment.app.Fragment

abstract class AppFragment : Fragment(){

    override fun onResume() {
        super.onResume()
        subscribeToViewModel()
        subscribeListeners()
        appearingAnimations()
    }

    override fun onPause() {
        unsubscribe()
        super.onPause()
    }

    abstract fun appearingAnimations()
    abstract fun subscribeToViewModel()
    abstract fun subscribeListeners()
    abstract fun unsubscribe()

}