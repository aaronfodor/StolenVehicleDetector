package com.arpadfodor.ktor

import com.arpadfodor.ktor.model.*
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
import java.lang.Exception
import java.text.DateFormat

/**
 * Access: 127.0.0.1:8080
 */
fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {

    val model = AppModel()

    val client = HttpClient(Apache) {
    }

    install(ContentNegotiation) {
        gson {
            setDateFormat(DateFormat.LONG)
            setPrettyPrinting()
        }
    }

    install(Authentication) {

        basic(name = AppModel.PERMISSION_MODIFY_SELF) {
            realm = AppModel.API_NAME
            validate {

                val statusCode = model.validateUser(it.name, it.password, AppModel.PERMISSION_MODIFY_SELF)
                if (ExceptionHandler.isNoException(statusCode)){
                    UserIdPrincipal(it.name)
                }
                else{
                    ExceptionHandler.throwAppropriateException(statusCode)
                }

            }
        }

        basic(name = AppModel.PERMISSION_ADMIN) {
            realm = AppModel.API_NAME
            validate {

                val statusCode = model.validateUser(it.name, it.password, AppModel.PERMISSION_ADMIN)
                if (ExceptionHandler.isNoException(statusCode)){
                    UserIdPrincipal(it.name)
                }
                else{
                    ExceptionHandler.throwAppropriateException(statusCode)
                }

            }
        }

        basic(name = AppModel.PERMISSION_API_GET) {
            realm = AppModel.API_NAME
            validate {

                val statusCode = model.validateUser(it.name, it.password, AppModel.PERMISSION_API_GET)
                if (ExceptionHandler.isNoException(statusCode)){
                    UserIdPrincipal(it.name)
                }
                else{
                    ExceptionHandler.throwAppropriateException(statusCode)
                }

            }
        }

        basic(name = AppModel.PERMISSION_API_POST) {
            realm = AppModel.API_NAME
            validate {

                val statusCode = model.validateUser(it.name, it.password, AppModel.PERMISSION_API_POST)
                if (ExceptionHandler.isNoException(statusCode)){
                    UserIdPrincipal(it.name)
                }
                else{
                    ExceptionHandler.throwAppropriateException(statusCode)
                }

            }
        }

        basic(name = AppModel.PERMISSION_REGISTER) {
            realm = AppModel.API_NAME
            validate {

                val statusCode = model.validateUser(it.name, it.password, AppModel.PERMISSION_REGISTER)
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
                    h1 { +AppModel.SERVER_NAME }
                }
            }
        }

        route("/api/v1"){

            get("") {

                call.respondHtml {
                    body {
                        h1 { +AppModel.SERVER_NAME }
                        h1 { +AppModel.API_NAME }
                        p { +"HTTP Basic Auth is used." }

                        h1 { +"Stolen vehicles" }
                        ul {
                            h2 { +"Get stolen vehicles" }
                            h3 { +"*/vehicles" }
                            h3 { +"*/vehicles/meta" }
                            li { +"Permission needed: ${AppModel.PERMISSION_API_GET}" }
                            li { +"Response type: json" }
                            h2 { +"Put stolen vehicles list" }
                            h3 { +"*/vehicles" }
                            li { +"Permission needed: ${AppModel.PERMISSION_ADMIN}" }
                            li { +"Rewrites existing stolen vehicles database." }
                            li { +"Required payload: json" }
                            h2 { +"Delete stolen vehicles" }
                            h3 { +"*/vehicles" }
                            li { +"Permission needed: ${AppModel.PERMISSION_ADMIN}" }
                            li { +"Deletes existing stolen vehicles, reports and coordinates "
                                + "(because reports & coordinates requires stolen vehicles data)." }
                        }

                        h1 { +"Coordinates" }
                        ul {
                            h2 { +"Get coordinates" }
                            h3 { +"*/coordinates" }
                            h3 { +"*/coordinates/meta" }
                            li { +"Permission needed: ${AppModel.PERMISSION_API_GET}" }
                            li { +"Response type: json" }
                        }

                        h1 { +"Reports" }
                        ul {
                            h2 { +"Get reports" }
                            h3{ +"*/reports" }
                            h3 { +"*/reports/meta" }
                            li { +"Permission needed: ${AppModel.PERMISSION_ADMIN}" }
                            li { +"Response type: json" }
                            h2 { +"Post report" }
                            h3 { +"*/report" }
                            li { +"Permission needed: ${AppModel.PERMISSION_API_POST}" }
                            li { +"Required payload: json" }
                            h2 { +"Delete reports" }
                            h3{ +"*/reports" }
                            li { +"Permission needed: ${AppModel.PERMISSION_ADMIN}" }
                            li { +"Deletes existing reports and coordinates "
                                + "(because coordinates requires reports data)." }
                        }

                        h1 { +"Self" }
                        ul {
                            h2 { +"Put current user" }
                            h3{ +"*/user/self" }
                            li { +"Permission needed: ${AppModel.PERMISSION_MODIFY_SELF}" }
                            li { +"Modifies the current user. "
                                +"Only password and name can be modified." }
                            h2 { +"Delete current user" }
                            h3{ +"*/user/self" }
                            li { +"Permission needed: ${AppModel.PERMISSION_MODIFY_SELF}" }
                            li { +"Deletes the current user." }
                        }

                        h1 { +"Users" }
                        ul {
                            h2 { +"Get users" }
                            h3{ +"*/users" }
                            h3 { +"*/users/meta" }
                            li { +"Permission needed: ${AppModel.PERMISSION_ADMIN}" }
                            li { +"Response type: json" }
                            h2 { +"Post new user" }
                            h3{ +"*/user" }
                            li { +"Permission needed: ${AppModel.PERMISSION_ADMIN}" }
                            li { +"Adds a new user if its email address is not occupied." }
                            h2 { +"Post new API user" }
                            h3{ +"*/api-user" }
                            li { +"Permission needed: ${AppModel.PERMISSION_REGISTER}" }
                            li { +"Registers a new user with ${AppModel.PERMISSION_API_GET}, ${AppModel.PERMISSION_API_POST}, ${AppModel.PERMISSION_MODIFY_SELF}"
                                + " permissions if its email address is not occupied." }
                            h2 { +"Put user" }
                            h3{ +"*/user" }
                            li { +"Permission needed: ${AppModel.PERMISSION_ADMIN}" }
                            li { +"Modifies existing User identified by its email address. "
                                +"Only the active flag and permissions can be modified. "
                                +"Note: a deactivated user cannot access the API & cannot delete itself." }
                            h2 { +"Delete user by email" }
                            h3{ +"*/user?email={user_email}" }
                            li { +"Permission needed: ${AppModel.PERMISSION_ADMIN}" }
                            li { +"Removes a user by its email address." }
                        }

                        h1 { +"Status" }
                        ul {
                            h2 { +"Vehicles" }
                            li { +"Last update (UTC): ${model.getTableModificationTimestamp(AppModel.VEHICLES)}" }
                            li { +"Number of stolen vehicles: ${model.getTableSize(AppModel.VEHICLES)}" }
                            h2 { +"Coordinates" }
                            li { +"Last update (UTC): ${model.getTableModificationTimestamp(AppModel.CURRENTS)}" }
                            li { +"Number of vehicles with coordinates: ${model.getTableSize(AppModel.CURRENTS)}" }
                            h2 { +"Reports" }
                            li { +"Last update (UTC): ${model.getTableModificationTimestamp(AppModel.REPORTS)}" }
                            li { +"Number of reports: ${model.getTableSize(AppModel.REPORTS)}" }
                            h2 { +"Users" }
                            li { +"Last update (UTC): ${model.getTableModificationTimestamp(AppModel.USERS)}" }
                            li { +"Number of users: ${model.getTableSize(AppModel.USERS)}" }
                        }
                    }
                }
            }

            authenticate(configurations = *arrayOf(AppModel.PERMISSION_API_GET)) {

                route("/vehicles"){

                    get("") {
                        var jsonContent = ""
                        try {
                            jsonContent = model.getDataAsJson(AppModel.VEHICLES)
                        }
                        catch (e: Exception){
                            throw InternalServerError()
                        }
                        call.respondText(jsonContent, contentType = ContentType.Text.JavaScript)
                    }

                    get("/meta") {
                        var jsonContent = ""
                        try {
                            jsonContent = model.getMetaDataAsJson(AppModel.VEHICLES)
                        }
                        catch (e: Exception){
                            throw InternalServerError()
                        }

                        call.respondText(jsonContent, contentType = ContentType.Text.JavaScript)
                    }

                }

            }

            authenticate(configurations = *arrayOf(AppModel.PERMISSION_ADMIN)) {

                put("/vehicles") {

                    val stringPayload = call.receive<String>()
                    val statusCode = model.rawStolenVehiclesToDatabase(stringPayload)
                    if (ExceptionHandler.isNoException(statusCode)) {
                        call.respond(HttpStatusCode.Created, "MODIFIED")
                    } else {
                        ExceptionHandler.throwAppropriateException(statusCode)
                    }

                }

                delete("/vehicles") {

                    val stringPayload = call.receive<String>()
                    val statusCode = model.deleteVehicles()
                    model.deleteCoordinates()
                    model.deleteReports()
                    if (ExceptionHandler.isNoException(statusCode)) {
                        call.respond(HttpStatusCode.OK, "DELETED")
                    } else {
                        ExceptionHandler.throwAppropriateException(statusCode)
                    }

                }

            }

            authenticate(configurations = *arrayOf(AppModel.PERMISSION_API_GET)) {

                route("/coordinates"){

                    get("") {
                        var jsonContent = ""
                        try {
                            jsonContent =  model.getDataAsJson(AppModel.CURRENTS)
                        }
                        catch (e: Exception){
                            throw InternalServerError()
                        }

                        call.respondText(jsonContent, contentType = ContentType.Text.JavaScript)
                    }

                    get("/meta") {
                        var jsonContent = ""
                        try {
                            jsonContent = model.getMetaDataAsJson(AppModel.CURRENTS)
                        }
                        catch (e: Exception){
                            throw InternalServerError()
                        }

                        call.respondText(jsonContent, contentType = ContentType.Text.JavaScript)
                    }

                }

            }

            authenticate(configurations = *arrayOf(AppModel.PERMISSION_API_POST)) {

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

            authenticate(configurations = *arrayOf(AppModel.PERMISSION_ADMIN)) {

                route("/reports"){

                    get("") {
                        var jsonContent = ""
                        try {
                            jsonContent =  model.getDataAsJson(AppModel.REPORTS)
                        }
                        catch (e: Exception){
                            throw InternalServerError()
                        }

                        call.respondText(jsonContent, contentType = ContentType.Text.JavaScript)
                    }

                    get("/meta") {
                        var jsonContent = ""
                        try {
                            jsonContent = model.getMetaDataAsJson(AppModel.REPORTS)
                        }
                        catch (e: Exception){
                            throw InternalServerError()
                        }

                        call.respondText(jsonContent, contentType = ContentType.Text.JavaScript)
                    }

                }

                delete("/reports"){

                    val stringPayload = call.receive<String>()
                    model.deleteReports()
                    val statusCode = model.deleteCoordinates()
                    if (ExceptionHandler.isNoException(statusCode)){
                        call.respond(HttpStatusCode.OK, "DELETED")
                    }
                    else{
                        ExceptionHandler.throwAppropriateException(statusCode)
                    }

                }

            }

            authenticate(configurations = *arrayOf(AppModel.PERMISSION_MODIFY_SELF)) {

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

            authenticate(configurations = *arrayOf(AppModel.PERMISSION_ADMIN)) {

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

            authenticate(configurations = *arrayOf(AppModel.PERMISSION_REGISTER)) {

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
