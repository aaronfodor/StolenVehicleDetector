package com.arpadfodor.stolenvehicledetector.android.app.viewmodel

import com.arpadfodor.stolenvehicledetector.android.app.model.api.ApiService
import com.arpadfodor.stolenvehicledetector.android.app.model.api.ApiVehicleReport
import com.arpadfodor.stolenvehicledetector.android.app.viewmodel.utils.RecognitionViewModel

class ReportViewModel : RecognitionViewModel(){

    override fun sendRecognition(id: Int, callback: (Boolean) -> Unit){

        val recognition = recognitions.value?.find { it.artificialId == id } ?: return
        val apiReport = ApiVehicleReport(0, recognition.licenseId, "",
            recognition.latitude.toDouble(), recognition.longitude.toDouble(),
            recognition.message, recognition.date)

        ApiService.postReport(apiReport) { isSuccess ->
            if(isSuccess){
                deleteRecognition(id)
            }
            callback(isSuccess)
        }

    }

    override fun deleteRecognition(id: Int){
        val filteredAlerts = recognitions.value?.filter {
            it.artificialId != id
        }
        recognitions.postValue(filteredAlerts)
    }

}