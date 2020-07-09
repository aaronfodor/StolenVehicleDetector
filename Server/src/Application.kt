package com.arpadfodor.ktor

import com.arpadfodor.ktor.model.*
import io.ktor.application.*
import io.ktor.auth.Authentication
import io.ktor.auth.UserIdPrincipal
import io.ktor.auth.authenticate
import io.ktor.auth.basic
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.html.*
import kotlinx.html.*
import kotlinx.css.*
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.features.StatusPages
import io.ktor.request.receive
import java.lang.Exception

/**
 * Access: 127.0.0.1:8080
 */
fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {

    val serverName = "Stolen Vehicle Detector Server"
    val apiName = "API v1"
    val adminName = "Administrator API v1"

    val model = AppModel()

    val client = HttpClient(Apache) {
    }

    install(Authentication) {

        basic(name = "Administrator") {
            realm = adminName
            validate {

                if (model.validateAdmin(it.name, it.password)){
                    UserIdPrincipal(it.name)
                }
                else{
                    throw InvalidCredentialsException()
                }

            }
        }

        basic(name = "API User") {
            realm = apiName
            validate {

                if (model.validateApiUser(it.name, it.password)){
                    UserIdPrincipal(it.name)
                }
                else{
                    throw InvalidCredentialsException()
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

        exception<InvalidCredentialsException> { exception ->
            call.respond(HttpStatusCode.Unauthorized, mapOf("OK" to false, "error" to (exception.message ?: "")))
        }

        exception<NotFoundException> { exception ->
            call.respond(HttpStatusCode.NotFound, mapOf("OK" to false, "error" to (exception.message ?: "")))
        }

    }

    routing {

        get("") {

            call.respondHtml {
                body {
                    h1 { +serverName }
                }
            }
        }

        route("/api/v1"){

            get("") {

                call.respondHtml {
                    body {
                        h1 { +serverName }
                        h1 { +apiName }
                        p { +"HTTP Basic Auth is needed (as API user)." }
                        ul {
                            h2 { +"Get stolen vehicles" }
                            h3 { +"*/vehicles" }
                            h3 { +"*/vehicles/meta" }
                            li { +"Response type: json" }
                            li { +"Last update (UTC): ${model.stolenVehiclesTimeStamp()}" }
                            li { +"Number of stolen vehicles: ${model.numStolenVehicles()}" }
                            h2 { +"Get vehicle coordinates" }
                            h3 { +"*/coordinates" }
                            h3 { +"*/coordinates/meta" }
                            li { +"Response type: json" }
                            li { +"Last update (UTC): ${model.vehicleCoordinatesTimeStamp()}" }
                            li { +"Number of vehicles with coordinates: ${model.numVehicleCoordinates()}" }
                            h2 { +"Post vehicle report" }
                            h3 { +"*/report" }
                            li { +"Required payload: json" }
                            li { +"Last update (UTC): ${model.vehicleReportsTimeStamp()}" }
                            li { +"Number of reports: ${model.numVehicleReports()}" }
                        }
                    }
                }
            }

            authenticate(configurations = *arrayOf("API User")) {

                route("/vehicles"){

                    get("") {
                        var jsonContent = ""
                        try {
                            jsonContent = model.getStolenVehiclesAsJson()
                        }
                        catch (e: Exception){
                            throw InternalServerError()
                        }
                        call.respondText(jsonContent, contentType = ContentType.Text.JavaScript)
                    }

                    get("/meta") {
                        var jsonContent = ""
                        try {
                            jsonContent = model.getStolenVehiclesMetaAsJson()
                        }
                        catch (e: Exception){
                            throw InternalServerError()
                        }

                        call.respondText(jsonContent, contentType = ContentType.Text.JavaScript)
                    }

                }

                route("/coordinates"){

                    get("") {
                        var jsonContent = ""
                        try {
                            jsonContent =  model.getVehicleCoordinatesAsJson()
                        }
                        catch (e: Exception){
                            throw InternalServerError()
                        }

                        call.respondText(jsonContent, contentType = ContentType.Text.JavaScript)
                    }

                    get("/meta") {
                        var jsonContent = ""
                        try {
                            jsonContent = model.getVehicleCoordinatesMetaAsJson()
                        }
                        catch (e: Exception){
                            throw InternalServerError()
                        }

                        call.respondText(jsonContent, contentType = ContentType.Text.JavaScript)
                    }

                }

                post("/report"){

                    try{
                        val stringPayload = call.receive<String>()

                        if(model.addReport(stringPayload)){
                            call.respond(HttpStatusCode.Created, "CREATED")
                        }
                        else{
                            throw BadRequest()
                        }
                    }
                    catch (e: Exception){
                        throw BadRequest()
                    }

                }

            }

        }

        route("/admin-api/v1") {

            get("") {

                call.respondHtml {
                    body {
                        h1 { +serverName }
                        h1 { +adminName }
                        p { +"HTTP Basic Auth is needed (as administrator)." }
                        ul {
                            h2 { +"Put stolen vehicles list" }
                            h3 { +"*/vehicles" }
                            li { +"Rewrites existing stolen vehicles database." }
                            li { +"Required payload: json" }
                            li { +"Last update (UTC): ${model.stolenVehiclesTimeStamp()}" }
                            li { +"Number of stolen vehicles: ${model.numStolenVehicles()}" }
                            h2 { +"Delete stolen vehicles" }
                            h3 { +"*/vehicles" }
                            li { +"Deletes existing stolen vehicles, reports and coordinates "
                                    + "(because reports & coordinates requires stolen vehicles data)." }
                            h2 { +"Delete vehicle reports" }
                            h3{ +"*/reports" }
                            li { +"Deletes existing reports and coordinates "
                                    + "(because coordinates requires reports data)." }
                            h2 { +"Get users" }
                            h3{ +"*/users" }
                            h2 { +"Delete user" }
                            h3{ +"*/user?name={username_to_delete}" }
                            li { +"Removes a user by its name." }
                            h2 { +"Post user" }
                            h3{ +"*/user" }
                            li { +"Adds a new user if its name is not occupied." }
                        }
                    }
                }
            }

            authenticate(configurations = *arrayOf("Administrator")) {

                put("/vehicles"){

                    try{
                        val stringPayload = call.receive<String>()

                        if(model.rawStolenVehiclesToDatabase(stringPayload)){
                            call.respond(HttpStatusCode.Created, "CREATED")
                        }
                        else{
                            throw BadRequest()
                        }
                    }
                    catch (e: Exception){
                        throw BadRequest()
                    }

                }

                delete("/vehicles"){

                    try{
                        if(model.deleteVehicles() && model.deleteCoordinates() && model.deleteReports()){
                            call.respond(HttpStatusCode.OK, "OK")
                        }
                        else{
                            throw InternalServerError()
                        }
                    }
                    catch (e: Exception){
                        throw InternalServerError()
                    }

                }

                delete("/reports"){

                    try{
                        if(model.deleteReports() && model.deleteCoordinates()){
                            call.respond(HttpStatusCode.OK, "OK")
                        }
                        else{
                            throw InternalServerError()
                        }
                    }
                    catch (e: Exception){
                        throw InternalServerError()
                    }

                }

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

                    try{
                        val stringPayload = call.receive<String>()

                        if(model.addUser(stringPayload)){
                            call.respond(HttpStatusCode.Created, "CREATED")
                        }
                        else{
                            throw BadRequest()
                        }
                    }
                    catch (e: Exception){
                        throw BadRequest()
                    }

                }

                delete("/user"){

                    val queryParameters: Parameters = call.parameters
                    val nameToDelete: String = queryParameters["name"] ?: ""

                    try{
                        if(model.deleteUser(nameToDelete)){
                            call.respond(HttpStatusCode.OK, "OK")
                        }
                        else{
                            throw BadRequest()
                        }
                    }
                    catch (e: Exception){
                        throw BadRequest()
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
