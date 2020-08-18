package com.arpadfodor.stolenvehicledetector.android.app.view

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.arpadfodor.stolenvehicledetector.android.app.R
import com.arpadfodor.stolenvehicledetector.android.app.view.utils.RecognitionActivity
import com.arpadfodor.stolenvehicledetector.android.app.viewmodel.UserReportsViewModel

class UserReportsActivity : RecognitionActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(UserReportsViewModel::class.java)
        (viewModel as UserReportsViewModel).update()

        listName = getString(R.string.user_reports_list)
        detailName = getString(R.string.user_report_details)

    }

}
