package com.arpadfodor.ktor.data

import com.arpadfodor.ktor.communication.StatusCodes
import com.arpadfodor.ktor.data.dataclasses.User
import com.google.gson.reflect.TypeToken
import com.arpadfodor.ktor.data.dataclasses.PermissionType

private val typeToken = object : TypeToken<MutableList<User>>() {}.type

class UsersRepository(name: String, metaDAO: MetaDAO) : DataclassDAO<User>(name, typeToken, metaDAO){

    override fun read() : MutableList<User> {
        val displayedUsers = mutableListOf<User>()
        for(user in records){
            val displayUser = User(user.email, "******", user.name, "******", user.active,
                    user.numReports, user.permissions, user.key, user.version, user.validFromUTC, user.validToUTC)
            displayedUsers.add(displayUser)
        }
        return displayedUsers
    }

    override fun add(items: List<User>) : Int{

        for(item in items){

            if(!item.email.contains("@") || !item.email.contains(".")){
                return StatusCodes.BAD_REQUEST
            }
            if(item.password == ""){
                return StatusCodes.BAD_REQUEST
            }
            if(!item.active || item.permissions.isEmpty()){
                return StatusCodes.BAD_REQUEST
            }

            for(record in records){
                if(record.email == item.email){
                    return StatusCodes.CONFLICT
                }
            }

        }

        return super.add(items)
    }

    fun deleteSelf(key: String, currentTimeUTC: String) : Int{
        for(user in records){
            if(user.email != key) continue
            // if User has been deactivated, it cannot delete itself
            if(!user.active) continue
            return delete(key, currentTimeUTC)
        }
        return StatusCodes.NOT_MODIFIED
    }

    fun updateUser(items: List<User>): Int {

        val areSameItems: (User, User) -> Boolean = {record, item ->
            (record.email == item.email)
        }

        val isUpdateNeeded: (User, User) -> Boolean = {record, item ->
            true
        }

        val updateRecordBasedOnItem: (User, User) -> Unit = {record, item ->
            record.active = item.active
            record.permissions = item.permissions
        }

        return super.update(items, areSameItems = areSameItems, isUpdateNeeded = isUpdateNeeded,
                updateRecordBasedOnItem = updateRecordBasedOnItem)
    }

    fun updateSelf(userToUpdate: User): Int {

        val areSameItems: (User, User) -> Boolean = {record, item ->
            (record.email == item.email)
        }

        val isUpdateNeeded: (User, User) -> Boolean = {record, item ->
            true
        }

        val updateRecordBasedOnItem: (User, User) -> Unit = {record, item ->
            record.name = userToUpdate.name
            record.password = userToUpdate.password
        }

        return super.update(listOf(userToUpdate), areSameItems = areSameItems, isUpdateNeeded = isUpdateNeeded,
                updateRecordBasedOnItem = updateRecordBasedOnItem)
    }

    fun authorizeUser(user: User, requiredPermission: PermissionType) : Int{

        for(record in records){

            if(record.email != user.email) continue
            if(!record.active) continue
            if(record.password != user.password) return StatusCodes.UNAUTHORIZED

            for(permission in record.permissions){
                if(requiredPermission == permission){
                    return StatusCodes.SUCCESS
                }
            }

        }

        return StatusCodes.UNAUTHORIZED
    }

    fun clearReportCounters() : Int{
        for(user in records){
            user.numReports = 0
        }
        return write(records)
    }

    fun increaseReportCounterOfUser(key: String) : Int{
        for(user in records){
            if(user.email == key){
                user.numReports += 1
                break
            }
        }
        return write(records)
    }

}