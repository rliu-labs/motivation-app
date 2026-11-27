package app.sentier.data

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class AutoBackupTest {

    @Test
    fun fileNamesAreDatedAndSortable() {
        assertEquals("sentier-auto-2026-11-27.json", AutoBackup.fileName(LocalDate.of(2026, 11, 27)))
    }

    @Test
    fun keepsOnlyTheFourLatestAutomaticBackups() {
        val names = listOf(
            "sentier-auto-2026-10-30.json",
            "sentier-auto-2026-11-06.json",
            "sentier-auto-2026-11-13.json",
            "sentier-auto-2026-11-20.json",
            "sentier-auto-2026-11-27.json",
            "sentier-auto-2026-10-23.json",
            "photo.jpg",
            "sentier-sauvegarde-2026-01-01.json",
        )
        assertEquals(
            listOf("sentier-auto-2026-10-30.json", "sentier-auto-2026-10-23.json"),
            AutoBackup.toDelete(names),
        )
    }
}
