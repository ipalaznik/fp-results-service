package by.formula1.fpieramohi.livetiming

import by.formula1.fpieramohi.livetiming.dto.TimingDataPartial
import by.formula1.fpieramohi.livetiming.dto.TimingDataPartialResponse
import by.formula1.fpieramohi.livetiming.dto.TimingDataResponse
import kotlinx.serialization.json.Json
import mu.KotlinLogging

private val json = Json { ignoreUnknownKeys = true }
private val logger = KotlinLogging.logger {}

fun parseF1TimingData(streamingData: String): TimingDataResponse {
        println("Websocket received initial TimingData: $streamingData")
        println("Parsing it.")
        val timingData = json.decodeFromString<TimingDataResponse>(streamingData)
        println("lines size = ${timingData.R.TimingData.Lines.size}, driver #1 = ${timingData.R.TimingData.Lines[1]}")
        return timingData
}

fun parseF1PartialTimingData(streamingData: String): TimingDataPartial? {
    println("Websocket received partial TimingData: $streamingData")
    println("Parsing it.")
    return try {
//        val fixedText = streamingData.replace("[\"TimingData\",{\"Lines\":", "[\"TimingData\":{\"Lines\":")
        val timingData = json.decodeFromString<TimingDataPartialResponse>(streamingData)
//        println("parsed head object: %s".format(timingData))
        val partialLinesData = timingData.M.firstOrNull()?.A?.get(1)
        val partialLines = partialLinesData?.let {
            json.decodeFromString<TimingDataPartial>(it.toString())
        }
        println("lines size = ${partialLines?.Lines?.size}, lines = ${partialLines?.Lines}")
        partialLines
    }
    catch (e: Exception) {
        println("error with parsing partial timing data %s".format(streamingData))
        println("%s".format(e))
        logger.error { e }
        null
    }
}