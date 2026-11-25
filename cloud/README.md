# Aster in the cloud

Your own Aster agent on Telegram, running in a small container. It downloads
the released `aster` binary, so nothing compiles.

## What you need

| Variable | Value |
| --- | --- |
| `ASTER_TELEGRAM_TOKEN` | Bot token from [@BotFather](https://t.me/BotFather) |
| `ASTER_API_KEY` | Key for your model provider |
| `ASTER_BASE_URL` | Provider endpoint, e.g. `https://openrouter.ai/api/v1` |
| `ASTER_MODEL` | Model id, e.g. `z-ai/glm-5.3-flash` |
| `ASTER_REMOTE_USERS` | Your Telegram user id. Leave it empty on the first deploy |
| `ASTER_MODE` | Optional. `manual` (default) asks before each action with buttons in the chat |

Mount a volume at `/data`. Memory, skills, sessions and scheduled check-ins live there.

## First run

1. Deploy with `ASTER_REMOTE_USERS` empty.
2. Message your bot. It replies with your user id and does nothing else.
3. Set `ASTER_REMOTE_USERS` to that id and redeploy. Start chatting.

## Railway

Create a service from this repo with root directory `cloud`, add the variables,
and attach a volume at `/data`. `railway.json` does the rest.

## Fly.io

```sh
cd cloud
fly launch --copy-config --no-deploy
fly volumes create aster_data --size 1
fly secrets set ASTER_TELEGRAM_TOKEN=... ASTER_API_KEY=... ASTER_BASE_URL=... ASTER_MODEL=...
fly deploy
```

## Anywhere with Docker

```sh
docker build -t aster-cloud cloud
docker run -d --restart always -v aster-data:/data --env-file .env aster-cloud
```

Pin a different release with `--build-arg ASTER_VERSION=x.y.z`.
