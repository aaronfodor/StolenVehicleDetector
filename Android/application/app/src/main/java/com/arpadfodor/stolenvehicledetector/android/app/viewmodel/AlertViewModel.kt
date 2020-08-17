package com.arpadfodor.stolenvehicledetector.android.app.viewmodel

import androidx.lifecycle.MutableLiveData
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

}