package com.arpadfodor.stolenvehicledetector.android.app.viewmodel.utils

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.arpadfodor.stolenvehicledetector.android.app.model.repository.dataclasses.UserRecognition

abstract class MasterDetailViewModel : ViewModel(){

    /**
     * List of recognition elements
     **/
    open val recognitions: MutableLiveData<List<UserRecognition>> by lazy {
        MutableLiveData<List<UserRecognition>>()
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

    fun getRecognitionById(id: Int) : UserRecognition?{
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

    open fun loadImage(imagePath: String, callback: (Bitmap) -> Unit){}

}