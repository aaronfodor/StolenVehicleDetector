package com.arpadfodor.stolenvehicledetector.android.app.view.utils

import android.text.SpannableStringBuilder
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.arpadfodor.stolenvehicledetector.android.app.R
import com.arpadfodor.stolenvehicledetector.android.app.viewmodel.utils.Recognition

@BindingAdapter("recognitionId")
fun TextView.setRecognitionId(item: Recognition) {
    text = item.licenseId
}

@BindingAdapter("recognitionDate")
fun TextView.setRecognitionDate(item: Recognition) {
    text = context.getString(R.string.recognition_item_timestamp, item.date)
}

@BindingAdapter("recognitionLocation")
fun TextView.setRecognitionLocation(item: Recognition) {
    text = context.getString(R.string.recognition_item_location, item.longitude, item.latitude)
}

@BindingAdapter("recognitionMessage")
fun TextView.setRecognitionMessage(item: Recognition) {

    visibility = if(item.message.isEmpty()){
        View.GONE
    }
    else{
        View.VISIBLE
    }

    text = context.getString(R.string.recognition_item_message, item.message)

}