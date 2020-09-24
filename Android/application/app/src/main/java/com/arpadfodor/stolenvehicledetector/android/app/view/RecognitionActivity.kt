package com.arpadfodor.stolenvehicledetector.android.app.view

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.arpadfodor.stolenvehicledetector.android.app.R
import com.arpadfodor.stolenvehicledetector.android.app.view.utils.MasterDetailActivity
import com.arpadfodor.stolenvehicledetector.android.app.viewmodel.RecognitionViewModel

class RecognitionActivity : MasterDetailActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(this).get(RecognitionViewModel::class.java)
        (viewModel as RecognitionViewModel).updateDataFromDb()
        super.onCreate(savedInstanceState)

        listName = getString(R.string.user_recognition_list)
        detailName = getString(R.string.user_recognition_details)

        sendSucceed = getString(R.string.recognition_sent)
        sendFailed = getString(R.string.recognition_sending_failed)
        deleted = getString(R.string.deleted)
        deleteFailed = getString(R.string.delete_failed)
        alreadySent = getString(R.string.recognition_already_sent)
        updateSucceed = getString(R.string.updated)
        updateFailed = getString(R.string.update_failed)

    }

    override fun onResume() {
        super.onResume()
        (viewModel as RecognitionViewModel).updateDataFromDb()
    }

}
