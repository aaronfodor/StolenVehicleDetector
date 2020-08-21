package com.arpadfodor.stolenvehicledetector.android.app.model

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.*

object TextToSpeechService : UtteranceProgressListener() {

    var textToSpeech: TextToSpeech? = null
    var requestCounter = 0
    var textToSpeechRequestId = System.currentTimeMillis() + requestCounter

    var startedCallback: () -> Unit = {}
    var finishedCallback: () -> Unit = {}
    var errorCallback: () -> Unit = {}

   /**
    * Initialize text to speech
    * Set text to speech listener
    **/
    fun init(context: Context){

        textToSpeech = TextToSpeech(context) { status ->
            if (status != TextToSpeech.ERROR) {
                textToSpeech?.language = Locale.UK
            }
        }

   }

    fun setCallbacks(started: () -> Unit, finished: () -> Unit, error: () -> Unit){
        startedCallback = started
        finishedCallback = finished
        errorCallback = error
        this.textToSpeech?.setOnUtteranceProgressListener(this)
    }

    /**
     * Start speaking
     *
     * @param textToSpeech
     */
    fun speak(textToSpeech: String){
        requestCounter++
        this.textToSpeech?.speak(textToSpeech, TextToSpeech.QUEUE_FLUSH, null, (textToSpeechRequestId).toString())
    }

    /*
     * Stop speaking
     */
    fun stop(){
        if(isSpeaking()){
            textToSpeech?.stop()
            onDone(null)
        }
    }

    fun isSpeaking(): Boolean{
        return textToSpeech?.isSpeaking ?: false
    }

    override fun onStart(p0: String?) {
        startedCallback()
    }

    override fun onDone(p0: String?) {
        finishedCallback()
    }

    override fun onError(p0: String?) {
        errorCallback()
    }

}