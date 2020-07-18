package com.arpadfodor.ktor.model

import com.arpadfodor.ktor.StatusCodes
import com.arpadfodor.ktor.model.dataclasses.User
import com.google.gson.reflect.TypeToken

private val typeToken = object : TypeToken<MutableList<User>>() {}.type

class UsersDAO(name: String,  metaDAO: MetaDAO?) : DAO<User>(name, typeToken, metaDAO){

    override fun read() : MutableList<User> {
        val content = records
        val displayedUsers = mutableListOf<User>()
        for(user in records){
            displayedUsers.add(User(user.email, user.name, "******", "******", user.active, user.permissions))
        }
        return displayedUsers
    }

    override fun add(newUser: User) : Int{

        if(!newUser.email.contains("@") || !newUser.email.contains(".")){
            return StatusCodes.BAD_REQUEST
        }
        if(newUser.password == ""){
            return StatusCodes.BAD_REQUEST
        }
        if(!newUser.active || newUser.permissions.isEmpty()){
            return StatusCodes.BAD_REQUEST
        }

        for(user in records){
            if(user.email == newUser.email){
                return StatusCodes.CONFLICT
            }
        }

        records.add(newUser)
        return write(records)

    }

    fun delete(key: String) : Int{
        val updatedUsers = records.filter { it.email != key }.toMutableList()
        records = updatedUsers
        return write(records)
    }

    fun setUserActiveFlag(key: String, value: Boolean) : Int{
        for(user in records){
            if(user.email == key){
                user.active = value
                return write(records)
            }
        }
        return StatusCodes.NOT_FOUND
    }

    fun updateUser(userToUpdate: User) : Int{
        for(user in records){
            if(user.email == userToUpdate.email){
                user.active = userToUpdate.active
                user.permissions = userToUpdate.permissions
            }
        }
        return write(records)
    }

    fun deleteSelf(key: String) : Int{
        for(user in records){
            if(user.email != key) continue
            // if User has been deactivated, it cannot delete itself
            if(!user.active) continue
            return delete(key)
        }
        return StatusCodes.NOT_MODIFIED
    }

    fun updateSelf(key: String, userToUpdate: User) : Int{
        for(user in records){
            if(user.email == key){
                user.name = userToUpdate.name
                user.password = userToUpdate.password
            }
        }
        return write(records)
    }

    fun validateCredentials(userWithRequiredPermission: User) : Int{

        for(user in records){

            if(user.email != userWithRequiredPermission.email) continue
            if(!user.active) continue
            if(user.password != userWithRequiredPermission.password) return StatusCodes.UNAUTHORIZED

            // get the user permission to validate
            val currentUserPermission = userWithRequiredPermission.permissions[0]
            for(permission in user.permissions){
                if(currentUserPermission == permission){
                    return StatusCodes.SUCCESS
                }
            }

        }

        return StatusCodes.UNAUTHORIZED

    }

}