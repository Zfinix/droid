# Security

Droid can read the screen, tap, type, send messages and pay for things on a phone, so a hole in it matters. Thank you for looking.

## Reporting

Report a vulnerability privately at [github.com/Zfinix/droid/security/advisories/new](https://github.com/Zfinix/droid/security/advisories/new). Please do not open a public issue for it.

Say what you found, how to reproduce it, and the Droid version (in Settings), Android version and, for the CLI, the `@withaster/droid` version (`npx @withaster/droid --version`).

## In scope

- The Android app, `dev.aster.probe`, from the latest release.
- `asterctl` and its MCP, ACP and relay servers on the phone.
- The `@withaster/droid` CLI.
- The relay Worker in `relay/`.
- Setup links from `droid.withaster.dev/setup`.

Prompt injection, where text on the screen talks the agent into something, is a known limit of agents that read untrusted content. Report it when it gets past an approval the agent should have asked for.

## Supported versions

Only the latest release gets fixes. Update from [droid.withaster.dev/download](https://droid.withaster.dev/download).
