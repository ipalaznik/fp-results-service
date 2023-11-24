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
//    val Heartbeat: Map<String, String>,
//    val TimingStats: Map<String, >,
//    val TimingAppData: Map<String, String>,
//    val DriverList: Map<String, String>,
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
    val NumberOfLaps: Int? = null,
    val NumberOfPitStops: Int? = null,
    val GapToLeader: String? = null,
    val IntervalToPositionAhead: BestLapTime? = null,
//    "GapToLeader":"+15.682","IntervalToPositionAhead":{"Value":"+0.466"}}}}
)

@Serializable
data class StatsTime (
    var TimeDiffToFastest: String? = null,
    var TimeDiffToPositionAhead: String? = null,
)

@Serializable
data class BestLapTime (
    val Value: String? = null,
//    val Lap: Int?
)

@Serializable
data class LastLapTime (
    val Value: String? = null,
//    val Status: Int,
    val OverallFastest: Boolean? = null,
    val PersonalFastest: Boolean? = null
)