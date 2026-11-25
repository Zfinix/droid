# droid.withaster.dev

The Droid website: a static site with no build step, served by Cloudflare Workers static assets.

| Path | What it is |
| --- | --- |
| `index.html` | The landing page, with `styles.css`, `ios.css`, `sorbet.css` and `app.js` |
| `setup/` | The setup link maker: builds a `/s#1.<payload>` link and its QR code in the browser |
| `s/index.html` | Where a setup link (`/s#1.<payload>`) lands when Droid is not there to open it |
| `privacy/` | What the app and the site send where |
| `404.html` | Served for any path that does not exist |
| `_redirects` | `/download` goes to `droid.apk` on the latest GitHub release |
| `_headers` | Security headers, and a policy that blocks network requests on `/s/` and `/setup/` |
| `qr.js`, `vendor/qrcode.js` | QR codes drawn in the page, with a pinned copy of qrcode-generator 1.4.4 (MIT) |
| `.well-known/assetlinks.json` | Lets Android verify the `https://droid.withaster.dev/s` app link for `dev.aster.probe` |
| `robots.txt`, `sitemap.xml` | Keeps setup links out of search and lists the public pages |
| `og.png`, `favicon.ico`, `apple-touch-icon.png` | Social card and icons |
| `img/`, `mascot/`, `logos/` | Only the assets the pages use; mascots ship as WebP with a PNG fallback |

## Run it locally

```sh
cd site
python3 -m http.server 8080
```

Then open http://localhost:8080. Every path in the pages works from the site root, so serve this folder, not the repo. A plain file server ignores `_redirects` and `_headers`; `npx wrangler dev` applies them.

## Setup links

A setup link carries the whole setup in the URL fragment, which a browser never sends to a server. `setup/setup.js` builds one the way `SetupLink.kt` reads it: JSON with `v`, `exp` (15 minutes ahead), a random nonce `n` and the parts the person filled in (`p` for the model, `ch` for the chat app, `mcp`, `relay`), raw-deflated with `CompressionStream` and base64url-encoded. On a phone with Droid installed, Android opens the app straight away, because `assetlinks.json` names the release signing certificate. Anywhere else the browser loads `s/index.html`, which only checks that a fragment is there: on a computer it shows the link as a QR code, and on Android it offers an `asterdroid://setup` button. It never decodes or sends the payload, and both pages are barred from network requests by their Content Security Policy.

The fingerprint in `assetlinks.json` is the SHA-256 of the release certificate. If the release key ever changes, read the new one with

```sh
keytool -list -v -keystore aster-release.jks -alias <alias> | grep SHA256
```

and replace it. Debug builds are signed with a different key, so the link opens them only after you pick Droid by hand.

## Deploy

`wrangler.jsonc` serves this folder as static assets under the Worker name `asterdroid-web`, the one already behind `droid.withaster.dev`, so a deploy replaces the live site. The custom domain route and the account are set in the file, and observability is on. `.assetsignore` keeps the config and this README out of the upload.

```sh
cd site
npx wrangler deploy
```

With `html_handling` set to `auto-trailing-slash`, `/s` redirects to `/s/` and the browser keeps the fragment.

`/download` only works once the latest release has an asset named exactly `droid.apk`. The APK is too big for static assets (25 MiB per file), so it stays on GitHub.
