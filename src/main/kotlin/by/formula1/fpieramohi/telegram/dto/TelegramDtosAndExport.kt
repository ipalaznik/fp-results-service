@file:Suppress("unused")

package by.formula1.fpieramohi.telegram.dto

import by.formula1.fpieramohi.livetiming.dto.DriverLine
import by.formula1.fpieramohi.telegram.dto.Team.*
import mu.KotlinLogging

data class ResultRow(
    val position: Int,
    val driver: Driver,
    val bestLapTime: String,
    val timeDiffToAhead: String,
    val timeDiffToFirst: String,
)

private val logger = KotlinLogging.logger {}

private val numberOfEntries = mapOf(
    0 to 20,
    1 to 15,
    2 to 10
)
fun List<DriverLine>.mapTimingToResultRows(sessionPart: Int? = null): List<ResultRow> {
    logger.info { "ResultRows: $this" }
    val index = sessionPart ?: 0
    return this
        .map {
            ResultRow(
                position = it.Line,
                driver = mapNumberToDriver(it.RacingNumber.toInt()),
                bestLapTime = it.BestLapTime.Value,
                timeDiffToAhead = it.Stats?.get(index)?.TimeDiffToPositionAhead ?: it.TimeDiffToPositionAhead.orEmpty(),
                timeDiffToFirst = it.Stats?.get(index)?.TimeDiffToFastest ?: it.TimeDiffToFastest.orEmpty()
            )
        }
        .sortedBy { it.position }
        .take(numberOfEntries.getOrDefault(sessionPart, 20))
}

fun List<ResultRow>.mapToMessageString() = this.joinToString("\r\n") { mapResultRowToString(it) }
private fun mapResultRowToString(it: ResultRow) =
    "${it.position}.  ${it.withIndent()}${it.driver.lastname}   ${it.timing()}"

private fun ResultRow.withIndent() = if (this.position < 10) " " else ""

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
    PEREZ(11, "Серхіа", "Перэс", "ПЕР", team = RED_BULL),
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
    SWICEL(39, "Робэрт", "Шварцман", "ШВА", team = FERRARI),
    LAWSON(40, "Ліам", "Лоўсан", "ЛОЎ", team = ALPHA_TAURI),
    UNKNOWN(0, "Невядомы", "Невядомы", "НЕВ", team = Team.UNKNOWN),
}

private fun mapNumberToDriver(number: Int) =
    Driver.entries.first { it.number == number }

enum class Team(
    val teamName: String,
    val emojiId: String
) {
    ALFA_ROMEO("Альфа Рамэа", ""),
    ALPHA_TAURI("Альфа Таўры", ""),
    ALPINE("Альпін", ""),
    ASTON_MARTIN("Астан Мартын", ""),
    FERRARI("Ферары", "5208706285255534951"),
    HAAS("Хаас", ""),
    MCLAREN("Макларэн", ""),
    MERCEDES("Мэрсэдэс", ""),
    RED_BULL("Рэд Бул", ""),
    WILLIAMS("Ўільямс", ""),
    UNKNOWN("Невядомы", ""),

}