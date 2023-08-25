package by.formula1.fpieramohi.livetiming

import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class F1DataParserKtTest {

    @Test
    fun parseF1DataShouldReturnObject() {
        val input = this.javaClass.getResource("/timingData.txt")?.readText()!!

        val parseF1Data = parseF1Data(input)

        assertNotNull(parseF1Data)
        assertTrue { parseF1Data.R.TimingData.Lines.size == 20 }
    }
}