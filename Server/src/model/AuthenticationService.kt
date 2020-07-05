package com.arpadfodor.ktor.model

import com.arpadfodor.ktor.model.dataclasses.User
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object AuthenticationService{

    private val dbPath = "resources/"
    private val dbName = "credentials.json"

    private var users: List<User> = emptyList()

    fun initialize(){
        val content = DataUtils.getFileContent("${dbPath}${dbName}")
        users = Gson().fromJson(content, object : TypeToken<List<User>>() {}.type)
    }

    fun validateCredentials(currentUser: User) : Boolean{
        for(user in users){
            if(user.name == currentUser.name){
                if(user.password == currentUser.password)
                    return (user.type == currentUser.type)
            }
        }
        return false
    }

}