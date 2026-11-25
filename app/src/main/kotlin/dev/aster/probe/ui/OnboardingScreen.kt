package dev.aster.probe.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.aster.probe.Channel
import dev.aster.probe.Env
import dev.aster.probe.Lan
import dev.aster.probe.Install
import dev.aster.probe.Models
import dev.aster.probe.Onboarding
import dev.aster.probe.Onboarding.Step

/** Which switches the permissions step reads, fresh each time the screen comes back from Settings. */
data class Readiness(
    val screen: Boolean,
    val keyboard: Boolean,
    val listener: Boolean,
    val posting: Boolean,
    val battery: Boolean,
    /** Android 13+ will grey out the screen switch until "Allow restricted settings" is chosen. */
    val restricted: Boolean,
)

/** A door opened inside a step, so its own back arrow returns to the step's choices. */
private sealed interface Inside {
    data object Computer : Inside
    data object Keys : Inside
    data class Key(val provider: Models.Provider) : Inside
    data class Custom(val url: String) : Inside
    data object Telegram : Inside
    data object Photon : Inside
}

/**
 * The first run, one decision per screen: a brain, then the one switch it
 * needs. Every door works without an account, and the free models are the
 * button at the bottom, so the shortest path to a working agent is to keep
 * pressing it. The chat in the app is where to talk unless someone picks
 * another place, so that choice is a row, not a step.
 */
@Composable
fun OnboardingScreen(
    step: Step,
    providers: List<Models.Provider>,
    keys: Set<String>,
    brain: String?,
    channel: Channel,
    servers: List<Lan.Server>?,
    scanning: Boolean,
    onWifi: Boolean,
    busy: String?,
    problem: String?,
    readiness: Readiness,
    onStep: (Step) -> Unit,
    onFree: () -> Unit,
    onScanLan: () -> Unit,
    onServer: (Lan.Server, String) -> Unit,
    onKey: (Models.Provider, String, Boolean) -> Unit,
    onSignIn: () -> Unit,
    onCustom: (String, String, String, Boolean) -> Unit,
    onChannel: (Channel, Map<String, String>) -> Unit,
    onGrant: (GrantId) -> Unit,
    onAppInfo: () -> Unit,
    onPosting: () -> Unit,
    onBattery: () -> Unit,
    onScanCode: () -> Unit,
    onLink: (String) -> Unit,
    onClearProblem: () -> Unit,
    onFinish: () -> Unit,
) {
    var inside by remember(step) { mutableStateOf<Inside?>(null) }
    BackHandler(enabled = inside != null) {
        inside = when (inside) {
            is Inside.Key, is Inside.Custom -> Inside.Keys
            else -> null
        }
    }
    LaunchedEffect(inside) { onClearProblem() }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Ink.ground)
            .safeDrawingPadding()
            .imePadding()
            .padding(horizontal = Gutter),
    ) {
        if (step != Step.WELCOME) {
            BackRow(
                title = title(step, inside),
                onBack = {
                    when (inside) {
                        null -> onStep(Onboarding.previous(step))
                        is Inside.Key, is Inside.Custom -> inside = Inside.Keys
                        else -> inside = null
                    }
                },
                modifier = Modifier.padding(top = 8.dp),
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            when (step) {
                Step.WELCOME -> Welcome()
                Step.BRAIN -> when (val at = inside) {
                    null -> Brains(brain, busy, onFree, onOpen = { inside = it }, onScanCode = onScanCode)
                    Inside.Computer -> Computer(servers, scanning, onWifi, onScanLan, onServer, onLink, onType = {
                        inside = Inside.Custom("http://")
                    })
                    Inside.Keys -> Keys(providers, keys, onOpen = { inside = it }, onSignIn = onSignIn, busy = busy, problem = problem)
                    is Inside.Key -> KeyEntry(at.provider, busy, problem, onKey, onLink)
                    is Inside.Custom -> Custom(at.url, busy, problem, onCustom)
                    else -> Unit
                }
                Step.TALK -> when (inside) {
                    null -> Talk(channel, onOpen = { inside = it }, onChannel = onChannel)
                    Inside.Telegram -> Telegram(onChannel, onLink)
                    Inside.Photon -> Photon(onChannel, onLink)
                    else -> Unit
                }
                Step.PERMISSIONS -> Permissions(
                    readiness,
                    channel,
                    onGrant,
                    onAppInfo,
                    onPosting,
                    onBattery,
                    onTalk = { onStep(Step.TALK) },
                )
            }
            if (problem != null && inside !is Inside.Key && inside !is Inside.Custom && inside != Inside.Keys) {
                Text(text = problem, style = Type.secondary, color = Ink.red, modifier = Modifier.padding(top = 12.dp))
            }
            Spacer(Modifier.height(16.dp))
        }
        Column(Modifier.padding(top = 8.dp, bottom = Gutter)) {
            when {
                step == Step.WELCOME -> {
                    PrimaryButton(text = "Get started", onClick = { onStep(Step.BRAIN) })
                    QuietButton(text = "I have a setup code", onClick = onScanCode)
                }
                step == Step.BRAIN && inside == null -> {
                    PrimaryButton(text = busy ?: "Use the free models", enabled = busy == null, onClick = onFree)
                    if (brain != null) QuietButton(text = "Keep $brain", onClick = { onStep(Step.PERMISSIONS) })
                }
                step == Step.TALK && inside == null ->
                    QuietButton(text = "Keep ${where(channel)}", onClick = { onStep(Step.PERMISSIONS) })
                step == Step.PERMISSIONS -> PrimaryButton(
                    text = if (channel == Channel.NONE) "Finish" else "Start Droid",
                    onClick = onFinish,
                )
            }
        }
    }
}

private fun title(step: Step, inside: Inside?): String = when (inside) {
    Inside.Computer -> "Your computer's model"
    Inside.Keys -> "Sign in or use a key"
    is Inside.Key -> inside.provider.name
    is Inside.Custom -> "Any OpenAI-compatible address"
    Inside.Telegram -> "Telegram"
    Inside.Photon -> "iMessage and WhatsApp"
    null -> when (step) {
        Step.WELCOME -> ""
        Step.BRAIN -> "Choose a model"
        Step.PERMISSIONS -> "Let Droid use the screen"
        Step.TALK -> "Where to talk to it"
    }
}

@Composable
private fun Prose(text: String, modifier: Modifier = Modifier) {
    Text(text = text, style = Type.body, color = Ink.dim, modifier = modifier.padding(top = 12.dp))
}

@Composable
private fun Welcome() {
    Lockup(Modifier.padding(top = 32.dp))
    Text(
        text = "An agent that uses your phone the way you do.",
        style = MaterialTheme.typography.displaySmall,
        color = Ink.text,
        modifier = Modifier.padding(top = 48.dp),
    )
    Prose(
        "Droid lets an AI agent see the screen, tap, type and open apps, so you can ask for things " +
            "in plain words. It works with any model, including free ones and the one on your own computer.",
        Modifier.padding(top = 8.dp),
    )
    Prose("Setup takes about two minutes, and none of it needs an account.")
    if (!Install.supported()) {
        Text(text = Install.UNSUPPORTED, style = Type.body, color = Ink.red, modifier = Modifier.padding(top = 12.dp))
    }
}

@Composable
private fun Brains(brain: String?, busy: String?, onFree: () -> Unit, onOpen: (Inside) -> Unit, onScanCode: () -> Unit) {
    Prose("The model is what thinks. The free models are the quickest start, and you can change it any time.")
    if (brain != null) {
        Text(
            text = "Now: $brain",
            style = Type.secondary,
            color = Ink.faint,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
    Column(Modifier.padding(top = 20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Door(
            title = "Try it free",
            badge = "NO ACCOUNT",
            detail = "No sign-up and no key. About 200 requests an hour, around 10 tasks, which is plenty to try Droid.",
            onClick = { if (busy == null) onFree() },
        )
        Text(
            text = "The free models keep logs of what they are sent, so do not use them on screens with private " +
                "information, such as banking or health apps.",
            style = Type.secondary,
            color = Ink.amber,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
        SectionLabel("OR", Modifier.padding(top = 14.dp))
        Door(
            title = "Use my computer's model",
            detail = "Ollama or LM Studio on the same Wi-Fi. Private, free, and no limits.",
            onClick = { onOpen(Inside.Computer) },
        )
        Door(
            title = "Sign in or use a key",
            detail = "OpenRouter, Google Gemini, NVIDIA and others, or any OpenAI-compatible address.",
            onClick = { onOpen(Inside.Keys) },
        )
    }
    QuietButton(text = "Scan a setup code", onClick = onScanCode, modifier = Modifier.padding(top = 12.dp))
}

@Composable
private fun Computer(
    servers: List<Lan.Server>?,
    scanning: Boolean,
    onWifi: Boolean,
    onScan: () -> Unit,
    onServer: (Lan.Server, String) -> Unit,
    onLink: (String) -> Unit,
    onType: () -> Unit,
) {
    LaunchedEffect(Unit) { if (servers == null && !scanning) onScan() }
    var picking by remember { mutableStateOf<Lan.Server?>(null) }
    when {
        scanning -> Prose("Looking for Ollama and LM Studio on your Wi-Fi. This takes a few seconds.")
        !onWifi && servers.isNullOrEmpty() -> Prose(
            "This phone is not on Wi-Fi. Connect it to the same Wi-Fi as your computer, then scan again.",
        )
        servers.isNullOrEmpty() -> NothingFound(onLink)
        else -> {
            Prose("Tap the model to use. Pick one that can use tools; bigger models follow instructions better.")
            servers.forEach { server ->
                SectionLabel(server.name.uppercase(), Modifier.padding(top = 20.dp, bottom = 8.dp))
                GroupCard {
                    if (server.models.isEmpty()) {
                        Text(
                            text = "It has no models yet. Download one on the computer, then scan again.",
                            style = Type.secondary,
                            color = Ink.dim,
                            modifier = Modifier.padding(14.dp),
                        )
                    }
                    server.models.take(SHOWN_MODELS).forEachIndexed { i, model ->
                        SettingRow(label = Models.pretty(model), value = "", onClick = { onServer(server, model) })
                        if (i < server.models.size.coerceAtMost(SHOWN_MODELS) - 1) RowDivider()
                    }
                    if (server.models.size > SHOWN_MODELS) {
                        RowDivider()
                        SettingRow(
                            label = "All ${server.models.size} models",
                            value = "",
                            onClick = { picking = server },
                        )
                    }
                }
            }
        }
    }
    if (!scanning) {
        Row(Modifier.padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            QuietButton(text = "Scan again", onClick = onScan, modifier = Modifier.weight(1f), color = Ink.accent)
            QuietButton(text = "Type the address", onClick = onType, modifier = Modifier.weight(1f))
        }
    }
    picking?.let { server ->
        PickerSheet(
            label = server.name.uppercase(),
            options = server.models.map { Option(id = it, title = Models.pretty(it), detail = it) },
            chosenId = null,
            onPick = { picking = null; onServer(server, it.id) },
            onDismiss = { picking = null },
        )
    }
}

/** What to change on the computer, because both servers only listen to the computer itself out of the box. */
@Composable
private fun NothingFound(onLink: (String) -> Unit) {
    Prose(
        "Nothing answered. Ollama and LM Studio only listen to their own computer until you let the " +
            "network in. Do this on the computer, then scan again.",
    )
    SectionLabel("OLLAMA", Modifier.padding(top = 20.dp, bottom = 8.dp))
    CodeBlock(text = "OLLAMA_HOST=0.0.0.0 OLLAMA_CONTEXT_LENGTH=32768 ollama serve", onCopy = null)
    Prose(
        "Or in the Ollama app's settings, turn on Expose Ollama to the network. The longer context matters: " +
            "Droid's instructions alone fill Ollama's default.",
    )
    SectionLabel("LM STUDIO", Modifier.padding(top = 20.dp, bottom = 8.dp))
    Prose(
        "Open the Developer tab, start the server, and in its settings turn on Serve on Local Network. " +
            "Or run this in a terminal:",
        Modifier.padding(top = 0.dp),
    )
    CodeBlock(text = "lms server start --bind 0.0.0.0", onCopy = null, modifier = Modifier.padding(top = 8.dp))
    QuietButton(
        text = "LM Studio's guide",
        onClick = { onLink("https://lmstudio.ai/docs/developer/core/server/serve-on-network") },
        modifier = Modifier.padding(top = 4.dp),
    )
}

@Composable
private fun Keys(
    providers: List<Models.Provider>,
    keys: Set<String>,
    onOpen: (Inside) -> Unit,
    onSignIn: () -> Unit,
    busy: String?,
    problem: String?,
) {
    var more by remember { mutableStateOf(false) }
    val keyed = providers.filter { it.needsKey && it.keyVars.isNotEmpty() && !it.custom }
    val short = Models.SHORTLIST.mapNotNull { id -> keyed.firstOrNull { it.id == id } }
    Door(
        title = busy ?: "Sign in with OpenRouter",
        detail = "Opens OpenRouter in the browser and brings a key back, with nothing to copy. " +
            "It has free models and paid ones.",
        onClick = onSignIn,
        modifier = Modifier.padding(top = 12.dp),
    )
    if (problem != null) {
        Text(text = problem, style = Type.secondary, color = Ink.red, modifier = Modifier.padding(top = 10.dp))
    }
    SectionLabel("PASTE A KEY", Modifier.padding(top = 24.dp, bottom = 8.dp))
    GroupCard {
        short.forEachIndexed { i, provider ->
            SettingRow(
                label = provider.name,
                value = when {
                    provider.keyVars.any { it in keys } -> "Key saved"
                    provider.id in FREE_TIER -> "Free tier"
                    else -> ""
                },
                onClick = { onOpen(Inside.Key(provider)) },
            )
            if (i < short.lastIndex) RowDivider()
        }
    }
    GroupCard(Modifier.padding(top = 10.dp)) {
        SettingRow(label = "More providers", value = "${keyed.size}", onClick = { more = true })
        RowDivider()
        SettingRow(label = "Any OpenAI-compatible address", value = "", onClick = { onOpen(Inside.Custom("https://")) })
    }
    if (more) {
        PickerSheet(
            label = "PROVIDER",
            options = keyed.map { Option(id = it.id, title = it.name) },
            chosenId = null,
            onPick = { picked ->
                more = false
                keyed.firstOrNull { it.id == picked.id }?.let { onOpen(Inside.Key(it)) }
            },
            onDismiss = { more = false },
        )
    }
}

@Composable
private fun KeyEntry(
    provider: Models.Provider,
    busy: String?,
    problem: String?,
    onKey: (Models.Provider, String, Boolean) -> Unit,
    onLink: (String) -> Unit,
) {
    var key by remember(provider.id) { mutableStateOf("") }
    Prose("Paste your ${provider.name} key. It stays on this phone, in Droid's own files, and is only sent to ${provider.name}.")
    KEY_PAGES[provider.id]?.let { page ->
        QuietButton(text = "Get a key from ${provider.name}", onClick = { onLink(page) }, color = Ink.accent)
    }
    Field(
        value = key,
        onValueChange = { key = it.trim() },
        hint = "Paste the key here",
        secret = true,
        onDone = { if (key.isNotEmpty() && busy == null) onKey(provider, key, true) },
        modifier = Modifier.padding(top = 8.dp),
    )
    if (problem != null) {
        Text(text = problem, style = Type.secondary, color = Ink.red, modifier = Modifier.padding(top = 10.dp))
    }
    Spacer(Modifier.height(16.dp))
    PrimaryButton(
        text = busy ?: "Check and use it",
        enabled = key.isNotEmpty() && busy == null,
        onClick = { onKey(provider, key, true) },
    )
    if (problem != null && busy == null) QuietButton(text = "Use it anyway", onClick = { onKey(provider, key, false) })
}

@Composable
private fun Custom(start: String, busy: String?, problem: String?, onCustom: (String, String, String, Boolean) -> Unit) {
    var url by remember(start) { mutableStateOf(start) }
    var model by remember { mutableStateOf("") }
    var key by remember { mutableStateOf("") }
    val allowed = Lan.allowedUrl(url)
    Prose(
        "Any server that speaks the OpenAI chat API: a router, a company gateway, or a model on your own " +
            "network. The address usually ends in /v1.",
    )
    SectionLabel("ADDRESS", Modifier.padding(top = 20.dp, bottom = 8.dp))
    Field(value = url, onValueChange = { url = it.trim() }, hint = "https://example.com/v1", uri = true)
    if (url.length > "https://".length && !allowed) {
        Text(
            text = "Use an https address, or plain http only to a computer on your own network, such as 192.168.1.20.",
            style = Type.secondary,
            color = Ink.amber,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
    SectionLabel("MODEL", Modifier.padding(top = 20.dp, bottom = 8.dp))
    Field(value = model, onValueChange = { model = it.trim() }, hint = "The model's id, as the server lists it")
    SectionLabel("KEY, IF IT NEEDS ONE", Modifier.padding(top = 20.dp, bottom = 8.dp))
    Field(value = key, onValueChange = { key = it.trim() }, hint = "Leave empty if it needs none", secret = true)
    if (problem != null) {
        Text(text = problem, style = Type.secondary, color = Ink.red, modifier = Modifier.padding(top = 10.dp))
    }
    Spacer(Modifier.height(16.dp))
    val ready = allowed && model.isNotEmpty() && busy == null
    PrimaryButton(text = busy ?: "Check and use it", enabled = ready, onClick = { onCustom(url, model, key, true) })
    if (problem != null && ready) QuietButton(text = "Use it anyway", onClick = { onCustom(url, model, key, false) })
}

@Composable
private fun Talk(channel: Channel, onOpen: (Inside) -> Unit, onChannel: (Channel, Map<String, String>) -> Unit) {
    Prose("The chat in Droid works with nothing to set up. Pick another place only if you want to reach the phone from elsewhere.")
    Column(Modifier.padding(top = 20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Door(
            title = "In this app",
            badge = if (channel == Channel.APP) "NOW" else "SIMPLEST",
            detail = "A chat inside Droid. Nothing else to set up.",
            onClick = { onChannel(Channel.APP, emptyMap()) },
        )
        Door(
            title = "Telegram",
            badge = if (channel == Channel.TELEGRAM) "NOW" else null,
            detail = "Message the agent from any device through your own Telegram bot.",
            onClick = { onOpen(Inside.Telegram) },
        )
        Door(
            title = "iMessage or WhatsApp",
            badge = if (channel == Channel.PHOTON) "NOW" else null,
            detail = "Text the agent like a person, through a free Photon project.",
            onClick = { onOpen(Inside.Photon) },
        )
        Door(
            title = "Nowhere, just let my agents in",
            badge = if (channel == Channel.NONE) "NOW" else null,
            detail = "No agent runs on the phone. Claude Code, Codex, Cursor and others drive it over MCP or ACP.",
            onClick = { onChannel(Channel.NONE, emptyMap()) },
        )
    }
}

@Composable
private fun Telegram(onChannel: (Channel, Map<String, String>) -> Unit, onLink: (String) -> Unit) {
    var token by remember { mutableStateOf("") }
    var users by remember { mutableStateOf("") }
    val valid = TELEGRAM_TOKEN.matches(token)
    Prose(
        "In Telegram, message @BotFather, send /newbot, and paste the token it gives you here.",
    )
    QuietButton(text = "Open @BotFather", onClick = { onLink("https://t.me/BotFather") }, color = Ink.accent)
    SectionLabel("BOT TOKEN", Modifier.padding(top = 12.dp, bottom = 8.dp))
    Field(value = token, onValueChange = { token = it.trim() }, hint = "123456789:AA...", secret = true)
    SectionLabel("YOUR TELEGRAM ID", Modifier.padding(top = 20.dp, bottom = 8.dp))
    Field(value = users, onValueChange = { users = it.trim() }, hint = "Leave empty if you do not know it")
    Prose(
        "Once Droid starts, message your bot. Until your id is here it answers nobody, except to tell you " +
            "your id. Paste that into Allowed people in settings.",
    )
    Spacer(Modifier.height(16.dp))
    PrimaryButton(
        text = "Use Telegram",
        enabled = valid,
        onClick = { onChannel(Channel.TELEGRAM, mapOf(Env.TELEGRAM_TOKEN to token, Env.REMOTE_USERS to users)) },
    )
}

@Composable
private fun Photon(onChannel: (Channel, Map<String, String>) -> Unit, onLink: (String) -> Unit) {
    var key by remember { mutableStateOf("") }
    var project by remember { mutableStateOf("") }
    var users by remember { mutableStateOf("") }
    Prose(
        "Photon gives the agent its own iMessage or WhatsApp line. Make a free project at photon.codes, " +
            "then paste its project key and project id here.",
    )
    QuietButton(text = "Open photon.codes", onClick = { onLink("https://photon.codes") }, color = Ink.accent)
    SectionLabel("PROJECT KEY", Modifier.padding(top = 12.dp, bottom = 8.dp))
    Field(value = key, onValueChange = { key = it.trim() }, hint = "pho_sk_...", secret = true)
    SectionLabel("PROJECT ID", Modifier.padding(top = 20.dp, bottom = 8.dp))
    Field(value = project, onValueChange = { project = it.trim() }, hint = "pho_prj_...")
    SectionLabel("WHO MAY TEXT IT", Modifier.padding(top = 20.dp, bottom = 8.dp))
    Field(value = users, onValueChange = { users = it.trim() }, hint = "+447700900123, you@example.com")
    Prose("Phone numbers or emails, separated by commas. Anyone else is ignored.")
    Spacer(Modifier.height(16.dp))
    PrimaryButton(
        text = "Use Photon",
        enabled = key.isNotEmpty() && project.isNotEmpty(),
        onClick = {
            onChannel(
                Channel.PHOTON,
                mapOf(Env.PHOTON_KEY to key, Env.PHOTON_PROJECT to project, Env.REMOTE_USERS to users),
            )
        },
    )
}

@Composable
private fun Permissions(
    readiness: Readiness,
    channel: Channel,
    onGrant: (GrantId) -> Unit,
    onAppInfo: () -> Unit,
    onPosting: () -> Unit,
    onBattery: () -> Unit,
    onTalk: () -> Unit,
) {
    var triedListener by remember { mutableStateOf(false) }
    Prose(
        if (readiness.screen) {
            "Screen is on, so Droid can use the phone. Everything else here is optional."
        } else {
            "This is the one switch Droid needs. Tap Screen, turn on Droid on the page that opens, then come back."
        },
    )
    GroupCard(Modifier.padding(top = 20.dp)) {
        Need(
            label = "Screen",
            detail = "Lets the agent see the screen, tap and scroll. Needed.",
            on = readiness.screen,
            onClick = { onGrant(GrantId.SCREEN) },
        )
    }
    if (!readiness.screen && readiness.restricted) RestrictedHelp("Screen", onAppInfo, Modifier.padding(top = 10.dp))
    SectionLabel("WHERE YOU TALK", Modifier.padding(top = 24.dp, bottom = 8.dp))
    GroupCard {
        SettingRow(label = "Chat", value = where(channel), onClick = onTalk)
    }
    SectionLabel("OPTIONAL", Modifier.padding(top = 24.dp, bottom = 8.dp))
    GroupCard {
        Need(
            label = "Notify me",
            detail = "Droid's questions and replies, while you are in another app.",
            on = readiness.posting,
            onClick = onPosting,
        )
        RowDivider()
        Need(
            label = "Keep running",
            detail = "Stops the battery saver from pausing the agent when the screen is off.",
            on = readiness.battery,
            onClick = onBattery,
        )
        RowDivider()
        Need(
            label = "Keyboard",
            detail = "Lets the agent type. Droid's keyboard has no keys, so switch back to yours to type yourself.",
            on = readiness.keyboard,
            onClick = { onGrant(GrantId.KEYBOARD) },
        )
        RowDivider()
        Need(
            label = "Read notifications",
            detail = "Lets the agent read your notifications, to react to messages and alerts.",
            on = readiness.listener,
            onClick = {
                triedListener = true
                onGrant(GrantId.NOTIFICATIONS)
            },
        )
    }
    if (triedListener && !readiness.listener && readiness.restricted) {
        RestrictedHelp("Read notifications", onAppInfo, Modifier.padding(top = 10.dp))
    }
    Prose("You can turn these on later from the home screen.")
}

@Composable
private fun Need(label: String, detail: String, on: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .pressable(onClick)
            .padding(horizontal = 14.dp, vertical = 13.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Dot(if (on) Ink.live else Ink.accent)
        Column(Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium, color = Ink.text)
            Text(text = detail, style = Type.secondary, color = Ink.dim, modifier = Modifier.padding(top = 2.dp))
        }
        Text(
            text = if (on) "On" else "Off",
            style = MaterialTheme.typography.labelMedium,
            color = if (on) Ink.dim else Ink.accent,
        )
        Chevron()
    }
}

/** The channel as its door on the talk step names it. */
private fun where(channel: Channel): String = when (channel) {
    Channel.APP -> "In this app"
    Channel.TELEGRAM -> "Telegram"
    Channel.PHOTON -> "iMessage or WhatsApp"
    Channel.NONE -> "Only other agents"
}

private const val SHOWN_MODELS = 6
private val TELEGRAM_TOKEN = Regex("[0-9]{5,15}:[A-Za-z0-9_-]{20,80}")
/** Providers with a free tier worth naming in the list. */
private val FREE_TIER = setOf("openrouter", "google_gemini", "nvidia_nim", "mistral")
/** Where each shortlisted provider hands out keys, so nobody has to search for the page. */
private val KEY_PAGES = mapOf(
    "openrouter" to "https://openrouter.ai/keys",
    "google_gemini" to "https://aistudio.google.com/apikey",
    "nvidia_nim" to "https://build.nvidia.com",
    "mistral" to "https://console.mistral.ai/api-keys",
    "deepseek" to "https://platform.deepseek.com/api_keys",
    "anthropic" to "https://console.anthropic.com/settings/keys",
    "openai" to "https://platform.openai.com/api-keys",
    "moonshot" to "https://platform.moonshot.ai",
    "zai" to "https://z.ai/manage-apikey/apikey-list",
)
