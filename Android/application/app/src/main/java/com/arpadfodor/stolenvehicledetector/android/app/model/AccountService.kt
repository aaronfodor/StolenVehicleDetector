package com.arpadfodor.stolenvehicledetector.android.app.model

import com.arpadfodor.stolenvehicledetector.android.app.model.api.ApiService
import com.arpadfodor.stolenvehicledetector.android.app.model.api.BasicAuthInterceptor
import com.arpadfodor.stolenvehicledetector.android.app.model.repository.UserRepository
import com.arpadfodor.stolenvehicledetector.android.app.model.repository.dataclasses.User
import okhttp3.OkHttpClient

object AccountService {

    private const val DEFAULT_USER_ID = "default_user@stolen_vehicle_detector"
    private const val DEFAULT_USER_NAME = "Default User"
    private const val DEFAULT_USER_PASSWORD = "default_user_czka84"

    var userId = DEFAULT_USER_ID
    var userDisplayName = DEFAULT_USER_NAME
    private var userPassword = DEFAULT_USER_PASSWORD

    private var isCurrentAccountGuest = true
    var rememberAccount = false

    fun getClient() : OkHttpClient{
        val httpClient = OkHttpClient.Builder().addInterceptor(
            BasicAuthInterceptor(userId, userPassword)
        ).build()
        return httpClient
    }

    fun login(email: String, name: String, password: String, success: () -> Unit, error: () -> Unit){

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

    fun registerAccount(email: String, name: String, password: String, success: () -> Unit, error: () -> Unit){

        login(DEFAULT_USER_ID, DEFAULT_USER_NAME, DEFAULT_USER_PASSWORD, success={

            ApiService.postApiUser(email, name, password, success={
                login(email, name, password, success, error)
            }, error=error)

        }, error=error)

    }

    fun deleteAccount(success: () -> Unit, error: () -> Unit){
        ApiService.deleteSelf(success={
            logout(success, error)
        }, error=error)
    }

    fun changeAccount(nameToSet: String = userDisplayName, passwordToSet: String = userPassword, success: () -> Unit, error: () -> Unit){
        ApiService.putSelf(userId, nameToSet, passwordToSet, success={
            login(userId, nameToSet, passwordToSet, success, error)
        }, error=error)
    }

    private fun tryLogin(email: String, name: String, password: String, success: () -> Unit, error: () -> Unit){

        userId = email
        userDisplayName = name
        userPassword = password
        isCurrentAccountGuest = false

        ApiService.initialize(getClient())
        ApiService.login(success, error)

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