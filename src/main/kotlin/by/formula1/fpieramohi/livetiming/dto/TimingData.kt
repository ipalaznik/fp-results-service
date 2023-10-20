@file:Suppress("PropertyName")

package by.formula1.fpieramohi.livetiming.dto

import kotlinx.serialization.Serializable

@Serializable
data class NegotiateResponse (
    val Url: String,
    val ConnectionToken: String,
    val ConnectionId: String,
    val KeepAliveTimeout: Double,
    val DisconnectTimeout: Double,
    val ConnectionTimeout: Double,
    val TryWebSockets: Boolean,
    val ProtocolVersion: String,
    val TransportConnectTimeout: Double,
    val LongPollDelay: Double
)

@Serializable
data class TimingDataResponse (
    val R: TimingDataWrapper,
    val I: Int
) {
    fun extractDriverLines() = R.TimingData.Lines.values.toList()

    fun extractDriverLinesByNumber() = R.TimingData.Lines
}

@Serializable
data class TimingDataWrapper (
    val TimingData: TimingData,
)

@Serializable
data class TimingData (
    val Lines: Map<Int, DriverLine>,
    val SessionPart: Int? = null,
    val Withheld: Boolean
)

@Serializable
data class DriverLine (
    var Stats: List<StatsTime>? = null,
    var TimeDiffToFastest: String? = null,
    var TimeDiffToPositionAhead: String? = null,
    val Line: Int,
    val Position: String,
    val ShowPosition: Boolean = true,
    val RacingNumber: String,
    val Retired: Boolean = false,
    val InPit: Boolean = false,
    val PitOut: Boolean = false,
    val Stopped: Boolean = false,
    val Status: Int,
//    val Sectors: List<Sector>,
//    val Speeds: List<Speed>,
    val BestLapTime: BestLapTime,
    val LastLapTime: LastLapTime,
//    val NumberOfLaps: Int,
//    val NumberOfPitStops: Int,

)

@Serializable
data class StatsTime (
    var TimeDiffToFastest: String? = null,
    var TimeDiffToPositionAhead: String? = null,
)

@Serializable
data class BestLapTime (
    val Value: String,
//    val Lap: Int?
)

@Serializable
data class LastLapTime (
    val Value: String,
//    val Status: Int,
//    val OverallFastest: Boolean,
//    val PersonalFastest: Boolean
)