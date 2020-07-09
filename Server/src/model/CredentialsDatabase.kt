package com.arpadfodor.ktor.model

import com.arpadfodor.ktor.model.dataclasses.User
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

object CredentialsDatabase{

    private val dbPath = "resources/"
    private val dbName = "credentials.json"

    private var users: MutableList<User> = mutableListOf()

    fun initialize(){
        users = read()
    }

    private fun read() : MutableList<User> {
        val content = DataUtils.getFileContent("${dbPath}${dbName}")
        val userData: MutableList<User> = Gson().fromJson(content, object : TypeToken<List<User>>() {}.type)
                ?: mutableListOf()
        return userData
    }

    private fun write(users: List<User>){
        val jsonContent = DataTransformer.objectToJsonString(users)
        File("${dbPath}${dbName}").writeText(jsonContent)
    }

    fun getUsers() : MutableList<User> {
        val displayedUsers = mutableListOf<User>()
        for(user in users){
            displayedUsers.add(User(user.name, "******", user.types))
        }
        return displayedUsers
    }

    fun addUser(newUser: User) : Boolean{
        for(user in users){
            if(user.name == newUser.name){
                return false
            }
        }
        users.add(newUser)
        write(users)
        return true
    }

    fun deleteUser(userNameToDelete: String) : Boolean{
        val updatedUsers = users.filter { it.name != userNameToDelete }.toMutableList()
        users = updatedUsers
        write(users)
        return true
    }

    fun validateCredentials(currentUser: User) : Boolean{

        for(user in users){

            if(user.name == currentUser.name){

                if(user.password == currentUser.password){
                    // get the user type to validate
                    val currentUserType = currentUser.types[0]
                    for(type in user.types){
                        if(currentUserType == type){
                            return true
                        }
                    }

                }

            }

        }

        return false

    }

}