package com.arpadfodor.ktor.model

import com.arpadfodor.ktor.communication.StatusCodes
import com.arpadfodor.ktor.model.dataclasses.PermissionType
import com.arpadfodor.ktor.model.dataclasses.User

class AuthenticatorService{

    fun validateUser(email: String, password: String, permission: String) : Int{
        val permissionToValidate = when(permission){
            Interactor.PERMISSION_API_GET -> PermissionType.API_GET
            Interactor.PERMISSION_API_POST -> PermissionType.API_POST
            Interactor.PERMISSION_MODIFY_SELF -> PermissionType.MODIFY_SELF
            Interactor.PERMISSION_REGISTER -> PermissionType.REGISTER
            Interactor.PERMISSION_ADMIN -> PermissionType.ADMINISTRATOR
            else -> return StatusCodes.BAD_REQUEST
        }
        val userToValidate = User(email, "", password, "", true, 0, mutableListOf(permissionToValidate))
        return Interactor.usersDAO.validateCredentials(userToValidate)
    }

}