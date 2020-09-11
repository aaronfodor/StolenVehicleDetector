package com.arpadfodor.stolenvehicledetector.android.app.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.arpadfodor.stolenvehicledetector.android.app.model.AccountService

class AccountViewModel : ViewModel(){

    /**
     * Whether the permissions has been granted
     **/
    val hasPermissionsGranted: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    /**
     * TAG of the fragment to show
     **/
    val fragmentTagToShow: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    fun isCurrentUserUnique() : Boolean {
        return AccountService.isCurrentUserUnique()
    }

    fun getAccountName() : String {
        return AccountService.userDisplayName
    }

    fun getAccountEmail() : String {
        return AccountService.userId
    }

}