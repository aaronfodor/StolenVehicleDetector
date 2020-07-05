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
    val apiName = "Stolen Vehicle Detector API v1"

    val model = AppModel()

    val client = HttpClient(Apache) {
    }

    install(Authentication) {

        basic(name = "Administrator") {
            realm = serverName
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
    /*install(ContentNegotiation) {
        gson {
            setPrettyPrinting()
        }
    }*/

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
                        h1 { +apiName }
                        ul {
                            h2 { +"Get stolen vehicles" }
                            p { +"*/vehicles" }
                            li { +"Response type: json" }
                            li { +"Last update (UTC): ${model.stolenVehiclesTimeStamp()}" }
                            li { +"Number of stolen vehicles: ${model.numStolenVehicles()}" }
                            h2 { +"Get vehicle coordinates" }
                            p { +"*/coordinates" }
                            li { +"Response type: json" }
                            li { +"Last update (UTC): ${model.vehicleCoordinatesTimeStamp()}" }
                            li { +"Number of vehicles with coordinates: ${model.numVehicleCoordinates()}" }
                            h2 { +"Post vehicle report" }
                            p { +"*/report" }
                            li { +"HTTP Basic Auth needed" }
                            li { +"Required payload: json" }
                            li { +"Last update (UTC): ${model.vehicleReportsTimeStamp()}" }
                            li { +"Number of reports: ${model.numVehicleReports()}" }
                        }
                    }
                }
            }

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

                get("/timestamp") {

                    var jsonContent = ""
                    try {
                        jsonContent = model.getStolenVehiclesAsJson()
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

            }

            authenticate(configurations = *arrayOf("API User")) {

                post("/report"){

                    try{

                        val stringPayload = call.receive<String>()

                        if(model.addReport(stringPayload)){
                            call.respond(HttpStatusCode.Created, "OK")
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

        route("/admin") {

            authenticate(configurations = *arrayOf("Administrator")) {

                get("") {

                    call.respondHtml {
                        body {
                            h1 { +"Administrator page" }
                        }
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
