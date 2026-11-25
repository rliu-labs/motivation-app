package app.sentier.steps

import org.junit.Assert.assertEquals
import org.junit.Test

class SensorAccumulateTest {
    @Test
    fun firstReadingStartsAtZero() {
        assertEquals(0L, SensorStepSource.accumulate(null, null, 0, 12_000f, 10))
    }

    @Test
    fun sameDayAddsDifference() {
        assertEquals(1_500L, SensorStepSource.accumulate(12_000f, 10, 1_000, 12_500f, 10))
    }

    @Test
    fun newDayRestartsFromZero() {
        assertEquals(300L, SensorStepSource.accumulate(12_000f, 9, 4_000, 12_300f, 10))
    }

    @Test
    fun rebootCountsWholeReading() {
        assertEquals(1_250L, SensorStepSource.accumulate(12_000f, 10, 1_000, 250f, 10))
    }
}
