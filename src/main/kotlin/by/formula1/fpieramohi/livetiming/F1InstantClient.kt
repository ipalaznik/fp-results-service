package by.formula1.fpieramohi.livetiming

import by.formula1.fpieramohi.livetiming.dto.NegotiateResponse
import by.formula1.fpieramohi.telegram.dto.mapTimingToResultRows
import by.formula1.fpieramohi.telegram.dto.mapToMessageString
import com.elbekd.bot.Bot
import com.elbekd.bot.model.toChatId
import com.elbekd.bot.types.Message
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.seconds

private val logger = KotlinLogging.logger {}

fun sendTimingsFromStreaming(bot: Bot, message: Message) {
    val client = createHttpClient()

    runBlocking {
        val shouldReconnect = true
        while (shouldReconnect) {
            try {
                setupWebsocket(client) {
                    handleF1Socket(bot, message)
                }
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

private suspend fun setupWebsocket(client: HttpClient, handler: suspend DefaultClientWebSocketSession.() -> Unit) {
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
        }, block = handler
    )
}

private suspend fun DefaultClientWebSocketSession.handleF1Socket(bot: Bot, message: Message) {
    send(F1_STREAMING_REQUEST)

    incoming.consumeEach { frame ->
        if (frame is Frame.Text) {
            val streamingData = frame.readText()
            val parsedF1Data = parseF1Data(streamingData)

            val response = parsedF1Data
                ?.extractDriverLines()
                ?.mapTimingToResultRows(parsedF1Data.R.TimingData.SessionPart)
                ?.mapToMessageString()
            receiveAndSendTimings(bot, message, response)
        } else {
            logger.warn { "non-text message: ${frame.data}" }
//            receiveAndSendTimings(bot, message, "No results selected from DB, I'm sorry!")
        }
    }
}

private suspend fun receiveAndSendTimings(bot: Bot, message: Message, resultText: String?) {
    if (resultText != null) {
        bot.sendMessage(message.chat.id.toChatId(), resultText)
        withContext(Dispatchers.IO) {
            TimeUnit.SECONDS.sleep(5)
        }
    }
}