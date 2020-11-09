package com.arpadfodor.stolenvehicledetector.model

import com.arpadfodor.stolenvehicledetector.communication.StatusCodes
import com.arpadfodor.stolenvehicledetector.data.dataclasses.PermissionType
import com.arpadfodor.stolenvehicledetector.data.dataclasses.User

class AuthService{

    fun authorizeUser(email: String, password: String, permission: String) : Int{

        val requiredPermission = when(permission){
            Interactor.PERMISSION_API_GET -> PermissionType.API_GET
            Interactor.PERMISSION_API_POST -> PermissionType.API_POST
            Interactor.PERMISSION_MODIFY_SELF -> PermissionType.MODIFY_SELF
            Interactor.PERMISSION_REGISTER -> PermissionType.REGISTER
            Interactor.PERMISSION_ADMIN -> PermissionType.ADMINISTRATOR
            else -> return StatusCodes.BAD_REQUEST
        }
        val userToValidate = User(email, password, "", "", true, 0, mutableListOf())

        return Interactor.userRepository.authorizeUser(userToValidate, requiredPermission)
    }

}