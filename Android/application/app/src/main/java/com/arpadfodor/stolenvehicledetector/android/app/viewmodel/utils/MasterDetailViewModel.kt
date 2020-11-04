package com.arpadfodor.stolenvehicledetector.android.app.viewmodel.utils

import androidx.lifecycle.MutableLiveData
import com.arpadfodor.stolenvehicledetector.android.app.model.LocationService
import com.arpadfodor.stolenvehicledetector.android.app.model.repository.dataclasses.UserRecognition

abstract class MasterDetailViewModel : AppViewModel(){

    /**
     * List of recognition elements
     **/
    open val recognitions: MutableLiveData<List<UserRecognition>> by lazy {
        MutableLiveData<List<UserRecognition>>()
    }

    /**
     * Whether or not the UI is in two-pane mode, i.e. running on a tablet device
     */
    var twoPane = false

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

        deselectRecognition()

        val filteredAlerts = recognitions.value?.filter {
            it.artificialId != id
        }
        recognitions.postValue(filteredAlerts)

        callback(true)

    }

    fun selectRecognition(id: Int) {
        selectedRecognitionId.postValue(id)
        showDetails.postValue(true)
        showSelection(id)
    }

    fun deselectRecognition() {
        selectedRecognitionId.postValue(0)
        showDetails.postValue(false)
        showSelection(0)
    }

    fun getRecognitionById(id: Int) : UserRecognition?{
        return recognitions.value?.find { it.artificialId == id }
    }

    open fun updateRecognitionMessage(id: Int, message: String, callback: (Boolean) -> Unit){

        val recognitionList = recognitions.value ?: return
        recognitionList.forEach {
            if(it.artificialId == id){
                it.message = message
            }
        }
        recognitions.postValue(recognitionList)
        callback(true)

    }

    private fun showSelection(id: Int){

        recognitions.value?.let {
            for(recognition in it){
                recognition.isSelected = recognition.artificialId == id
            }
        }
        recognitions.postValue(recognitions.value)

    }

    fun getAddressFromLocation(lat: Double, long: Double, callback: (String) -> Unit){
        return LocationService.getAddressFromLocation(lat, long, callback)
    }

}