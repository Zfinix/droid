package dev.aster.probe.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * The Android 13 block on apps installed from a file, and the way round it.
 * The menu item that lifts it only appears once the greyed switch has been
 * tapped, which is the step people miss, so it comes first.
 */
@Composable
fun RestrictedHelp(what: String, onAppInfo: () -> Unit, modifier: Modifier = Modifier) {
    GroupCard(modifier) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Text(
                text = "If the switch is greyed out, Android is blocking it because Droid was installed from a file.",
                style = Type.body,
                color = Ink.amber,
            )
            Text(
                text = "1. Tap the greyed switch once, then close the message.\n" +
                    "2. Open App info, tap the three-dot menu at the top right, and choose Allow restricted " +
                    "settings. It only shows up after step 1.\n" +
                    "3. Come back here and turn on $what.",
                style = Type.secondary,
                color = Ink.dim,
                modifier = Modifier.padding(top = 6.dp),
            )
            QuietButton(text = "Open App info", onClick = onAppInfo, color = Ink.accent)
        }
    }
}
