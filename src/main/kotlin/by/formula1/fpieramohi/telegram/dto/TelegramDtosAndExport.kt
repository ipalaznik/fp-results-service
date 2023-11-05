@file:Suppress("unused")

package by.formula1.fpieramohi.telegram.dto

import by.formula1.fpieramohi.livetiming.dto.DriverLine
import by.formula1.fpieramohi.livetiming.dto.DriverLinePartial
import by.formula1.fpieramohi.telegram.dto.Team.*
import mu.KotlinLogging

data class ResultRow(
    val position: Int,
    val driver: Driver,
    val bestLapTime: String,
    val timeDiffToAhead: String,
    val timeDiffToFirst: String,
) {
    fun merge(partialDriverLine: DriverLinePartial, part: Int?): ResultRow {
        return ResultRow(
            partialDriverLine.Line ?: position,
            driver,
            partialDriverLine.BestLapTime?.Value ?: partialDriverLine.BestLapTimes?.getOrDefault(part, null)?.Value ?: bestLapTime,
            partialDriverLine.Stats?.get(part)?.TimeDiffToPositionAhead ?: partialDriverLine.TimeDiffToPositionAhead ?: timeDiffToAhead,
            partialDriverLine.Stats?.get(part)?.TimeDiffToFastest ?: partialDriverLine.TimeDiffToFastest ?: timeDiffToFirst
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
        timeDiffToFirst = this.Stats?.get(index - 1)?.TimeDiffToFastest ?: this.TimeDiffToFastest.orEmpty()
    )
}

fun Collection<ResultRow>.mapToMessageString() = this.joinToString("\r\n") { mapResultRowToString(it) }
private fun mapResultRowToString(it: ResultRow) =
        "${it.position}. ${it.withIndent()}${it.driver.team.emojiId} ${it.driver.lastname}   ${it.timing()}"

private fun ResultRow.withIndent() = if (this.position < 10) "  " else ""

private fun ResultRow.timing() = if (this.position == 1) this.bestLapTime else this.timeDiffToFirst

enum class Driver(
    val number: Int,
    val firstname: String,
    val lastname: String,
    val shortName: String,
    val emojiId: String = "\uD83D\uDE05",
    val team: Team
) {
    VERSTAPPEN(1, "Макс", "Верстапен", "ВЕР", team = RED_BULL),
    PEREZ(11, "Серхіа", "Перэс", "ПЕР", team = RED_BULL, emojiId = "\uD83C\uDDF2\uD83C\uDDFD"),
    ALONSO(14, "Фернанда", "Алонса", "АЛО", team = ASTON_MARTIN),
    HAMILTON(44, "Льюіс", "Гэмілтан", "ГЭМ", team = MERCEDES),
    SAINZ(55, "Карлас", "Сайнс", "САЙ", team = FERRARI),
    RUSSELL(63, "Джордж", "Расэл", "РАС", team = MERCEDES),
    LECLERC(16, "Шарль", "Леклер", "ЛЕК", team = FERRARI),
    STROLL(18, "Лэнс", "Строл", "СТР", team = ASTON_MARTIN),
    NORRIS(4, "Ланда", "Норыс", "НОР", team = MCLAREN),
    PIASTRI(81, "Оскар", "Піастры", "ПІЯ", team = MCLAREN),
    OCON(31, "Эстэбан", "Акон", "АКО", team = ALPINE),
    GASLY(10, "П'ер", "Гаслі", "ГАС", team = ALPINE),
    ALBON(23, "Алекс", "Албан", "АЛБ", team = WILLIAMS),
    SARGEANT(2, "Логан", "Сарджэнт", "САР", team = WILLIAMS),
    HULKENBERG(27, "Ніка", "Хюлкенберг", "ХЮЛ", team = HAAS),
    MAGNUSSEN(20, "Кевін", "Магнусэн", "МАГ", team = HAAS),
    BOTTAS(77, "Вальтэры", "Ботас", "БОТ", team = ALFA_ROMEO),
    ZHOU(24, "Гуанью", "Чжоў", "ЧЖО", team = ALFA_ROMEO),
    TSUNODA(22, "Юкі", "Цунода", "ЦУН", team = ALPHA_TAURI),
    RICCIARDO(3, "Даніэль", "Рыкарда", "РЫК", team = ALPHA_TAURI),
    HIDDEN(39, "Робэрт", "Шв***ман", "ШВА", team = FERRARI),
    LAWSON(40, "Ліам", "Лоўсан", "ЛОЎ", team = ALPHA_TAURI),
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