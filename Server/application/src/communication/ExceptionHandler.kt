package com.arpadfodor.stolenvehicledetector.communication

import java.lang.RuntimeException

object ExceptionHandler {

    fun isNoException(statusCode: Int) : Boolean{
        return when(statusCode){
            StatusCodes.SUCCESS -> true
            else -> false
        }
    }

    fun throwAppropriateException(statusCode: Int) : Nothing{
        when(statusCode){
            StatusCodes.NOT_MODIFIED -> throw NotModified()
            StatusCodes.CONFLICT -> throw Conflict()
            StatusCodes.BAD_REQUEST -> throw BadRequest()
            StatusCodes.NOT_FOUND -> throw NotFound()
            StatusCodes.UNAUTHORIZED -> throw InvalidCredentials()
            StatusCodes.INTERNAL_SERVER_ERROR -> throw InternalServerError()
            else -> throw RuntimeException("$statusCode")
        }
    }

}