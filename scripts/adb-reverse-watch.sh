#!/usr/bin/env bash
#
# Mode develop: jaga agar SEMUA emulator/HP yang tersambung selalu meneruskan port 8000 (localhost
# di perangkat) ke `php artisan serve` di Mac.
#
# Penerusan `adb reverse` hilang setiap emulator/HP dimulai ulang, kabel USB dicabut, atau adb server
# di-restart (Android Studio sering melakukannya). Biarkan skrip ini berjalan di satu terminal selama
# develop; ia memasang ulang penerusan secara otomatis. Hentikan dengan Ctrl+C.
#
# Pemakaian:  scripts/adb-reverse-watch.sh [port]     (default 8000)

set -u
PORT="${1:-8000}"
ADB="${ANDROID_HOME:-$HOME/Library/Android/sdk}/platform-tools/adb"
command -v "$ADB" >/dev/null 2>&1 || ADB="adb"

echo "Mengawasi perangkat: tcp:$PORT di perangkat -> localhost:$PORT di komputer ini (Ctrl+C untuk berhenti)"
while true; do
    for serial in $("$ADB" devices 2>/dev/null | awk 'NR > 1 && $2 == "device" { print $1 }'); do
        if ! "$ADB" -s "$serial" reverse --list 2>/dev/null | grep -q "tcp:$PORT tcp:$PORT"; then
            if "$ADB" -s "$serial" reverse "tcp:$PORT" "tcp:$PORT" >/dev/null 2>&1; then
                echo "$(date +%H:%M:%S)  $serial: penerusan port $PORT dipasang"
            fi
        fi
    done
    sleep 3
done
