package dev.aster.probe

import android.Manifest.permission as P
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings

/**
 * What the phone has to hand over, and when to ask. Droid used to declare
 * every permission a phone offers and ask for fifteen of them at launch. The
 * agent reaches calls, texts and contacts by tapping through the apps that own
 * them, so the only runtime permissions left are the ones the app itself
 * uses, each asked for at the moment it is first needed.
 */
object Permissions {
    /** Asked just before the scanner opens, and nowhere else. */
    const val CAMERA = 3
    /** Asked the first time the chat sends a message, or from the row that offers it. */
    const val NOTIFY = 4

    /** The agent's questions and replies arrive as notifications while it works in other apps. */
    val notify: List<String> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) listOf(P.POST_NOTIFICATIONS) else emptyList()

    fun missing(activity: Activity, wanted: List<String>): Array<String> = wanted
        .filter { activity.checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED }
        .toTypedArray()

    fun request(activity: Activity, wanted: List<String>, code: Int) {
        val ask = missing(activity, wanted)
        if (ask.isNotEmpty()) activity.requestPermissions(ask, code)
    }

    fun granted(context: Context, permission: String): Boolean =
        context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED

    /** True the first time it is called for [what], so an automatic prompt shows once and never nags. */
    fun firstAsk(context: Context, what: String): Boolean {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (prefs.getBoolean(what, false)) return false
        prefs.edit().putBoolean(what, true).apply()
        return true
    }

    private const val PREFS = "permissions"

    /**
     * Android 13 greys out the accessibility switch for an app installed from
     * a file, until App info's menu says "Allow restricted settings". The app
     * op Settings checks cannot be read without a privileged permission, so
     * this goes by what decides it: how the app was installed. A store or adb
     * install is never restricted. Whether the person already allowed it is
     * not visible here, which is why the help reads "if it is greyed out".
     */
    fun restricted(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return false
        val source = runCatching {
            context.packageManager.getInstallSourceInfo(context.packageName).packageSource
        }.getOrDefault(PackageInstaller.PACKAGE_SOURCE_UNSPECIFIED)
        return source == PackageInstaller.PACKAGE_SOURCE_LOCAL_FILE ||
            source == PackageInstaller.PACKAGE_SOURCE_DOWNLOADED_FILE
    }

    /** App info for Droid, where the menu with "Allow restricted settings" is. */
    fun appInfo(context: Context): Intent =
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))

    /** Doze stops a long-running agent, its relay link and its chat bridge unless the phone exempts it. */
    fun unrestrictedBattery(context: Context): Boolean =
        context.getSystemService(PowerManager::class.java)?.isIgnoringBatteryOptimizations(context.packageName) ?: false

    /** The one-tap system prompt for the exemption, rather than a list to search. */
    fun batteryPrompt(context: Context): Intent =
        Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.fromParts("package", context.packageName, null))
}
