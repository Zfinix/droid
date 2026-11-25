# Building from source

## Prerequisites

- JDK 17, the Android SDK with platform 35 and build-tools 35.0.0, and NDK 27.2.12479018
- Rust nightly with `rust-src` (`rustup component add rust-src`), because `build-std` and JSON target specs are nightly features
- An [Aster](https://github.com/Zfinix/aster) checkout beside this repo; `ASTER_REPO` points somewhere else if it is not

## Build and install

```sh
./build-agent.sh     # cross-compiles aster-cli to app/src/main/jniLibs/arm64-v8a/libaster.so
./build-client.sh    # cross-compiles asterctl to libclient.so
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

`grant.sh` grants everything the app cannot ask for itself:

```sh
./grant.sh
```

The script grants the camera and notification permissions, the background and exact-alarm app-ops, notification access and the battery exemption. It also enables and selects the keyboard and enables the accessibility service, then reports any denied grants.

Check that the accessibility service is enabled. The agent needs it to read and control the screen.

### Troubleshooting a source build

| symptom | what to do |
| --- | --- |
| "This build has no agent" | `libaster.so` is missing from the APK: rerun `./build-agent.sh`, rebuild, reinstall |
| `serve is not a verb`, or another verb missing | a stale `asterctl`: `./build-client.sh`, reinstall, then open the app once so the bin symlinks refresh |
| the agent cannot reach the screen | rerun `./grant.sh`, or turn on Droid in Accessibility settings |
| `.env` values do not land | read it back on a debug build: `adb shell run-as dev.aster.probe cat files/.env` |

## Releases

`release.sh` builds a signed release. It rebuilds both native binaries, runs the Gradle release task, verifies the signature and writes the artifact to `dist/`. An APK lands twice, as `asterdroid-<version>.apk` and as `droid.apk`, the name [droid.withaster.dev/download](https://droid.withaster.dev/download) points at through `releases/latest/download/droid.apk`.

```sh
./release.sh apk                  # signed release APK, plus droid.apk
./release.sh aab                  # signed release App Bundle, for Play only
./release.sh apk -v 0.2.0 -c 2    # set the version, then build
./release.sh apk -s -i            # reuse jniLibs, install over adb
```

Signing uses `keystore.properties` and the key it names, both outside git. `./release.sh check` reports the SDK, NDK, build-tools, Aster checkout and signing setup without building anything. `./release.sh version` prints the current version, and `./release.sh notes` prints the matching file from `docs/release-notes/`.

## Releases from CI

`.github/workflows/release.yml` runs the same `release.sh` on GitHub's runners. It checks out Aster beside this repository, installs the NDK, platform 35 and build-tools, cross-compiles both native binaries, then builds and uploads the signed artifacts.

Trigger it by pushing a tag, or by hand from **Actions → release → Run workflow**, where you can pick `apk`, `aab` or both, set the version, and publish a GitHub Release.

```sh
git tag v0.2.0
git push origin v0.2.0
```

A tag push builds both artifacts and publishes a Release with the two APKs, using `docs/release-notes/v0.2.0.md` as the body when that file exists. The AAB stays in the workflow run's artifacts and never goes on the Release, because a phone cannot install it. Add these repository secrets first:

| secret | what it is |
| --- | --- |
| `ANDROID_KEYSTORE_BASE64` | `base64 -i aster-release.jks` |
| `ANDROID_KEYSTORE_PASSWORD` | `storePassword` |
| `ANDROID_KEY_ALIAS` | `keyAlias` |
| `ANDROID_KEY_PASSWORD` | `keyPassword` |

The keystore is written to a file and removed with the runner, so the key never leaves the secrets store. The native build is cached on `target/android-tls` and `target/android-client`, which is what keeps a run under the 90 minute timeout after the first one.

## Permissions

The app declares only what it uses: network, foreground service, wake and boot, exact alarms, battery exemption, camera (for setup codes) and notifications. Notifications are asked for on the first chat message and the camera when scanning. Everything else the agent does goes through the accessibility service or another app's screen, such as Messages or Phone. Accessibility, notification access and the keyboard are Settings toggles, or `grant.sh` on a source build.

The accessibility service requests interactive windows, view IDs, not-important views, key-event filtering and screenshots. It declares `isAccessibilityTool` and sets `notificationTimeout` to 0 to avoid event coalescing.

Touch exploration is disabled because it changes normal tap behaviour and interferes with manual use.

## Repository layout

```
app/src/main/
  AndroidManifest.xml            permissions, services and receivers
  assets/                        AGENTS.md, the flattened skills, providers.json (generated)
  jniLibs/arm64-v8a/             libaster.so (the agent), libclient.so (asterctl)
  kotlin/dev/aster/probe/
    AsterA11yService.kt          capture, gestures, OCR, the socket, the verbs
    AsterAgentService.kt         the supervised agent process
    AsterIme.kt                  the invisible keyboard
    AsterNotifications.kt        the notification listener
    Mirror*.kt                   MediaProjection, the encoder, consent, viewers
    Grid.kt, Vision.kt           lettered cells, blobs, changed regions, the aim read
    MarksOverlay.kt              the map's indices drawn on the live screen
    Shortcuts.kt                 apps, settings, intents, alarms, the store
    Install.kt                   bins, skills, instructions and .env on every start
    Env.kt                       the .env, read and written from the phone
    Models.kt                    the provider catalog, model lists, the child's env
    Sessions.kt, Feed.kt         transcripts and the live feed
    WakeReceiver.kt              `later`, as an alarm and a wakeup file
    MainActivity.kt, ui/         the app
  res/xml/a11y_config.xml        what the accessibility service may do
client/
  src/main.rs                    the CLI, and serve's detached re-exec
  src/socket.rs                  the abstract socket, one line in
  src/server.rs                  the mirror: axum, the websocket, Annex-B to AVCC
  viewer.html                    the mirror page, served from the binary
skills/<name>/SKILL.md           task instructions, shipped into assets
device-AGENTS.md                 device instructions, shipped as AGENTS.md
docs/ANDROID.md                  the longer architecture write-up
target-spec/                     the custom Android target that turns TLS on
build-agent.sh, build-client.sh  the two cross-builds
grant.sh                         every permission and toggle, over adb
```