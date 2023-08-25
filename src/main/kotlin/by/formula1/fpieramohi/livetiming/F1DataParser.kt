package by.formula1.fpieramohi.livetiming

import by.formula1.fpieramohi.livetiming.dto.TimingDataResponse
import kotlinx.serialization.json.Json
import mu.KotlinLogging

private val json = Json { ignoreUnknownKeys = true }
private val logger = KotlinLogging.logger {}

fun parseF1Data(streamingData: String): TimingDataResponse? {
    return if (streamingData.contains("\"TimingData\"") && streamingData.contains("\"R\"")) {
        println("Websocket received TimingData. Parsing it.")
        println("TimingData: $streamingData")
        try {
            val timingData = json.decodeFromString<TimingDataResponse>(streamingData)
            println("lines size = ${timingData.R.TimingData.Lines.size}, driver #1 = ${timingData.R.TimingData.Lines[1]}")
            timingData
        }
        catch (e: Exception) {
            println("error with parsing timing data %s".format(streamingData))
            println("%s".format(e))
            logger.error { e }
            null
        }
    } else {
//        val length = if (streamingData.length < 100) streamingData.length else 100
        println("Websocket received text: $streamingData")
        null
    }
}