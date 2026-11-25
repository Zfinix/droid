# Setup

The short path is in the [README](../README.md#install). This page is the same setup in detail: onboarding, each chat app, configuration and troubleshooting. Building from source is in [BUILDING](BUILDING.md).

## Onboarding

Droid walks a new phone through the setup one step at a time. Settings can run it again whenever you want to change something.

1. **Choose a brain.**
   - **Try it free** uses free models through Kilo Gateway, with no account and no key. It allows about 200 requests an hour, around 10 tasks, and runs on a trial service that keeps logs, so keep it away from banking, health and other private screens.
   - **Use my computer's model** finds Ollama or LM Studio on the same Wi-Fi. If it finds neither, it shows the command that makes each listen on the network.
   - **Sign in or use a key.** OpenRouter signs in with nothing to copy. Any other provider, or any OpenAI-compatible address, takes a key, which Droid tries before it saves it. Key pages are in [PROVIDERS](PROVIDERS.md).
2. **Where to talk to it.** The chat in the app needs nothing else and is the default. [Telegram](#telegram) and [WhatsApp or iMessage](#whatsapp-and-imessage) each need one more thing, below. **Nowhere, just let my agents in** runs no agent on the phone, for when only agents on a computer drive it ([Connect a computer](../README.md#connect-a-computer)).
3. **What Droid may use.** Only **Screen** is needed: it is the Accessibility service that lets the agent see the screen and tap. The rest are optional, and each says what it is for. If the Screen switch is greyed out, see [Troubleshooting](#troubleshooting).
4. **Done.** Say what you want done:

```txt
Open settings and turn on Do Not Disturb.
```

To skip the typing, make a setup link on a computer at [droid.withaster.dev/setup](https://droid.withaster.dev/setup) and scan its QR code with the phone's camera. It carries the model, its key and the chat app. Droid shows every change before it writes anything, and a link works once, for 15 minutes.

## Approvals

The agent asks before anything risky, such as sending a message, buying something or deleting data. In the app and on Telegram it shows buttons. On WhatsApp and iMessage it ends with "Reply yes, always, or no." Until you answer, it waits.

## Telegram

Choose **Telegram** under **Where to talk to it**, in onboarding or by running it again from Settings.

1. In Telegram, open [@BotFather](https://t.me/BotFather) and send `/newbot`. Follow the prompts to name the bot and pick its username. BotFather replies with a bot token.
2. In Droid, paste the token into **Bot token**. Leave **Your Telegram ID** empty if you do not know it, and tap **Use Telegram**.
3. In Telegram, open your new bot (not BotFather) and send `hello`. Until your id is allowed, the bot only tells you your id:

   ```txt
   This bot isn't set up yet. Your user id is 8675309. Restart it with --user 8675309 to allow it.
   ```

4. In Droid's settings, paste that number into **Allowed ids** and save. Use the number, not your username or the bot token. Separate several ids with commas. The reply mentions `--user`; on the phone, Allowed ids is the same thing.
5. Send the bot an instruction. Use `/help` for chat commands, or `/mirror` to watch the phone in a browser.

## WhatsApp and iMessage

[Photon](https://photon.codes) gives the agent its own WhatsApp or iMessage line. One Photon project carries both. Choose **iMessage or WhatsApp** under **Where to talk to it**, in onboarding or by running it again from Settings.

1. Make a free project at [photon.codes](https://photon.codes) and copy its project key (`pho_sk_...`) and project id (`pho_prj_...`).
2. In Droid, paste them into **Project key** and **Project ID**.
3. Under **Who may text it**, add the phone numbers or emails allowed to message the agent, separated by commas, with the country code on numbers (`+447700900123`). Anyone else is ignored. Tap **Use Photon**.
4. Text the agent's line from one of those numbers.

## Configure from your computer

You can push a `.env` over adb instead of typing values on the phone. It is merged into the phone's settings, and only the names in the file change. Android 10 and older ignore a pushed `.env`, because other apps can write that folder there; use the app or a setup link instead.

```sh
cat > .env <<'EOF'
ASTER_CHANNEL=telegram
ASTER_TELEGRAM_TOKEN=YOUR_BOT_TOKEN
ASTER_REMOTE_USERS=YOUR_TELEGRAM_USER_ID
<PROVIDER>_API_KEY=YOUR_PROVIDER_KEY
EOF
adb push .env /sdcard/Android/data/dev.aster.probe/files/.env
```

The file is read the next time the agent starts. For Photon, use `ASTER_CHANNEL=photon` with `ASTER_PHOTON_KEY`, `ASTER_PHOTON_PROJECT` and `ASTER_REMOTE_USERS`.

## Configuration

The agent reads these from `files/.env` inside the app. The settings sheet, setup links and pushed files all write the same file, and each keeps the names it does not set.

| variable | what it is |
| --- | --- |
| `ASTER_CHANNEL` | where people talk to the agent: `app`, `telegram`, `photon` or `none`. Unset, it is `telegram` when a bot token is set and `app` otherwise |
| `ASTER_TELEGRAM_TOKEN` | the Telegram bot token |
| `ASTER_PHOTON_KEY`, `ASTER_PHOTON_PROJECT` | the Photon project key and project id |
| `ASTER_REMOTE_USERS` | who may message the agent, separated by commas: Telegram ids, or phone numbers and emails for Photon |
| `<PROVIDER>_API_KEY` | whichever key the chosen provider wants; [PROVIDERS](PROVIDERS.md) lists them |
| `ASTER_BASE_URL`, `ASTER_MODEL`, `ASTER_EFFORT` | the provider, model and effort, set by the app's pickers |
| `ASTER_MCP_TOKEN` | turns on access for other agents (MCP); the key clients send, 32 characters or more |
| `ASTER_ACP_TOKEN` | turns on access for editors (ACP); the same rules |
| `ASTER_MCP_PORT`, `ASTER_MCP_BIND` | where MCP listens, `7071` and `127.0.0.1` by default; Over Wi-Fi sets the bind to the Wi-Fi address |
| `ASTER_ACP_PORT`, `ASTER_ACP_BIND` | where ACP listens, `7072` and `127.0.0.1` by default |
| `ASTER_RELAY_URL` | a relay you host yourself ([RELAY](RELAY.md#self-hosting)) |
| `ASTER_AGENT_CMD` | a shell command run in place of the bundled agent ([USING](USING.md#any-harness)) |

The app's own provider and model choices take precedence over the same names in `.env`.

## Troubleshooting

| symptom | what to do |
| --- | --- |
| the browser will not open the download, or Android blocks the install | allow installs from your browser when Android asks, or under **Settings > Apps > (your browser) > Install unknown apps** |
| Play Protect says the app is unknown or unsafe | tap **More details**, then **Install anyway**. Droid is not on the Play Store, so Play Protect has not scanned it |
| "App not installed" or "package appears to be invalid" | check you downloaded `droid.apk`, not an `.aab`, and that the phone is 64-bit ARM on Android 8 or later |
| the Screen switch is greyed out, or Android says "Restricted setting" | tap the greyed switch once, then **Settings > Apps > Droid**, the three-dot menu, **Allow restricted settings**, and turn Screen on again |
| the agent says it cannot reach the screen | Screen access is off. Turn it on again from Droid's home screen. If it keeps turning off, turn on **Keep running** so battery saving leaves Droid alone |
| nothing works after a reboot | Droid starts again after a reboot. If it does not, open the app once, and allow Droid in your phone's own autostart setting if it has one |
| the free model stops with a rate limit | the free tier allows about 200 requests an hour. Wait, or switch to your computer's model or a key in Settings |
| the Telegram bot never replies | check Droid's status says it is connected to Telegram, that you messaged the bot whose token you entered, and that **Allowed ids** has the number from the bot's reply |
| the Telegram bot only replies with your id | paste that number into **Allowed ids** in settings |
| WhatsApp or iMessage never replies | check the project key and project id, and that your number is under Allowed people with its country code |
| the agent stopped and is waiting | it is asking for approval. Answer the buttons, or reply yes, always or no |
| typing reports posted and nothing arrives | the field handles its own input, as a terminal does; there is no way around it from here |
| `/mirror` says the port is held | something else on the phone listens on 7070; stop it |
| the mirror says capture was not allowed, or the agent says the screen is locked | a PIN or pattern lock is up. Unlock the phone; taps do not reach apps behind it |

Still stuck? [Open an issue](https://github.com/Zfinix/droid/issues) with your phone model, Android version and Droid version (in Settings). Logs help, from a computer with adb:

```sh
adb logcat -d -s ASTEREYES ASTERAGENT ASTERPROBE ASTERINSTALL ASTERWAKE aster-mirror > droid.log
```
