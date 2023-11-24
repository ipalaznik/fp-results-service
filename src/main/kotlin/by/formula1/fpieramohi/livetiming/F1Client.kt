package by.formula1.fpieramohi.livetiming

import by.formula1.fpieramohi.livetiming.dto.NegotiateResponse
import by.formula1.fpieramohi.telegram.dto.mapTimingToResultRows
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import java.time.LocalDateTime
import kotlin.time.Duration.Companion.seconds

private val logger = KotlinLogging.logger {}

data class Timeline (
    val data: String,
    val timestamp: LocalDateTime
)

fun subscribeToF1StreamingData() {
    val client = createHttpClient()

    runBlocking {
        val shouldReconnect = true
        while (shouldReconnect) {
            try {
                setupWebsocket(client)
            } catch (e: NoTransformationFoundException) {
                logger.error { e }
                logger.error { "Connection failed. Retrying in 1 sec" }
                delay(1.seconds)
            }
        }
    }
    client.close()
    println("Connection closed. Goodbye!")
}

private fun createHttpClient() = HttpClient {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
    install(HttpTimeout)
    install(WebSockets)
}

private suspend fun setupWebsocket(client: HttpClient) {
    val httpResponse: HttpResponse = client.get(HTTP_NEGOTIATION_URL)
    val negotiateResponse: NegotiateResponse = httpResponse.body()
    logger.info { "negotiate token: ${negotiateResponse.ConnectionToken}" }

    val cookie = (httpResponse.headers["Set-Cookie"] ?: httpResponse.headers["Set-Cookie"])!!
    logger.info { "negotiate cookie: $cookie" }
    client.wss(
        host = "livetiming.formula1.com",
        path = "$SIGNALR_PATH${negotiateResponse.ConnectionToken}",
        request = {
            header("Connection", "keep-alive, Upgrade")
            header("user-agent", "BestHTTP")
            header("Accept-Encoding", "gzip,identity")
            header(HttpHeaders.Cookie, cookie)
        }
    ) {
        handleF1Socket()
    }
}

private suspend fun DefaultClientWebSocketSession.handleF1Socket() {
    send(F1_STREAMING_REQUEST)

    incoming.consumeEach { frame ->
        if (frame is Frame.Text) {
            val streamingData = frame.readText()
            val parsedF1Data = parseF1TimingData(streamingData)

            parsedF1Data.extractDriverLines()
                .mapTimingToResultRows(parsedF1Data.R.TimingData.SessionPart)
//                ?.also { saveResultRows(it) }
        } else {
            logger.warn { "non-text message: ${frame.data}" }
        }
    }
}

