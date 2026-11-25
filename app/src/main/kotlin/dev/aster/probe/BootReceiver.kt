package dev.aster.probe

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Puts the agent back after a restart. A phone that reboots overnight would
 * otherwise sit with nothing running until someone opens Droid, which for a
 * phone nobody is holding means never. A setup that was never finished, or an
 * agent someone stopped on purpose, stays down.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        if (!Onboarding.done(context) || !AsterAgentService.wanted(context)) return
        AsterAgentService.start(context)
    }
}
