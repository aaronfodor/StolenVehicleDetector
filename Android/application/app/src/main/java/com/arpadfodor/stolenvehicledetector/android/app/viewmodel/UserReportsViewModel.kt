package com.arpadfodor.stolenvehicledetector.android.app.viewmodel

import androidx.lifecycle.MutableLiveData
import com.arpadfodor.stolenvehicledetector.android.app.model.AuthenticationService
import com.arpadfodor.stolenvehicledetector.android.app.model.api.ApiService
import com.arpadfodor.stolenvehicledetector.android.app.model.api.ApiVehicleReport
import com.arpadfodor.stolenvehicledetector.android.app.model.db.DatabaseService
import com.arpadfodor.stolenvehicledetector.android.app.viewmodel.utils.Recognition
import com.arpadfodor.stolenvehicledetector.android.app.viewmodel.utils.RecognitionViewModel

class UserReportsViewModel : RecognitionViewModel(){

    /**
     * List of recognition elements
     **/
    override val recognitions: MutableLiveData<List<Recognition>> by lazy {
        MutableLiveData<List<Recognition>>(listOf())
    }

    fun update(){

        val user = AuthenticationService.userName

        DatabaseService.getUserReportsByUser(user){userReportList ->

            val recognitionList = mutableListOf<Recognition>()

            for(element in userReportList){
                recognitionList.add(
                    Recognition(element.Id, element.Vehicle, null, element.timestampUTC,
                        element.latitude.toString(), element.longitude.toString(), element.Reporter,
                        element.message, element.isSent)
                )
            }

        }

    }

    override fun sendRecognition(id: Int, callback: (Boolean) -> Unit){

        val recognition = recognitions.value?.find { it.artificialId == id } ?: return
        if(!recognition.isActive){
            return
        }

        val apiReport =
            ApiVehicleReport(recognition.artificialId, recognition.licenseId, recognition.reporter,
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

    override fun deleteRecognition(id: Int, callback: (Boolean) -> Unit){

        val user = AuthenticationService.userName

        DatabaseService.deleteUserReportByIdAndUser(id, user){ isSuccess ->

            if(isSuccess){
                val filteredAlerts = recognitions.value?.filter {
                    it.artificialId != id
                }
                recognitions.postValue(filteredAlerts)
                callback(true)
            }
            callback(isSuccess)

        }
    }

}