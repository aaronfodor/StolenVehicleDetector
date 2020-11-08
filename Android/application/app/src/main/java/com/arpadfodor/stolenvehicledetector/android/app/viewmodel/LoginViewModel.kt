package com.arpadfodor.stolenvehicledetector.android.app.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.arpadfodor.stolenvehicledetector.android.app.model.AccountService
import com.arpadfodor.stolenvehicledetector.android.app.model.repository.UserRecognitionRepository

class LoginViewModel : ViewModel(){

    /**
     * TAG of the fragment to show
     **/
    val fragmentTagToShow: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    fun login(email: String, name: String, password: String, rememberAccount: Boolean, success: () -> Unit, error: () -> Unit){
        AccountService.rememberAccount = rememberAccount
        AccountService.login(email, name, password, success, error)
    }

    fun loginAsGuest(success: () -> Unit, error: () -> Unit){
        AccountService.loginAsGuest(success, error)
    }

    fun tryAutoLogin(success: () -> Unit, error: () -> Unit){
        AccountService.tryAutoLogin(success, error)
    }

    fun forgotPassword(email: String, success: () -> Unit, error: () -> Unit){
        AccountService.sendPasswordToEmail(email, success, error)
    }

    fun registerAccount(email: String, name: String,  password: String, rememberAccount:
    Boolean, success: () -> Unit, error: () -> Unit){
        AccountService.registerAccount(email, name, password, success, error)
    }

}