package com.arpadfodor.stolenvehicledetector.android.app.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.arpadfodor.stolenvehicledetector.android.app.viewmodel.utils.Recognition

class AlertViewModel : ViewModel(){

    companion object{

        /**
         * Use it to pass list parameter to an instance of this activity before starting it.
         * Used because passing custom objects between activities can be problematic via intents.
         **/
        fun setParameter(list: List<Recognition>){
            listParam = list
        }

        private var listParam = listOf<Recognition>()

    }

    /**
     * List of alert elements
     **/
    val alerts: MutableLiveData<List<Recognition>> by lazy {
        MutableLiveData<List<Recognition>>(listParam)
    }

    /**
     * The current, selected alert
     **/
    val selectedAlertId: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>(0)
    }

    /**
     * Whether to show details fragment
     **/
    val showDetails: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>(false)
    }

    fun sendRecognition(id: Int){
        val toSend = alerts.value?.find { it.artificialId == id } ?: return
        //TODO: API send
        deleteRecognition(id)
    }

    fun deleteRecognition(id: Int){
        val filteredAlerts = alerts.value?.filter {
            it.artificialId != id
        }
        alerts.postValue(filteredAlerts)
    }

    fun selectRecognition(id: Int) {
        selectedAlertId.value = id
        showDetails.value = true
    }

    fun deselectRecognition() {
        selectedAlertId.value = 0
        showDetails.value = false
    }

    fun getAlertById(id: Int) : Recognition?{
        return alerts.value?.find { it.artificialId == id }
    }

    fun updateAlertMessageById(id: Int, message: String){

        val updatedList = alerts.value ?: return

        for(alert in updatedList){
            if(alert.artificialId != id){
                continue
            }
            alert.message = message
        }

        alerts.value = updatedList

    }

}