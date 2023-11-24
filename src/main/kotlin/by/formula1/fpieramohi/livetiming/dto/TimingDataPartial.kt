@file:Suppress("PropertyName")

package by.formula1.fpieramohi.livetiming.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray

@Serializable
data class TimingDataPartialResponse (
    val M: List<HMA>,
    val C: String
)

@Serializable
data class HMA (
    val H: String,
    val M: String,
    val A: JsonArray
)

@Serializable
data class TimingDataWrapperPartial (
    val TimingData: TimingDataPartial,
)

@Serializable
data class TimingDataPartial (
    val Lines: Map<Int, DriverLinePartial>,
    val SessionPart: Int? = null,
//    val Withheld: Boolean
)

@Serializable
data class DriverLinePartial (
//    var Stats: List<StatsTime>? = null,
    var Stats: Map<Int, StatsTime>? = null,
    var TimeDiffToFastest: String? = null,
    var TimeDiffToPositionAhead: String? = null,
    val Line: Int? = null,
    val Position: String? = null,
    val ShowPosition: Boolean? = true,
    val RacingNumber: String? = null,
    val Retired: Boolean? = false,
    val InPit: Boolean? = false,
    val PitOut: Boolean? = false,
    val Stopped: Boolean? = false,
    val Status: Int? = null,
//    val Sectors: List<Sector>,
//    val Speeds: List<Speed>,
    val BestLapTime: BestLapTime? = null,
    val BestLapTimes: Map<Int, BestLapTime>? = null,
    val LastLapTime: LastLapTime? = null,
    val GapToLeader: String? = null,
    val IntervalToPositionAhead: BestLapTime? = null,
//    val NumberOfLaps: Int,
//    val NumberOfPitStops: Int,
)

//@Serializable
//data class StatsTime (
//    var TimeDiffToFastest: String? = null,
//    var TimeDiffToPositionAhead: String? = null,
//)
//
//@Serializable
//data class BestLapTime (
//    val Value: String,
////    val Lap: Int?
//)
//
//@Serializable
//data class LastLapTime (
//    val Value: String,
////    val Status: Int,
////    val OverallFastest: Boolean,
////    val PersonalFastest: Boolean
//)