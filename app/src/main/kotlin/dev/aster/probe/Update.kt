package dev.aster.probe

import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject

/**
 * Whether a newer Droid is out. Droid is installed from a file, so nothing
 * else tells the person an update exists. This only reads the latest release
 * and links to the download page; it never downloads or installs anything.
 */
object Update {

    const val PAGE = "https://droid.withaster.dev/download"

    /** The latest release's version, such as `0.4.0`, or null when GitHub did not answer. Network. */
    fun latest(): String? = runCatching {
        val connection = (URL(LATEST).openConnection() as HttpURLConnection).apply {
            connectTimeout = TIMEOUT_MS
            readTimeout = TIMEOUT_MS
            setRequestProperty("Accept", "application/vnd.github+json")
            setRequestProperty("User-Agent", "Droid")
        }
        if (connection.responseCode != 200) return null
        val body = connection.inputStream.bufferedReader().use { it.readText() }
        JSONObject(body).optString("tag_name").ifEmpty { null }?.removePrefix("v")
    }.getOrNull()

    /** True when [latest] is a higher version than [current], compared number by number. */
    fun newer(latest: String, current: String): Boolean {
        val a = parts(latest) ?: return false
        val b = parts(current) ?: return false
        for (i in 0 until maxOf(a.size, b.size)) {
            val x = a.getOrElse(i) { 0 }
            val y = b.getOrElse(i) { 0 }
            if (x != y) return x > y
        }
        return false
    }

    private fun parts(version: String): List<Int>? =
        version.trim().removePrefix("v").substringBefore('-').split('.').map { it.toIntOrNull() ?: return null }

    private const val LATEST = "https://api.github.com/repos/Zfinix/droid/releases/latest"
    private const val TIMEOUT_MS = 10_000
}
