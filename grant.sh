#!/bin/sh
# Grant the permissions and service toggles the app declares, via adb.
set -u

PKG=dev.aster.probe
A11Y=$PKG/$PKG.AsterA11yService
IME=$PKG/.AsterIme
LISTENER=$PKG/$PKG.AsterNotifications

failed=""

try() {
  out="$(adb shell "$@" 2>&1)"
  case "$out" in
    *Exception*|*Error*|*error*|*"Unknown permission"*|*"not found"*)
      failed="$failed\n  $* -> $(printf '%s' "$out" | head -1)"
      ;;
  esac
}

# Runtime permissions the app declares (android.permission.*)
PERMISSIONS="
CAMERA
POST_NOTIFICATIONS
"

for p in $PERMISSIONS; do
  try pm grant "$PKG" "android.permission.$p"
done

# Sorted app operations (appops)
APPOPS="
ACCESS_RESTRICTED_SETTINGS
RUN_ANY_IN_BACKGROUND
RUN_IN_BACKGROUND
SCHEDULE_EXACT_ALARM
START_FOREGROUND
"

for op in $APPOPS; do
  try appops set "$PKG" "$op" allow
done

# Service toggles and component enabling
try cmd notification allow_listener "$LISTENER"
try dumpsys deviceidle whitelist +$PKG
try ime enable "$IME"
try ime set "$IME"

# Accessibility service enabling
current="$(adb shell settings get secure enabled_accessibility_services | tr -d '\r')"
case "$current" in
  *"$A11Y"*) ;;
  null|"") try settings put secure enabled_accessibility_services "$A11Y" ;;
  *) try settings put secure enabled_accessibility_services "$current:$A11Y" ;;
esac
try settings put secure accessibility_enabled 1

# Report failures and denied runtime permissions
if [ -n "$failed" ]; then
  printf 'refused:%b\n' "$failed"
fi

adb shell dumpsys package "$PKG" | grep -E "granted=false" | sed 's/^ *//' | sort -u | sed 's/^/still denied: /'
echo "done"
