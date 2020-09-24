package com.arpadfodor.stolenvehicledetector.android.app.viewmodel

import androidx.lifecycle.MutableLiveData
import com.arpadfodor.stolenvehicledetector.android.app.model.AccountService
import com.arpadfodor.stolenvehicledetector.android.app.model.repository.UserRecognitionRepository
import com.arpadfodor.stolenvehicledetector.android.app.viewmodel.utils.AppViewModel

class AccountViewModel : AppViewModel(){

    /**
     * TAG of the fragment to show
     **/
    val fragmentTagToShow: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    fun isCurrentAccountGuest() : Boolean {
        return AccountService.isCurrentAccountGuest()
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