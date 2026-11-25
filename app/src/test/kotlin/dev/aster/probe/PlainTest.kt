package dev.aster.probe

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class PlainTest {

    @Test
    fun `a refused key reads as a key problem`() {
        assertEquals(Plain.Trouble.KEY, Plain.trouble("provider error: 401 Unauthorized"))
        assertEquals(Plain.Trouble.KEY, Plain.trouble("Invalid API key provided"))
    }

    @Test
    fun `a rate limit reads as busy`() {
        assertEquals(Plain.Trouble.BUSY, Plain.trouble("HTTP 429 Too Many Requests"))
        assertEquals(Plain.Trouble.BUSY, Plain.trouble("rate limit exceeded for free models"))
    }

    @Test
    fun `a dead connection reads as offline`() {
        assertEquals(Plain.Trouble.OFFLINE, Plain.trouble("error sending request: dns error: failed to lookup address"))
        assertEquals(Plain.Trouble.OFFLINE, Plain.trouble("operation timed out"))
    }

    @Test
    fun `a status code inside a longer number is not one`() {
        assertEquals(Plain.Trouble.OTHER, Plain.trouble("error: tool call_14012 failed"))
    }

    @Test
    fun `nothing raw reaches the person`() {
        val raw = "Error: reqwest::Error { kind: Decode, source: Error(\"EOF\") }"
        assertFalse(Plain.status(raw).contains("reqwest"))
        assertFalse(Plain.chat(raw).contains("reqwest"))
    }
}
