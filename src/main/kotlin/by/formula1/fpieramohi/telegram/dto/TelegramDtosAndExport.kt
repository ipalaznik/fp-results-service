@file:Suppress("unused")

package by.formula1.fpieramohi.telegram.dto

import by.formula1.fpieramohi.livetiming.dto.DriverLine
import by.formula1.fpieramohi.livetiming.dto.DriverLinePartial
import by.formula1.fpieramohi.telegram.dto.Team.*
import mu.KotlinLogging

data class ResultRow(
    val position: Int,
    val driver: Driver,
    val bestLapTime: String?,
    val timeDiffToAhead: String,
    val timeDiffToFirst: String,
    var gapToLeader: String?,
    var intervalToAhead: String?,
    val inPit: Boolean?,
    val pitOut: Boolean?,
    val lastLapTime: String?,
) {
    fun merge(partialDriverLine: DriverLinePartial, part: Int?): ResultRow {
        return ResultRow(
            partialDriverLine.Line ?: position,
            driver,
            partialDriverLine.BestLapTime?.Value ?: partialDriverLine.BestLapTimes?.getOrDefault(part, null)?.Value ?: bestLapTime,
            partialDriverLine.Stats?.get(part)?.TimeDiffToPositionAhead ?: partialDriverLine.TimeDiffToPositionAhead ?: timeDiffToAhead,
            partialDriverLine.Stats?.get(part)?.TimeDiffToFastest ?: partialDriverLine.TimeDiffToFastest ?: timeDiffToFirst,
            partialDriverLine.GapToLeader ?: gapToLeader,
            partialDriverLine.IntervalToPositionAhead?.Value ?: intervalToAhead,
            partialDriverLine.InPit ?: inPit,
            partialDriverLine.PitOut ?: pitOut,
            partialDriverLine.LastLapTime?.Value ?: lastLapTime
        )
    }
}

private val logger = KotlinLogging.logger {}

private val numberOfEntries = mapOf(
    0 to 20,
    1 to 15,
    2 to 10
)

fun List<DriverLine>.mapTimingToResultRows(sessionPart: Int? = null): List<ResultRow> {
    logger.info { "ResultRows: $this" }
    val index = (sessionPart ?: 1) - 1
    return this
        .map { it.mapTimingToResultRow(sessionPart) }
        .sortedBy { it.position }
        .take(numberOfEntries.getOrDefault(index, 20))
}

fun DriverLine.mapTimingToResultRow(sessionPart: Int? = null): ResultRow {
    val index = sessionPart ?: 0
    return ResultRow(
        position = this.Line,
        driver = mapNumberToDriver(this.RacingNumber.toInt()),
        bestLapTime = this.BestLapTime.Value,
        timeDiffToAhead = this.Stats?.get(index - 1)?.TimeDiffToPositionAhead ?: this.TimeDiffToPositionAhead.orEmpty(),
        timeDiffToFirst = this.Stats?.get(index - 1)?.TimeDiffToFastest ?: this.TimeDiffToFastest.orEmpty(),
        gapToLeader = this.GapToLeader,
        intervalToAhead = this.IntervalToPositionAhead?.Value,
        inPit = this.InPit,
        pitOut = this.PitOut,
        lastLapTime = this.LastLapTime.Value,
    )
}

fun Collection<ResultRow>.mapToMessageString() = this.joinToString("\r\n") { mapResultRowToString(it) }
private fun mapResultRowToString(it: ResultRow) =
        "${it.position}. ${it.withIndent()}${it.driver.emojiId} ${it.driver.lastname}   ${it.timing()}"

fun Collection<ResultRow>.mapToFPStreamingString() = this.joinToString("\r\n") { mapResultRowToFPStreamingString(it) }
private fun mapResultRowToFPStreamingString(it: ResultRow) =
        "${it.position}. ${it.withIndent()}${it.driver.emojiId} ${it.driver.lastname}  ${it.timing()}    (${it.lapTime()})"

fun Collection<ResultRow>.mapRaceToMessageString(withGap: Boolean) = this.joinToString("\r\n") { mapRaceResultRowToString(it, withGap) }
private fun mapRaceResultRowToString(it: ResultRow, withGap: Boolean) =
        "${it.position}. ${it.withIndent()}${it.driver.emojiId} ${it.driver.lastname}  ${it.interval()}" + gapText(it, withGap)

private fun gapText(it: ResultRow, withGap: Boolean) = if (withGap) "   (${it.gapToLeader})" else ""

private fun ResultRow.withIndent() = if (this.position < 10) "  " else ""

private fun ResultRow.timing() = if (this.position == 1) this.bestLapTime else this.timeDiffToFirst
private fun ResultRow.lapTime() = if (this.inPit == true) "ПІТ" else
    if (this.pitOut == true) "АЎТ" else this.lastLapTime

private fun ResultRow.interval() = if (this.position == 1) "" else this.intervalToAhead
private fun ResultRow.gapToLeader() = if (this.position == 1) "" else this.gapToLeader
//|9,2F15|d,1AD|4,281|W,BC|X,27","M":[{"H":"Streaming","M":"feed","A":["TimingData",{"Lines":{"18":{"Sectors":{"0":{"Value":"19.638","PersonalFastest":false},"1":{"Value":""},"2":{"Value":""}},"Speeds":{"I2":{"Value":""},"FL":{"Value":"","PersonalFastest":false}}}}},"2023-11-05T17:48:35.807Z"]}]}
//Parsing it.
//
//lines size = 1, lines = {14=DriverLinePartial(Stats=null, TimeDiffToFastest=null, TimeDiffToPositionAhead=null, Line=null, Position=null, ShowPosition=true, RacingNumber=null, Retired=false, InPit=false, PitOut=false, Stopped=false, Status=null, BestLapTime=null, BestLapTimes=null, LastLapTime=null)}
//Websocket received partial TimingData: {"C":"d-CF2CCC20-B,0|H8D,0|H8E,6|9,380A|d,1BE|4,29B|W,DA|X,2C","M":[{"H":"Streaming","M":"feed","A":["TimingData",{"Lines":{"1":{"Sectors":{"1":{"Segments":{"5":{"Status":2048}}}}}}},"2023-11-05T17:52:39.382Z"]}]}
//Parsing it.
//lines size = 1, lines = {1=DriverLinePartial(Stats=null, TimeDiffToFastest=null, TimeDiffToPositionAhead=null, Line=null, Position=null, ShowPosition=true, RacingNumber=null, Retired=false, InPit=false, PitOut=false, Stopped=false, Status=null, BestLapTime=null, BestLapTimes=null, LastLapTime=null)}
//Websocket received text: {"C":"d-CF2CCC20-B,0|H8D,0|H8E,6|9,380A|d,1BE|4,29B|W,DB|X,2C","M":[{"H":"Streaming","M":"feed","A":["TimingAppData",{"Lines":{"24":{"Stints":{"3":{"TotalLaps":2}}}}},"2023-11-05T17:52:39.538Z"]}]}
//Websocket received partial TimingData: {"C":"d-CF2CCC20-B,0|H8D,0|H8E,6|9,380B|d,1BE|4,29B|W,DB|X,2C","M":[{"H":"Streaming","M":"feed","A":["TimingData",{"Lines":{"18":{"GapToLeader":"+15.682","IntervalToPositionAhead":{"Value":"+0.466"}}}},"2023-11-05T17:52:39.601Z"]}]}
//Parsing it.
//
enum class Driver(
    val number: Int,
    val firstname: String,
    val lastname: String,
    val shortName: String,
    val emojiId: String = "\uD83D\uDE05",
    val team: Team
) {
    VERSTAPPEN(1, "Макс", "Верстапен", "ВЕР", "\uD83C\uDDF3\uD83C\uDDF1", team = RED_BULL),
    PEREZ(11, "Серхіа", "Перэс", "ПЕР", "\uD83C\uDDF2\uD83C\uDDFD", team = RED_BULL),
    ALONSO(14, "Фернанда", "Алонса", "АЛО", "\uD83C\uDDEA\uD83C\uDDF8", team = ASTON_MARTIN),
    HAMILTON(44, "Льюіс", "Гэмілтан", "ГЭМ", "\uD83C\uDDEC\uD83C\uDDE7", team = MERCEDES),
    SAINZ(55, "Карлас", "Сайнс", "САЙ", "\uD83C\uDDEA\uD83C\uDDF8", team = FERRARI),
    RUSSELL(63, "Джордж", "Расэл", "РАС", "\uD83C\uDDEC\uD83C\uDDE7", team = MERCEDES),
    LECLERC(16, "Шарль", "Леклер", "ЛЕК", "\uD83C\uDDF2\uD83C\uDDE8", team = FERRARI),
    STROLL(18, "Лэнс", "Строл", "СТР", "\uD83C\uDDE8\uD83C\uDDE6", team = ASTON_MARTIN),
    NORRIS(4, "Ланда", "Норыс", "НОР", "\uD83C\uDDEC\uD83C\uDDE7", team = MCLAREN),
    PIASTRI(81, "Оскар", "Піастры", "ПІЯ", "\uD83C\uDDE6\uD83C\uDDFA", team = MCLAREN),
    OCON(31, "Эстэбан", "Акон", "АКО", "\uD83C\uDDEB\uD83C\uDDF7", team = ALPINE),
    GASLY(10, "П'ер", "Гаслі", "ГАС", "\uD83C\uDDEB\uD83C\uDDF7", team = ALPINE),
    ALBON(23, "Алекс", "Албан", "АЛБ", "\uD83C\uDDF9\uD83C\uDDED", team = WILLIAMS),
    SARGEANT(2, "Логан", "Сарджэнт", "САР", "\uD83C\uDDFA\uD83C\uDDF8", team = WILLIAMS),
    HULKENBERG(27, "Ніка", "Хюлкенберг", "ХЮЛ", "\uD83C\uDDE9\uD83C\uDDEA", team = HAAS),
    MAGNUSSEN(20, "Кевін", "Магнусэн", "МАГ", "\uD83C\uDDE9\uD83C\uDDF0", team = HAAS),
    BOTTAS(77, "Вальтэры", "Ботас", "БОТ", "\uD83C\uDDEB\uD83C\uDDEE", team = ALFA_ROMEO),
    ZHOU(24, "Гуанью", "Чжоў", "ЧЖО", "\uD83C\uDDE8\uD83C\uDDF3", team = ALFA_ROMEO),
    TSUNODA(22, "Юкі", "Цунода", "ЦУН", "\uD83C\uDDEF\uD83C\uDDF5", team = ALPHA_TAURI),
    RICCIARDO(3, "Даніэль", "Рыкарда", "РЫК", "\uD83C\uDDE6\uD83C\uDDFA", team = ALPHA_TAURI),
    HIDDEN(39, "Робэрт", "Шв***ман", "ШВА", team = FERRARI),
    LAWSON(40, "Ліам", "Лоўсан", "ЛОЎ", "\uD83C\uDDF3\uD83C\uDDFF", team = ALPHA_TAURI),
    UNKNOWN(0, "Невядомы", "Невядомы", "НЕВ", team = Team.UNKNOWN),
}

private fun mapNumberToDriver(number: Int) =
    Driver.entries.find { it.number == number }
        ?: Driver.UNKNOWN

enum class Team(
    val teamName: String,
    val emojiId: String = "",
    val customEmojiId: String = ""
) {
    RED_BULL("Рэд Бул", "\u0031\u20E3"),
    FERRARI("Ферары", "\u0032\u20E3", "5208706285255534951"),
    MERCEDES("Мэрсэдэс", "\u0033\u20E3"),
    ALPINE("Альпін", "\u0034\u20E3"),
    MCLAREN("Макларэн", "\u0035\u20E3"),
    ALFA_ROMEO("Альфа Рамэа", "\u0036\u20E3"),
    ASTON_MARTIN("Астан Мартын", "\u0037\u20E3"),
    HAAS("Хаас", "\u0038\u20E3"),
    ALPHA_TAURI("Альфа Таўры", "\u0039\u20E3"),
    WILLIAMS("Ўільямс", "\uD83D\uDD1F"),
    UNKNOWN("Невядомы", "\u2757"),

}