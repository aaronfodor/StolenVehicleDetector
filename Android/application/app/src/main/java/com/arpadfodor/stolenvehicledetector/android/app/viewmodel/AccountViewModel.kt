package com.arpadfodor.stolenvehicledetector.android.app.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.arpadfodor.stolenvehicledetector.android.app.model.AccountService
import com.arpadfodor.stolenvehicledetector.android.app.model.repository.UserRecognitionRepository

class AccountViewModel : ViewModel(){

    /**
     * TAG of the fragment to show
     **/
    val fragmentTagToShow: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    fun isCurrentAccountGuest() : Boolean {
        return AccountService.isCurrentAccountGuest()
    }

    fun getAccountName() : String {
        return AccountService.userDisplayName
    }

    fun getAccountEmail() : String {
        return AccountService.userId
    }

    fun logout(success: () -> Unit, error: () -> Unit){
        AccountService.logout(success, error)
    }

    fun deleteAccount(success: () -> Unit, error: () -> Unit){

        AccountService.deleteAccount(
            success = {
            UserRecognitionRepository.deleteAllFromUser(AccountService.userId){}
            success()
            },
            error = error)

    }

    fun editAccount(newName: String, newPassword: String, success:() -> Unit, error:() -> Unit){
        AccountService.changeAccount(newName, newPassword, success, error)
    }

}