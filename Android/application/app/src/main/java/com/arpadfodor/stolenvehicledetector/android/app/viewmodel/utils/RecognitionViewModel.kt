package com.arpadfodor.stolenvehicledetector.android.app.viewmodel.utils

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.arpadfodor.stolenvehicledetector.android.app.model.api.ApiService
import com.arpadfodor.stolenvehicledetector.android.app.model.api.ApiVehicleReport

abstract class RecognitionViewModel : ViewModel(){

    /**
     * List of recognition elements
     **/
    open val recognitions: MutableLiveData<List<Recognition>> by lazy {
        MutableLiveData<List<Recognition>>()
    }

    /**
     * The current, selected recognition
     **/
    val selectedRecognitionId: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>(0)
    }

    /**
     * Whether to show details fragment
     **/
    val showDetails: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>(false)
    }

    fun sendRecognition(id: Int, callback: (Boolean) -> Unit){

        val recognition = recognitions.value?.find { it.artificialId == id } ?: return
        val apiReport = ApiVehicleReport(0, recognition.licenseId, "",
            recognition.latitude.toDouble(), recognition.longitude.toDouble(),
            recognition.message, recognition.date)

        ApiService.postReport(apiReport) { isSuccess ->
            if(isSuccess){
                deleteRecognition(id){ isSuccess ->
                    callback(isSuccess)
                }
            }
            callback(isSuccess)
        }

    }

    fun deleteRecognition(id: Int, callback: (Boolean) -> Unit){
        val filteredAlerts = recognitions.value?.filter {
            it.artificialId != id
        }
        recognitions.postValue(filteredAlerts)
        callback(true)
    }

    fun selectRecognition(id: Int) {
        selectedRecognitionId.value = id
        showDetails.value = true
    }

    fun deselectRecognition() {
        selectedRecognitionId.value = 0
        showDetails.value = false
    }

    fun getRecognitionById(id: Int) : Recognition?{
        return recognitions.value?.find { it.artificialId == id }
    }

    fun updateRecognitionMessageById(id: Int, message: String){

        val updatedList = recognitions.value ?: return

        for(alert in updatedList){
            if(alert.artificialId != id){
                continue
            }
            alert.message = message
        }

        recognitions.value = updatedList

    }

}