package com.arpadfodor.stolenvehicledetector.android.app.model

object AccountService {

    var userId = ""
    var userDisplayName = ""
    private var userPassword = ""

    private var isUniqueAccountLoggedIn = false

    fun login(email: String, password: String, rememberAccount: Boolean, success: () -> Unit, error: () -> Unit){

        //TODO: persist if remember account set

        tryLogin(email, password,
            success = {
                isUniqueAccountLoggedIn = true
                success()
            },
            error = {
                isUniqueAccountLoggedIn = false
                error()
            })

    }

    fun logout(success: () -> Unit, error: () -> Unit){

        userDisplayName = ""
        userId = ""
        userPassword = ""

        success()

    }

    fun loginAsGuest(success: () -> Unit, error: () -> Unit){

        userDisplayName = "Default User"
        userId = "default_user@stolen_vehicle_detector"
        userPassword = "default_user_czka84"

        isUniqueAccountLoggedIn = false
        success()

    }

    fun registerAccount(name: String, email: String, password: String, rememberAccount: Boolean, success: () -> Unit, error: () -> Unit){

        //TODO: API create account call

        login(email, password, rememberAccount, success, error)

    }

    fun deleteAccount(password: String, success: () -> Unit, error: () -> Unit){

        if(password != userPassword || !isUniqueAccountLoggedIn){
            error()
            return
        }

        //TODO: API delete call

        logout(success, error)

    }

    fun changeAccountPassword(currentPassword: String, newPassword: String, success: () -> Unit, error: () -> Unit){

        if(currentPassword == newPassword || userPassword != currentPassword){
            error()
            return
        }

        //TODO: API change password call

        login(userId, newPassword, false, success, error)

    }

    private fun tryLogin(email: String, password: String, success: () -> Unit, error: () -> Unit){

        //TODO: API login logic

        userDisplayName = "unique user"
        userId = email
        userPassword = password

        success()

    }

    fun isCurrentUserUnique() : Boolean{
        return isUniqueAccountLoggedIn
    }

}