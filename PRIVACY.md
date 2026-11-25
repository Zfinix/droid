# Privacy

Droid has no server of its own and no account. What leaves the phone goes to the services you choose, and this page lists each one.

## The model provider

To decide what to do, the agent sends the model your messages, the text it reads off the screen, and screenshots when it needs to see. That goes to whichever provider you picked, under that provider's own terms.

- **Try it free** sends it through Kilo Gateway to NVIDIA's trial service, which keeps logs. Keep it away from banking, health and other private screens.
- **Your computer's model** (Ollama, LM Studio) keeps it on your own network.
- **A key** sends it to that provider. Plain `http` is only allowed to addresses on your own network, so a key never crosses the internet without TLS.

## The chat app

- **In the app**, the conversation stays on the phone, apart from what goes to the model.
- **Telegram** carries your messages, the agent's replies and any screenshot it sends you, through your own bot.
- **WhatsApp and iMessage** go through your [Photon](https://photon.codes) project, which carries the same.

## Computers you connect

Agents on a computer (MCP) and editors (ACP) can see and use the phone once they hold its key. Over USB they reach it through adb; over Wi-Fi or Tailscale, on your network. Nothing passes through us.

A relay carries MCP and ACP traffic with TLS to each end, not end to end, so the relay and Cloudflare can read calls, replies and screenshots as they pass. The relay stores client names, scopes, hashed tokens and timestamps, and keeps no bodies. Aster Relay is coming soon; a relay you host yourself sees what this paragraph says, on your own account. [docs/RELAY.md](docs/RELAY.md#threat-model) has the detail.

## Where keys are kept

- **On the phone**, in `files/.env` inside the app's private storage, where other apps cannot read it. Droid opts out of Android backup and device transfer, so the file never lands in a cloud backup or on another phone.
- **MCP and ACP keys** are 64 characters, made on the phone when you turn each one on. Turning it off deletes the key. Copied keys are marked sensitive, so Android 13 and later hide them in the clipboard preview.
- **On a computer**, `aster-droid pair` saves them in `~/.config/aster-droid/credentials.json`, mode 0600 in a 0700 directory. `setup` points each client at `aster-droid`, so the key stays out of the clients' own config files unless you choose `--http`.

Whoever holds a key controls the phone: calls, messages, money. Treat it like a password.

## What Droid does not send

Droid has no analytics, crash reporting or tracking of its own, on the phone or in the CLI. It does read text on the screen with Google's ML Kit, which runs on the phone. Google says ML Kit sends metrics about the library's performance and use to Google ([ML Kit terms](https://developers.google.com/ml-kit/terms)); it does not send the text it reads. To tell you about updates, Droid asks GitHub for the newest release number.

## Questions

[Open an issue](https://github.com/Zfinix/droid/issues), or for anything sensitive, see [SECURITY.md](SECURITY.md).
