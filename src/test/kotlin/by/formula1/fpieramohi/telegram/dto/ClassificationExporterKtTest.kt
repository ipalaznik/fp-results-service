package by.formula1.fpieramohi.telegram.dto

import by.formula1.fpieramohi.livetiming.dto.BestLapTime
import by.formula1.fpieramohi.livetiming.dto.DriverLine
import by.formula1.fpieramohi.livetiming.dto.LastLapTime
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

class ClassificationExporterKtTest {

    @Test
    fun shouldMapTimingToResultRowsToMessageString() {
        val driverLines: List<DriverLine> = listOf(
            createDriverLine(1, Driver.VERSTAPPEN.number),
            createDriverLine(2, Driver.ALONSO.number)
        )

        val resultRows = driverLines.mapTimingToResultRows()
            .mapToMessageString()

        val expectedResult = """
            1.   Верстапен   1:23.111
            2.   Алонса   +0.022""".trimIndent()
        assertEquals(expectedResult, resultRows)
    }

    private fun createDriverLine(position: Int, racingNumber: Int): DriverLine {
        return DriverLine(
            TimeDiffToFastest = "+${0.011 * position}",
            TimeDiffToPositionAhead = "+0.999",
            Line = position,
            Position = "$position",
            BestLapTime = BestLapTime("1:23.${100 + (11 * position)}"),
            LastLapTime = LastLapTime("2:34.234"),
            RacingNumber = racingNumber.toString(),
            Status = 1
        )
    }
}