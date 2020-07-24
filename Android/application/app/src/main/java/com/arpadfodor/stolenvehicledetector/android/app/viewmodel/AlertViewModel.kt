package com.arpadfodor.stolenvehicledetector.android.app.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.arpadfodor.stolenvehicledetector.android.app.viewmodel.utils.Report

class AlertViewModel : ViewModel(){

    /**
     * List of alert elements
     **/
    val alerts: MutableLiveData<List<Report>> by lazy {
        MutableLiveData<List<Report>>()
    }

}