@file:Suppress("NonAsciiCharacters")

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Reference: https://junit.org/junit5/docs/current/user-guide/
 */
class SampleJUnitTest {
    @Test
    fun `should be something when~`() {
        assertEquals(1, 1)
    }

    @Test
    fun `~のとき、〇〇になること`() {
        assertEquals(1, 1)
    }
}
