package com.arpadfodor.stolenvehicledetector.android.app.viewmodel

import androidx.lifecycle.MutableLiveData
import com.arpadfodor.stolenvehicledetector.android.app.model.repository.UserRecognitionRepository
import com.arpadfodor.stolenvehicledetector.android.app.model.repository.dataclasses.UserRecognition
import com.arpadfodor.stolenvehicledetector.android.app.viewmodel.utils.MasterDetailViewModel

class AlertViewModel : MasterDetailViewModel(){

    companion object{

        /**
         * Use it to pass list parameter to an instance of this activity before starting it.
         * Used because passing custom objects between activities can be problematic via intents.
         **/
        fun setParameter(list: List<UserRecognition>){
            listParam = list
        }

        private var listParam = listOf<UserRecognition>()

    }

    /**
     * List of recognition elements
     **/
    override val recognitions: MutableLiveData<List<UserRecognition>> by lazy {
        MutableLiveData<List<UserRecognition>>(listParam)
    }

    override fun sendRecognition(id: Int, callback: (Boolean) -> Unit){

        val recognition = recognitions.value?.find { it.artificialId == id } ?: return

        Thread{

            UserRecognitionRepository.postUserRecognition(recognition) { isSuccess ->

                if(isSuccess){

                    deleteRecognition(id){ deleteSuccess ->
                        callback(deleteSuccess)
                    }

                }
                else{
                    callback(false)
                }

            }

        }.start()

    }

}