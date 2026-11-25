package dev.aster.probe

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateTest {

    @Test
    fun `a higher release is newer`() {
        assertTrue(Update.newer("0.4.0", "0.3.1"))
        assertTrue(Update.newer("v0.3.2", "0.3.1"))
        assertTrue(Update.newer("1.0", "0.9.9"))
    }

    @Test
    fun `the same or an older release is not`() {
        assertFalse(Update.newer("0.3.1", "0.3.1"))
        assertFalse(Update.newer("0.3.0", "0.3.1"))
        assertFalse(Update.newer("0.3", "0.3.0"))
    }

    @Test
    fun `a tag that is not a version is ignored`() {
        assertFalse(Update.newer("nightly", "0.3.1"))
        assertFalse(Update.newer("0.4.0", ""))
    }
}
