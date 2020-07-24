package com.arpadfodor.stolenvehicledetector.android.app.view.utils

import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.arpadfodor.stolenvehicledetector.android.app.R
import com.arpadfodor.stolenvehicledetector.android.app.viewmodel.utils.Report

@BindingAdapter("reportId")
fun TextView.setReportId(item: Report) {
    text = item.licenseId
}

@BindingAdapter("reportDate")
fun TextView.setReportDate(item: Report) {
    text = context.getString(R.string.report_item_timestamp, item.date)
}

@BindingAdapter("reportLocation")
fun TextView.setReportLocation(item: Report) {
    text = context.getString(R.string.report_item_location, item.longitude, item.latitude)
}

@BindingAdapter("reportMessage")
fun TextView.setReportMessage(item: Report) {

    visibility = if(item.message.isEmpty()){
        View.GONE
    }
    else{
        View.VISIBLE
    }

    text = context.getString(R.string.report_item_message, item.message)

}