package dev.aster.probe

/**
 * What a person reads when something goes wrong, in place of what the agent
 * printed. The raw line still goes to the log, where it helps whoever debugs
 * it; a notification that reads like a stack trace helps nobody holding the
 * phone.
 */
object Plain {

    enum class Trouble { KEY, BUSY, OFFLINE, OTHER }

    fun trouble(text: String): Trouble {
        val lower = text.lowercase()
        return when {
            KEY.containsMatchIn(lower) -> Trouble.KEY
            BUSY.containsMatchIn(lower) -> Trouble.BUSY
            OFFLINE.containsMatchIn(lower) -> Trouble.OFFLINE
            else -> Trouble.OTHER
        }
    }

    /** For the status notification, which stays up while the agent retries on its own. */
    fun status(line: String): String = when (trouble(line)) {
        Trouble.KEY -> "A key was turned down. Check your keys in Settings."
        Trouble.BUSY -> "The model is busy or over its limit. Droid keeps trying."
        Trouble.OFFLINE -> "Droid cannot reach the internet. It tries again on its own."
        Trouble.OTHER -> "The agent ran into a problem. Open Droid to see what happened."
    }

    /** For the chat, where the person can send the message again. */
    fun chat(message: String): String = when (trouble(message)) {
        Trouble.KEY -> "The model's provider turned down the key. Check it in Settings, then try again."
        Trouble.BUSY -> "The model is busy or over its limit. Wait a minute, then try again."
        Trouble.OFFLINE -> "Droid could not reach the model. Check that the phone is online, then try again."
        Trouble.OTHER -> "The agent ran into a problem and stopped. Try again, or start a new chat."
    }

    private val KEY = Regex("""\b(401|403)\b|unauthori[sz]ed|forbidden|invalid (api )?key|invalid token|api key""")
    private val BUSY = Regex("""\b429\b|rate.?limit|too many requests|quota|overloaded""")
    private val OFFLINE = Regex(
        """dns|timed out|timeout|connection (refused|reset)|network|unreachable|could not connect|failed to connect|no route""",
    )
}
