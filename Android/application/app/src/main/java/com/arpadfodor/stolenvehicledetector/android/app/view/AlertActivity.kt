package com.arpadfodor.stolenvehicledetector.android.app.view

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.arpadfodor.stolenvehicledetector.android.app.view.utils.RecognitionActivity
import com.arpadfodor.stolenvehicledetector.android.app.viewmodel.AlertViewModel

class AlertActivity : RecognitionActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(AlertViewModel::class.java)
    }

}
