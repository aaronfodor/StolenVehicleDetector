package com.arpadfodor.stolenvehicledetector.android.app.view.utils

import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.marginBottom
import androidx.core.view.marginEnd
import androidx.core.view.marginStart
import androidx.core.view.marginTop
import androidx.databinding.BindingAdapter
import com.arpadfodor.stolenvehicledetector.android.app.R
import com.arpadfodor.stolenvehicledetector.android.app.model.repository.dataclasses.UserRecognition

@BindingAdapter("recognitionId")
fun TextView.setRecognitionId(item: UserRecognition) {
    text = item.licenseId
}

@BindingAdapter("recognitionDate")
fun TextView.setRecognitionDate(item: UserRecognition) {
    text = context.getString(R.string.recognition_item_timestamp, item.date)
}

@BindingAdapter("recognitionLocation")
fun TextView.setRecognitionLocation(item: UserRecognition) {
    text = context.getString(R.string.recognition_item_location, item.longitude.toFloat(), item.latitude.toFloat())
}

@BindingAdapter("recognitionMessage")
fun TextView.setRecognitionMessage(item: UserRecognition) {

    visibility = if(item.message.isEmpty()){
        View.GONE
    }
    else{
        View.VISIBLE
    }

    text = context.getString(R.string.recognition_item_message, item.message)

}

@BindingAdapter("recognitionEditButton")
fun ImageButton.setRecognitionEditButton(item: UserRecognition) {

    if(item.isSent){
        this.setImageResource(android.R.drawable.ic_menu_info_details)
    }
    else{
        this.setImageResource(android.R.drawable.ic_menu_edit)
    }

}

@BindingAdapter("recognitionSendButton")
fun ImageButton.setRecognitionSendButton(item: UserRecognition) {

    if(item.isAlert){

        if(item.isSent){
            this.setImageResource(R.drawable.icon_tick)
        }
        else{
            this.setImageResource(android.R.drawable.ic_input_add)
        }

    }

    else{

        if(item.isSent){
            this.setImageResource(R.drawable.icon_tick)
        }
        else{
            this.setImageResource(android.R.drawable.ic_menu_send)
        }

    }

}