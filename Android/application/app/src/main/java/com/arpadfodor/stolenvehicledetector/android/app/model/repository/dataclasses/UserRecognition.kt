package com.arpadfodor.stolenvehicledetector.android.app.model.repository.dataclasses

import android.graphics.Bitmap

data class UserRecognition(
    val artificialId: Int,
    var isSent: Boolean,
    val licenseId: String,
    val image: Bitmap?,
    val date: String,
    val latitude: String,
    val longitude: String,
    val reporter: String,
    var message: String = ""
    )