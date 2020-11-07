package com.arpadfodor.ktor

import com.arpadfodor.ktor.communication.*
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.html.*
import kotlinx.html.*
import kotlinx.css.*
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.features.ContentNegotiation
import io.ktor.features.StatusPages
import io.ktor.gson.gson
import io.ktor.request.receive
import com.arpadfodor.ktor.model.Interactor
import com.arpadfodor.ktor.model.AuthService
import java.lang.Exception
import java.text.DateFormat

/**
 * Access: 127.0.0.1:8080
 */
fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {

    val model = Interactor()
    val authenticator = AuthService()

    val client = HttpClient(Apache) {
    }

    install(ContentNegotiation) {
        gson {
            setDateFormat(DateFormat.LONG)
            setPrettyPrinting()
        }
    }

    install(Authentication) {

        basic(name = Interactor.PERMISSION_MODIFY_SELF) {
            realm = Interactor.API_NAME
            validate {

                val statusCode = authenticator.authorizeUser(it.name, it.password, Interactor.PERMISSION_MODIFY_SELF)
                if (ExceptionHandler.isNoException(statusCode)){
                    UserIdPrincipal(it.name)
                }
                else{
                    ExceptionHandler.throwAppropriateException(statusCode)
                }

            }
        }

        basic(name = Interactor.PERMISSION_ADMIN) {
            realm = Interactor.API_NAME
            validate {

                val statusCode = authenticator.authorizeUser(it.name, it.password, Interactor.PERMISSION_ADMIN)
                if (ExceptionHandler.isNoException(statusCode)){
                    UserIdPrincipal(it.name)
                }
                else{
                    ExceptionHandler.throwAppropriateException(statusCode)
                }

            }
        }

        basic(name = Interactor.PERMISSION_API_GET) {
            realm = Interactor.API_NAME
            validate {

                val statusCode = authenticator.authorizeUser(it.name, it.password, Interactor.PERMISSION_API_GET)
                if (ExceptionHandler.isNoException(statusCode)){
                    UserIdPrincipal(it.name)
                }
                else{
                    ExceptionHandler.throwAppropriateException(statusCode)
                }

            }
        }

        basic(name = Interactor.PERMISSION_API_POST) {
            realm = Interactor.API_NAME
            validate {

                val statusCode = authenticator.authorizeUser(it.name, it.password, Interactor.PERMISSION_API_POST)
                if (ExceptionHandler.isNoException(statusCode)){
                    UserIdPrincipal(it.name)
                }
                else{
                    ExceptionHandler.throwAppropriateException(statusCode)
                }

            }
        }

        basic(name = Interactor.PERMISSION_REGISTER) {
            realm = Interactor.API_NAME
            validate {

                val statusCode = authenticator.authorizeUser(it.name, it.password, Interactor.PERMISSION_REGISTER)
                if (ExceptionHandler.isNoException(statusCode)){
                    UserIdPrincipal(it.name)
                }
                else{
                    ExceptionHandler.throwAppropriateException(statusCode)
                }

            }
        }

    }

    install(StatusPages) {

        exception<InternalServerError> { exception ->
            call.respond(HttpStatusCode.InternalServerError, mapOf("OK" to false, "error" to (exception.message ?: "")))
        }
        exception<BadRequest> { exception ->
            call.respond(HttpStatusCode.BadRequest, mapOf("OK" to false, "error" to (exception.message ?: "")))
        }
        exception<InvalidCredentials> { exception ->
            call.respond(HttpStatusCode.Unauthorized, mapOf("OK" to false, "error" to (exception.message ?: "")))
        }
        exception<NotFound> { exception ->
            call.respond(HttpStatusCode.NotFound, mapOf("OK" to false, "error" to (exception.message ?: "")))
        }
        exception<NotModified> { exception ->
            call.respond(HttpStatusCode.NotModified, mapOf("OK" to false, "error" to (exception.message ?: "")))
        }
        exception<Conflict> { exception ->
            call.respond(HttpStatusCode.Conflict, mapOf("OK" to false, "error" to (exception.message ?: "")))
        }

    }

    routing {

        get("") {

            call.respondHtml {
                body {
                    h1 { +Interactor.SERVER_NAME }
                }
            }
        }

        route("/api/v1"){

            get("") {

                call.respondHtml {
                    body {
                        h1 { +Interactor.SERVER_NAME }
                        h1 { +Interactor.API_NAME }
                        p { +"HTTP Basic Auth is used." }

                        h1 { +"Stolen vehicles" }
                        ul {
                            h2 { +"Get vehicles" }
                            h3 { +"*/vehicles" }
                            h3 { +"*/vehicles/meta" }
                            li { +"Permission needed: ${Interactor.PERMISSION_API_GET}" }
                            li { +"Response type: json" }
                            h2 { +"Post load web-parsed vehicles" }
                            h3 { +"*/vehicles/load" }
                            li { +"Permission needed: ${Interactor.PERMISSION_ADMIN}" }
                            li { +"Loads vehicles from the web-parsed local file." }
                            h2 { +"Put vehicles list" }
                            h3 { +"*/vehicles" }
                            li { +"Permission needed: ${Interactor.PERMISSION_ADMIN}" }
                            li { +"Rewrites existing vehicles database." }
                            li { +"Required payload: json" }
                            h2 { +"Delete vehicles" }
                            h3 { +"*/vehicles" }
                            li { +"Permission needed: ${Interactor.PERMISSION_ADMIN}" }
                            li { +"Deletes existing vehicles, reports and report history "
                                + "(because reports & history requires vehicles data)." }
                        }

                        h1 { +"Reports" }
                        ul {
                            h2 { +"Get reports" }
                            h3 { +"*/reports" }
                            h3 { +"*/reports/meta" }
                            li { +"Permission needed: ${Interactor.PERMISSION_API_GET}" }
                            li { +"Response type: json" }
                            h2 { +"Post report" }
                            h3 { +"*/report" }
                            li { +"Permission needed: ${Interactor.PERMISSION_API_POST}" }
                            li { +"Required payload: json" }
                            h2 { +"Delete reports" }
                            h3{ +"*/reports" }
                            li { +"Permission needed: ${Interactor.PERMISSION_ADMIN}" }
                            li { +"Deletes existing reports." }
                        }

                        h1 { +"Self" }
                        ul {
                            h2 { +"Put current user" }
                            h3{ +"*/user/self" }
                            li { +"Permission needed: ${Interactor.PERMISSION_MODIFY_SELF}" }
                            li { +"Modifies the current user. "
                                +"Only password and name can be modified." }
                            h2 { +"Delete current user" }
                            h3{ +"*/user/self" }
                            li { +"Permission needed: ${Interactor.PERMISSION_MODIFY_SELF}" }
                            li { +"Deletes the current user." }
                        }

                        h1 { +"Users" }
                        ul {
                            h2 { +"Get users" }
                            h3{ +"*/users" }
                            h3 { +"*/users/meta" }
                            li { +"Permission needed: ${Interactor.PERMISSION_ADMIN}" }
                            li { +"Response type: json" }
                            h2 { +"Post new user" }
                            h3{ +"*/user" }
                            li { +"Permission needed: ${Interactor.PERMISSION_ADMIN}" }
                            li { +"Adds a new user if its email address is not occupied." }
                            h2 { +"Post new API user" }
                            h3{ +"*/api-user" }
                            li { +"Permission needed: ${Interactor.PERMISSION_REGISTER}" }
                            li { +"Registers a new user with ${Interactor.PERMISSION_API_GET}, ${Interactor.PERMISSION_API_POST}, ${Interactor.PERMISSION_MODIFY_SELF}"
                                + " permissions if its email address is not occupied." }
                            h2 { +"Put user" }
                            h3{ +"*/user" }
                            li { +"Permission needed: ${Interactor.PERMISSION_ADMIN}" }
                            li { +"Modifies existing User identified by its email address. "
                                +"Only the active flag and permissions can be modified. "
                                +"Note: a deactivated user cannot access the API & cannot delete itself." }
                            h2 { +"Delete user by email" }
                            h3{ +"*/user?email={user_email}" }
                            li { +"Permission needed: ${Interactor.PERMISSION_ADMIN}" }
                            li { +"Removes a user by its email address." }
                        }

                        h1 { +"Status" }
                        ul {
                            h2 { +"Vehicles" }
                            li { +"Last update (UTC): ${model.getTableModificationTimestamp(Interactor.VEHICLE)}" }
                            li { +"Number of stolen vehicles: ${model.getTableSize(Interactor.VEHICLE)}" }
                            h2 { +"Reports" }
                            li { +"Last update (UTC): ${model.getTableModificationTimestamp(Interactor.REPORT)}" }
                            li { +"Report number (distinct vehicles): ${model.getTableSize(Interactor.REPORT)}" }
                            h2 { +"Users" }
                            li { +"Last update (UTC): ${model.getTableModificationTimestamp(Interactor.USER)}" }
                            li { +"Number of users: ${model.getTableSize(Interactor.USER)}" }
                        }
                    }
                }
            }

            authenticate(configurations = *arrayOf(Interactor.PERMISSION_API_GET)) {

                route("/vehicles"){

                    get("") {
                        var jsonContent = ""
                        try {
                            jsonContent = model.getDataAsJson(Interactor.VEHICLE)
                        }
                        catch (e: Exception){
                            throw InternalServerError()
                        }
                        call.respondText(jsonContent, contentType = ContentType.Text.JavaScript)
                    }

                    get("/meta") {
                        var jsonContent = ""
                        try {
                            jsonContent = model.getMetaDataAsJson(Interactor.VEHICLE)
                        }
                        catch (e: Exception){
                            throw InternalServerError()
                        }

                        call.respondText(jsonContent, contentType = ContentType.Text.JavaScript)
                    }

                }

            }

            authenticate(configurations = *arrayOf(Interactor.PERMISSION_ADMIN)) {

                post("/vehicles/load") {

                    val statusCode = model.rawVehiclesFileToDatabase()
                    if (ExceptionHandler.isNoException(statusCode)) {
                        call.respond(HttpStatusCode.Created, "MODIFIED")
                    } else {
                        ExceptionHandler.throwAppropriateException(statusCode)
                    }

                }

                put("/vehicles") {

                    val stringPayload = call.receive<String>()
                    val statusCode = model.rawVehiclesToDatabase(stringPayload)
                    if (ExceptionHandler.isNoException(statusCode)) {
                        call.respond(HttpStatusCode.Created, "MODIFIED")
                    } else {
                        ExceptionHandler.throwAppropriateException(statusCode)
                    }

                }

                delete("/vehicles") {

                    val stringPayload = call.receive<String>()
                    val statusCode = model.deleteVehicles()
                    model.deleteReports()
                    if (ExceptionHandler.isNoException(statusCode)) {
                        call.respond(HttpStatusCode.OK, "DELETED")
                    } else {
                        ExceptionHandler.throwAppropriateException(statusCode)
                    }

                }

            }

            authenticate(configurations = *arrayOf(Interactor.PERMISSION_API_POST)) {

                post("/report") {

                    val stringPayload = call.receive<String>()
                    val email = call.principal<UserIdPrincipal>()?.name ?: ""
                    val statusCode = model.addReport(stringPayload, email)
                    if (ExceptionHandler.isNoException(statusCode)){
                        call.respond(HttpStatusCode.Created, "CREATED")
                    }
                    else{
                        ExceptionHandler.throwAppropriateException(statusCode)
                    }

                }

            }

            authenticate(configurations = *arrayOf(Interactor.PERMISSION_ADMIN)) {

                delete("/reports"){

                    val stringPayload = call.receive<String>()
                    val statusCode = model.deleteReports()
                    if (ExceptionHandler.isNoException(statusCode)){
                        call.respond(HttpStatusCode.OK, "DELETED")
                    }
                    else{
                        ExceptionHandler.throwAppropriateException(statusCode)
                    }

                }

            }

            authenticate(configurations = *arrayOf(Interactor.PERMISSION_MODIFY_SELF)) {

                put("/user/self"){

                    val email = call.principal<UserIdPrincipal>()?.name ?: ""

                    val stringPayload = call.receive<String>()
                    val statusCode = model.updateSelf(email, stringPayload)
                    if (ExceptionHandler.isNoException(statusCode)){
                        call.respond(HttpStatusCode.OK, "MODIFIED")
                    }
                    else{
                        ExceptionHandler.throwAppropriateException(statusCode)
                    }

                }

                delete("/user/self"){

                    val email = call.principal<UserIdPrincipal>()?.name ?: ""

                    val statusCode = model.deleteSelf(email)
                    if (ExceptionHandler.isNoException(statusCode)){
                        call.respond(HttpStatusCode.OK, "DELETED")
                    }
                    else{
                        ExceptionHandler.throwAppropriateException(statusCode)
                    }

                }

            }

            authenticate(configurations = *arrayOf(Interactor.PERMISSION_ADMIN)) {

                get("/users"){
                    var jsonContent = ""
                    try {
                        jsonContent =  model.getUsers()
                    }
                    catch (e: Exception){
                        throw InternalServerError()
                    }

                    call.respondText(jsonContent, contentType = ContentType.Text.JavaScript)
                }

                post("/user"){

                    val stringPayload = call.receive<String>()
                    val statusCode = model.addUser(stringPayload)
                    if (ExceptionHandler.isNoException(statusCode)){
                        call.respond(HttpStatusCode.Created, "CREATED")
                    }
                    else{
                        ExceptionHandler.throwAppropriateException(statusCode)
                    }

                }

                put("/user"){

                    val stringPayload = call.receive<String>()
                    val statusCode = model.updateUser(stringPayload)
                    if (ExceptionHandler.isNoException(statusCode)){
                        call.respond(HttpStatusCode.OK, "MODIFIED")
                    }
                    else{
                        ExceptionHandler.throwAppropriateException(statusCode)
                    }

                }

                delete("/user"){

                    val queryParameters: Parameters = call.parameters
                    val emailToDelete: String = queryParameters["email"] ?: ""

                    val stringPayload = call.receive<String>()
                    val statusCode = model.deleteUser(emailToDelete)
                    if (ExceptionHandler.isNoException(statusCode)){
                        call.respond(HttpStatusCode.OK, "DELETED")
                    }
                    else{
                        ExceptionHandler.throwAppropriateException(statusCode)
                    }

                }

            }

            authenticate(configurations = *arrayOf(Interactor.PERMISSION_REGISTER)) {

                post("/api-user"){

                    val stringPayload = call.receive<String>()
                    val statusCode = model.addApiUser(stringPayload)
                    if (ExceptionHandler.isNoException(statusCode)){
                        call.respond(HttpStatusCode.Created, "CREATED")
                    }
                    else{
                        ExceptionHandler.throwAppropriateException(statusCode)
                    }

                }

            }

        }

        get("/styles.css") {
            call.respondCss {
                body {
                    backgroundColor = Color.red
                }
                p {
                    fontSize = 2.em
                }
                rule("p.myclass") {
                    color = Color.blue
                }
            }
        }

    }

}

fun FlowOrMetaDataContent.styleCss(builder: CSSBuilder.() -> Unit) {
    style(type = ContentType.Text.CSS.toString()) {
        +CSSBuilder().apply(builder).toString()
    }
}

fun CommonAttributeGroupFacade.style(builder: CSSBuilder.() -> Unit) {
    this.style = CSSBuilder().apply(builder).toString().trim()
}

suspend inline fun ApplicationCall.respondCss(builder: CSSBuilder.() -> Unit) {
    this.respondText(CSSBuilder().apply(builder).toString(), ContentType.Text.CSS)
}
