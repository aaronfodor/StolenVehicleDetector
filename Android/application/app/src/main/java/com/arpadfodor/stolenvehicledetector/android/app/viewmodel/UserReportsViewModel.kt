package com.arpadfodor.stolenvehicledetector.android.app.viewmodel

import androidx.lifecycle.MutableLiveData
import com.arpadfodor.stolenvehicledetector.android.app.model.AuthenticationService
import com.arpadfodor.stolenvehicledetector.android.app.model.api.ApiService
import com.arpadfodor.stolenvehicledetector.android.app.model.api.ApiVehicleReport
import com.arpadfodor.stolenvehicledetector.android.app.model.repository.UserReportRepository
import com.arpadfodor.stolenvehicledetector.android.app.viewmodel.utils.Recognition
import com.arpadfodor.stolenvehicledetector.android.app.viewmodel.utils.RecognitionViewModel

class UserReportsViewModel : RecognitionViewModel(){

    /**
     * List of recognition elements
     **/
    override val recognitions: MutableLiveData<List<Recognition>> by lazy {
        MutableLiveData<List<Recognition>>(listOf())
    }

    fun updateDataFromDb(){

        val user = AuthenticationService.userName

        UserReportRepository.getByUser(user){ userReportList ->

            val recognitionList = mutableListOf<Recognition>()

            for(userReport in userReportList){
                recognitionList.add(
                    Recognition(userReport.Id?.toInt() ?: 0, userReport.isSent,
                        userReport.Vehicle, null, userReport.timestampUTC,
                        userReport.latitude.toString(), userReport.longitude.toString(),
                        userReport.Reporter, userReport.message)
                )
            }

            recognitions.postValue(recognitionList)

        }

    }

    override fun sendRecognition(id: Int, callback: (Boolean) -> Unit){

        val recognition = recognitions.value?.find { it.artificialId == id } ?: return

        val apiReport =
            ApiVehicleReport(recognition.artificialId, recognition.licenseId, recognition.reporter,
            recognition.latitude.toDouble(), recognition.longitude.toDouble(),
            recognition.message, recognition.date)

        ApiService.postReport(apiReport) { isPostSuccess ->

            if(isPostSuccess){

                val user = AuthenticationService.userName
                UserReportRepository.updateSentFlagByIdAndUser(id, user, true){ isDbSuccess ->

                    if(isDbSuccess){
                        updateDataFromDb()
                        callback(true)
                    }
                    else{
                        callback(false)
                    }

                }

            }
            else{
                callback(false)
            }

        }

    }

    override fun updateRecognitionMessage(id: Int, message: String, callback: (Boolean) -> Unit){
        val user = AuthenticationService.userName
        UserReportRepository.updateMessageByIdAndUser(id, user, message){ isSuccess ->
            if(isSuccess){
                updateDataFromDb()
            }
            callback(isSuccess)
        }
    }

    override fun deleteRecognition(id: Int, callback: (Boolean) -> Unit){

        val user = AuthenticationService.userName

        UserReportRepository.deleteByIdAndUser(id, user){ isSuccess ->

            if(isSuccess){
                updateDataFromDb()
            }
            callback(isSuccess)

        }
    }

}