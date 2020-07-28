package com.arpadfodor.stolenvehicledetector.android.app.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.arpadfodor.stolenvehicledetector.android.app.model.api.ApiService
import com.arpadfodor.stolenvehicledetector.android.app.model.api.ApiVehicleReport
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

    fun sendRecognition(id: Int, callback: (Boolean) -> Unit){

        val recognition = alerts.value?.find { it.artificialId == id } ?: return
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

    fun getRecognitionById(id: Int) : Recognition?{
        return alerts.value?.find { it.artificialId == id }
    }

    fun updateRecognitionMessageById(id: Int, message: String){

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