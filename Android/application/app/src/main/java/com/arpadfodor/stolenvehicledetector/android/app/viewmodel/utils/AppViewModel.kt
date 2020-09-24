package com.arpadfodor.stolenvehicledetector.android.app.viewmodel.utils

import androidx.lifecycle.ViewModel
import com.arpadfodor.stolenvehicledetector.android.app.model.AccountService

open class AppViewModel : ViewModel(){

    fun getCurrentUserName() : String {
        return AccountService.getDisplayUserName()
    }

    fun getCurrentUserEmail() : String {
        return AccountService.getDisplayUserEmail()
    }

}