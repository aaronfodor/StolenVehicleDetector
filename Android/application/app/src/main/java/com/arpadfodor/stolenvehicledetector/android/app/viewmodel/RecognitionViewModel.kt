package com.arpadfodor.stolenvehicledetector.android.app.viewmodel

import androidx.lifecycle.MutableLiveData
import com.arpadfodor.stolenvehicledetector.android.app.model.AccountService
import com.arpadfodor.stolenvehicledetector.android.app.model.api.ApiService
import com.arpadfodor.stolenvehicledetector.android.app.model.api.dataclasses.ApiReport
import com.arpadfodor.stolenvehicledetector.android.app.model.repository.UserRecognitionRepository
import com.arpadfodor.stolenvehicledetector.android.app.model.repository.dataclasses.UserRecognition
import com.arpadfodor.stolenvehicledetector.android.app.viewmodel.utils.MasterDetailViewModel

class RecognitionViewModel : MasterDetailViewModel(){

    /**
     * List of recognition elements
     **/
    override val recognitions: MutableLiveData<List<UserRecognition>> by lazy {
        MutableLiveData<List<UserRecognition>>(listOf())
    }

    fun updateDataFromDb(){

        val user = AccountService.userId

        UserRecognitionRepository.getByUser(user){ userRecognitionList ->

            var isSelectedRecognitionExists = false

            userRecognitionList.forEach { element ->
                if(element.artificialId == selectedRecognitionId.value){
                    element.isSelected = true
                    isSelectedRecognitionExists = true
                }
            }

            recognitions.postValue(userRecognitionList)

            // if the selected Id does not exists anymore, clear show details flag
            if(!isSelectedRecognitionExists){
                showDetails.postValue(false)
            }

        }

    }

    override fun sendRecognition(id: Int, callback: (Boolean) -> Unit){

        val recognition = recognitions.value?.find { it.artificialId == id } ?: return

        val apiReport =
            ApiReport(recognition.artificialId, recognition.licenseId, recognition.reporter,
            recognition.latitude.toDouble(), recognition.longitude.toDouble(),
            recognition.message, recognition.date)

        ApiService.postReport(apiReport) { isPostSuccess ->

            if(isPostSuccess){

                deselectRecognition()

                val user = AccountService.userId
                UserRecognitionRepository.updateSentFlagByIdAndUser(id, user, true){ isDbSuccess ->

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

        val user = AccountService.userId

        UserRecognitionRepository.updateMessageByIdAndUser(id, user, message){ isSuccess ->
            if(isSuccess){
                updateDataFromDb()
            }
            callback(isSuccess)
        }

    }

    override fun deleteRecognition(id: Int, callback: (Boolean) -> Unit){

        Thread{

            val user = AccountService.userId

            UserRecognitionRepository.deleteByIdAndUser(id, user){ isSuccess ->
                if(isSuccess){
                    deselectRecognition()
                    updateDataFromDb()
                }
                callback(isSuccess)
            }

        }.start()

    }

}