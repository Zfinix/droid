#!/bin/sh
# Runs the agent as a Telegram bot. Everything it keeps (memory, skills,
# sessions, cron) lives under $HOME, so mount a volume there to keep it.

set -eu

if [ -z "${ASTER_TELEGRAM_TOKEN:-}" ]; then
  echo "ASTER_TELEGRAM_TOKEN is not set. Create a bot with @BotFather and add its token." >&2
  exit 1
fi

# With nobody allowed, the bot only tells each sender their id and runs nothing.
if [ -z "${ASTER_REMOTE_USERS:-}" ]; then
  echo "ASTER_REMOTE_USERS is not set. Message the bot once; it replies with your user id. Add it and redeploy." >&2
fi

WORK="$HOME/work"
mkdir -p "$HOME/.aster" "$WORK"
[ -f "$WORK/AGENTS.md" ] || cp /opt/aster/AGENTS.md "$WORK/AGENTS.md"

cd "$WORK"
exec aster remote telegram --mode "${ASTER_MODE:-manual}"
