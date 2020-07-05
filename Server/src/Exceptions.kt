package com.arpadfodor.ktor

/**
 * Parent exception indicating problems originated from the server.
 */
open class InternalServerError(message: String = "Internal server error.") : RuntimeException(message)

/**
 * Parent exception indicating problems originated from the client request.
 */
open class BadRequest(message: String = "Bad request.") : RuntimeException(message)

/**
 * Indicates that the user has not been authorized.
 */
class InvalidCredentialsException(message: String = "User has not been authorized.") : BadRequest(message)

/**
 * Indicates that the provided key is missing on the server.
 * For example, thrown when an Id/payload has not been found on the server.
 */
class NotFoundException(message: String = "Invalid Id has been provided or not found on the server.") : BadRequest(message)