package com.arpadfodor.stolenvehicledetector.android.app.viewmodel.utils

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.arpadfodor.stolenvehicledetector.android.app.view.utils.AppSnackBarBuilder
import com.google.android.material.snackbar.Snackbar

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
        selectedRecognitionId.postValue(id)
        showDetails.postValue(true)
    }

    fun deselectRecognition() {
        selectedRecognitionId.postValue(0)
        showDetails.postValue(false)
    }

    fun getRecognitionById(id: Int) : Recognition?{
        return recognitions.value?.find { it.artificialId == id }
    }

    open fun updateRecognitionMessage(id: Int, message: String, callback: (Boolean) -> Unit){

        val updatedList = recognitions.value ?: return

        for(recognition in updatedList){
            if(recognition.artificialId != id){
                continue
            }
            recognition.message = message
        }

        recognitions.postValue(updatedList)
        callback(true)

    }

}