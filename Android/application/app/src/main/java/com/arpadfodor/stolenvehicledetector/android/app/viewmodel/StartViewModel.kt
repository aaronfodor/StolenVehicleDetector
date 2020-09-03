package com.arpadfodor.stolenvehicledetector.android.app.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class StartViewModel : ViewModel(){

    /**
     * Whether the permissions has been granted
     **/
    val hasPermissionsGranted: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

}