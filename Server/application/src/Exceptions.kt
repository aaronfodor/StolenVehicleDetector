package com.arpadfodor.ktor

/**
 * Parent exception indicating problems originated from the client request.
 * HTTP 400
 */
open class BadRequest(message: String = "Bad request.") : RuntimeException(message)

/**
 * Indicates that the user has not been authorized.
 * HTTP 401
 */
class InvalidCredentials(message: String = "User has not been authorized.") : BadRequest(message)

/**
 * Indicates that the provided key is missing on the server.
 * For example, thrown when an Id/payload has not been found on the server.
 * HTTP 404
 */
class NotFound(message: String = "Invalid key has been provided / element not found on the server.") : BadRequest(message)

/**
 * Indicates that the provided element already exists on the server.
 * HTTP 409
 */
class Conflict(message: String = "Conflict: already exists.") : BadRequest(message)

/**
 * Indicates that nothing happened on the server.
 * HTTP 304
 */
class NotModified(message: String = "Nothing has been modified.") : RuntimeException(message)

/**
 * Parent exception indicating problems originated from the server.
 * HTTP 500
 */
class InternalServerError(message: String = "Internal server error.") : RuntimeException(message)