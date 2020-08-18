package com.arpadfodor.stolenvehicledetector.android.app.viewmodel.utils

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

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

    open fun sendRecognition(id: Int, callback: (Boolean) -> Unit){}

    open fun deleteRecognition(id: Int, callback: (Boolean) -> Unit){
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