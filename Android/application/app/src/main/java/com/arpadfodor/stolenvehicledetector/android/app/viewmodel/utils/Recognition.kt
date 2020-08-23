package com.arpadfodor.stolenvehicledetector.android.app.viewmodel.utils

import android.graphics.Bitmap

data class Recognition(
    val artificialId: Int,
    var isSent: Boolean,
    val licenseId: String,
    val image: Bitmap?,
    val imagePath: String?,
    val date: String,
    val latitude: String,
    val longitude: String,
    val reporter: String,
    var message: String = ""
    )