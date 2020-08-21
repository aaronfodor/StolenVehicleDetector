package com.arpadfodor.stolenvehicledetector.android.app.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.arpadfodor.stolenvehicledetector.android.app.model.TextToSpeechService

class HowToUseViewModel : ViewModel(){

    val isTextToSpeechSpeaking: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    fun subscribeTextToSpeechListeners(errorCallback: () -> Unit){

        val isSpeaking = TextToSpeechService.isSpeaking()
        isTextToSpeechSpeaking.postValue(isSpeaking)

        TextToSpeechService.setCallbacks(
            started = {
                isTextToSpeechSpeaking.postValue(true)
            },
            finished = {
                isTextToSpeechSpeaking.postValue(false)
            },
            error = {
                isTextToSpeechSpeaking.postValue(false)
                errorCallback()
            }
        )

    }

    fun textToSpeechButtonClicked(text: String){

        if(isTextToSpeechSpeaking.value == true){
            stopTextToSpeech()
        }
        else{
            startTextToSpeech(text)
        }

    }

    fun startTextToSpeech(text: String){
        TextToSpeechService.speak(text)
    }

    fun stopTextToSpeech(){
        TextToSpeechService.stop()
    }

}