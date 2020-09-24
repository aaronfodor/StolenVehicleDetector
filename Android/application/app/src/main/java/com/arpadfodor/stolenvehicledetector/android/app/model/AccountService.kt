package com.arpadfodor.stolenvehicledetector.android.app.model

import com.arpadfodor.stolenvehicledetector.android.app.model.repository.UserRepository
import com.arpadfodor.stolenvehicledetector.android.app.model.repository.dataclasses.User

object AccountService {

    private const val DEFAULT_USER_ID = "default_user@stolen_vehicle_detector"
    private const val DEFAULT_USER_NAME = "Default User"
    private const val DEFAULT_USER_PASSWORD = "default_user_czka84"

    var userId = ""
    var userDisplayName = ""
    private var userPassword = ""

    private var isCurrentAccountGuest = true

    fun login(email: String, name: String, password: String, rememberAccount: Boolean, success: () -> Unit, error: () -> Unit){

        tryLogin(email, name, password,
            success = {

                if(rememberAccount){
                    val userToSave = User(email, name, password, "", 0)
                    UserRepository.saveUser(userToSave, {})
                }

                success()

            },
            error = {
                error()
            })

    }

    fun logout(success: () -> Unit, error: () -> Unit){

        userId = ""
        userDisplayName = ""
        userPassword = ""

        isCurrentAccountGuest = true

        UserRepository.deleteUser{ isSuccess ->
            if(isSuccess){
                success()
            }
            else{
                error()
            }
        }

    }

    fun loginAsGuest(success: () -> Unit, error: () -> Unit){

        userId = DEFAULT_USER_ID
        userDisplayName = DEFAULT_USER_NAME
        userPassword = DEFAULT_USER_PASSWORD

        isCurrentAccountGuest = true
        success()

    }

    fun tryAutoLogin(success: () -> Unit, error: () -> Unit){

        UserRepository.getUser { user ->

            if(user == null){
                isCurrentAccountGuest = true
                error()
            }
            else{
                tryLogin(user.email, user.name, user.password, success, error)
            }

        }

    }

    fun sendPasswordToEmail(email: String, success: () -> Unit, error: () -> Unit){

        //TODO: API send password to email
        
        success()

    }

    fun registerAccount(email: String, name: String, password: String, rememberAccount: Boolean, success: () -> Unit, error: () -> Unit){

        //TODO: API create account call

        login(email, name, password, rememberAccount, success, error)

    }

    fun deleteAccount(success: () -> Unit, error: () -> Unit){
        //TODO: API delete call
        logout(success, error)
    }

    fun changeAccount(newName: String = "", newPassword: String = "", success: () -> Unit, error: () -> Unit){

        var nameToSet = userDisplayName
        var passwordToSet = userPassword

        if(newName.isNotEmpty()){
            nameToSet = newName
        }

        if(newPassword.isNotEmpty()){
            passwordToSet = newPassword
        }

        //TODO: API change call
        login(userId, nameToSet, passwordToSet, false, success, error)

    }

    private fun tryLogin(email: String, name: String, password: String, success: () -> Unit, error: () -> Unit){

        //TODO: API login logic

        userId = email
        userDisplayName = name
        userPassword = password

        isCurrentAccountGuest = false
        success()

    }

    fun isCurrentAccountGuest() : Boolean{
        return isCurrentAccountGuest
    }

    fun getDisplayUserName() : String{

        return if(userDisplayName == DEFAULT_USER_NAME){
            ""
        }
        else{
            userDisplayName
        }

    }

    fun getDisplayUserEmail() : String{

        return if(userId == DEFAULT_USER_ID){
            ""
        }
        else{
            userId
        }

    }

}