package dev.aster.probe

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * `adb shell am broadcast -a dev.aster.probe.KEYS -p dev.aster.probe`: the
 * keys for other agents and editors, as `mcp=… acp=…` in the result, so
 * `aster-droid pair --adb` needs nothing pasted. Only the shell holds DUMP, and
 * a computer with adb on this phone can already open Connect and tap Copy, so
 * this hands nothing to anyone who did not already have it. An empty value
 * means that way in is turned off.
 */
class KeysReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val values = Env.all(context)
        resultCode = Activity.RESULT_OK
        resultData = "mcp=${values[Env.MCP_TOKEN].orEmpty()} acp=${values[Env.ACP_TOKEN].orEmpty()}"
    }
}
