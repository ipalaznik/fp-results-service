package by.formula1.fpieramohi.livetiming

import by.formula1.fpieramohi.livetiming.dto.NegotiateResponse
import by.formula1.fpieramohi.telegram.dto.*
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
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.seconds

private val logger = KotlinLogging.logger {}
private var currentResults = ConcurrentHashMap<Int, ResultRow>()
// val client = KMongo.createClient()
//        val database = client.getDatabase("test")
//        val col = database.getCollection<Tweet>()
//
//        col.insertOne(Tweet(ZonedDateTime.now(), "Bottas 1", emptyList(), "Alfa Romeo"))

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
            try {
                parseAndSendResults(streamingData, bot, message)
            }
            catch (e: Exception) {
                println("error with parsing timing data %s".format(streamingData))
                println("%s".format(e))
                logger.error { e }

            }
        } else {
            logger.warn { "non-text message: ${frame.data}" }
//            receiveAndSendTimings(bot, message, "No results selected from DB, I'm sorry!")
        }
    }
}

private suspend fun parseAndSendResults(
    streamingData: String,
    bot: Bot,
    message: Message
) {
    if (streamingData.contains("\"TimingData\"") && streamingData.contains("\"R\"")) {
        //            if (parsedF1Data != null) {
        val parsedF1Data = parseF1TimingData(streamingData)

        //                val response = parsedF1Data
        //                    .extractDriverLines()
        //                    .mapTimingToResultRows(parsedF1Data.R.TimingData.SessionPart)
        //                    .mapToMessageString()
        //                receiveAndSendTimings(bot, message, response)

        parsedF1Data
            .extractDriverLinesByNumber()
            .mapValues { it.value.mapTimingToResultRow(parsedF1Data.R.TimingData.SessionPart) }
            .let { currentResults.putAll(it) }
        receiveAndSendTimings(bot, message)
    } else if (streamingData.contains("\"TimingData\"") && streamingData.contains("\"Streaming\"")) {
        val parsedF1Data = parseF1PartialTimingData(streamingData)
        parsedF1Data?.Lines
            ?.mapValues {
                val merged = currentResults[it.key]!!.merge(it.value)
                println("Merging line ${it.key} with value: ${it.value}")
                println("Merged value: $merged")
                merged
            }
            ?.forEach {
                currentResults[it.key] = it.value
            }
        receiveAndSendTimings(bot, message)
    } else {
        println("Websocket received text: $streamingData")
    }
}

private fun mapCurrentResultsToText() = currentResults
    .values
    .toList()
    .sortedBy { it.position }
    .mapToMessageString()

private suspend fun receiveAndSendTimings(bot: Bot, message: Message, resultText: String?) {
    if (!resultText.isNullOrBlank()) {
        bot.sendMessage(message.chat.id.toChatId(), resultText)
        withContext(Dispatchers.IO) {
            TimeUnit.SECONDS.sleep(5)
        }
    }
}

private suspend fun receiveAndSendTimings(bot: Bot, message: Message) {
    val resultText = mapCurrentResultsToText()
    bot.sendMessage(message.chat.id.toChatId(), resultText)
//    withContext(Dispatchers.IO) {
//        TimeUnit.SECONDS.sleep(5)
//    }
}