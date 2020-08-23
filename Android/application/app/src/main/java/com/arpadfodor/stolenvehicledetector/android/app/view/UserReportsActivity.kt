package com.arpadfodor.stolenvehicledetector.android.app.view

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.arpadfodor.stolenvehicledetector.android.app.R
import com.arpadfodor.stolenvehicledetector.android.app.view.utils.RecognitionActivity
import com.arpadfodor.stolenvehicledetector.android.app.viewmodel.UserReportViewModel

class UserReportsActivity : RecognitionActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(UserReportViewModel::class.java)
        (viewModel as UserReportViewModel).updateDataFromDb()

        listName = getString(R.string.user_reports_list)
        detailName = getString(R.string.user_report_details)

        sendSucceed = getString(R.string.report_sent)
        sendFailed = getString(R.string.report_sending_failed)
        deleted = getString(R.string.report_deleted)
        alreadySent = getString(R.string.report_already_sent)
        updateSucceed = getString(R.string.report_updated)
        updateFailed = getString(R.string.report_update_failed)

    }

    override fun onResume() {
        super.onResume()
        (viewModel as UserReportViewModel).updateDataFromDb()
    }

}
