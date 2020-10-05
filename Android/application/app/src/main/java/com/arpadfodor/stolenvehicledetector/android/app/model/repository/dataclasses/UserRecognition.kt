package com.arpadfodor.stolenvehicledetector.android.app.model.repository.dataclasses

import android.graphics.Bitmap

data class UserRecognition(
    val artificialId: Int,
    var isSelected: Boolean,
    var isSent: Boolean,
    val isAlert: Boolean,
    val licenseId: String,
    val image: Bitmap?,
    val date: String,
    val latitude: String,
    val longitude: String,
    val reporter: String,
    var message: String = ""
    )