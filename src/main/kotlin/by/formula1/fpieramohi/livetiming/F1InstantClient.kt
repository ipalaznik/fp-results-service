package by.formula1.fpieramohi.livetiming

import by.formula1.fpieramohi.livetiming.dto.NegotiateResponse
import by.formula1.fpieramohi.telegram.dto.*
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
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.seconds

private val logger = KotlinLogging.logger {}
private var currentResults = ConcurrentHashMap<Int, ResultRow>()
//val client = KMongo.createClient()
//val database: MongoDatabase = client.getDatabase("test")
//val collection = database.getCollection<String>()

fun readTimingsFromStreaming() {
    val client = createHttpClient()

    runBlocking {
        val shouldReconnect = true
        while (shouldReconnect) {
            try {
                setupWebsocket(client) {
                    handleF1Socket()
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

private suspend fun DefaultClientWebSocketSession.handleF1Socket() {
    send(F1_STREAMING_REQUEST)

    incoming.consumeEach { frame ->
        if (frame is Frame.Text) {
            val streamingData = frame.readText()
            try {
                parseAndSendResults(streamingData)
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

private suspend fun parseAndSendResults(streamingData: String) {
    if (streamingData.contains("\"TimingData\"") && streamingData.contains("\"R\"")) {
//        collection.insertOne(streamingData)

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
    } else if (streamingData.contains("\"TimingData\"") && streamingData.contains("\"Streaming\"")) {
//        collection.insertOne(streamingData)
        val parsedF1Data = parseF1PartialTimingData(streamingData)
        var isUpdated = false

        parsedF1Data?.Lines
            ?.forEach {
                val part = parsedF1Data.SessionPart
                val currentResult = currentResults[it.key]
                val merged = currentResult!!.merge(it.value, part)
                if (merged != currentResult) {
                    isUpdated = true
                    currentResults[it.key] = merged
                    println("Updating line ${it.key} with new value: $merged")
                }
            }

    } else {
        println("Websocket received text: $streamingData")
    }
}

fun mapCurrentResultsToText(): String {
    val results = currentResults
        .values
        .toList()
        .sortedBy { it.position }
        .mapToMessageString()
    println(results)
    return results.ifEmpty { "No results yet" }
}
