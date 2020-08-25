package com.arpadfodor.stolenvehicledetector.android.app.viewmodel

import androidx.lifecycle.MutableLiveData
import com.arpadfodor.stolenvehicledetector.android.app.model.MediaHandler
import com.arpadfodor.stolenvehicledetector.android.app.model.db.dataclasses.UserReport
import com.arpadfodor.stolenvehicledetector.android.app.model.repository.UserReportRepository
import com.arpadfodor.stolenvehicledetector.android.app.viewmodel.utils.Recognition
import com.arpadfodor.stolenvehicledetector.android.app.viewmodel.utils.RecognitionViewModel

class AlertViewModel : RecognitionViewModel(){

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
     * List of recognition elements
     **/
    override val recognitions: MutableLiveData<List<Recognition>> by lazy {
        MutableLiveData<List<Recognition>>(listParam)
    }

    override fun sendRecognition(id: Int, callback: (Boolean) -> Unit){

        val recognition = recognitions.value?.find { it.artificialId == id } ?: return

        Thread{

            val userReport =
                UserReport(null, recognition.licenseId, recognition.reporter,
                    recognition.latitude.toDouble(), recognition.longitude.toDouble(),
                    recognition.message, recognition.date, false, null)

            UserReportRepository.postUserReport(userReport, recognition.image) { isSuccess ->

                if(isSuccess){
                    deleteRecognition(id){ isSuccess ->
                        callback(isSuccess)
                    }
                }
                else{
                    callback(false)
                }

            }

        }.start()

    }

}