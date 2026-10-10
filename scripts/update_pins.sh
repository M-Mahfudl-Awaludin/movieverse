#!/usr/bin/env bash
# Memperbarui / memverifikasi certificate pin untuk api.themoviedb.org.
#
# Skrip ini terhubung ke server TMDB, mengambil seluruh rantai sertifikat, menghitung pin
# (SHA-256 dari public key / SPKI, base64) tiap sertifikat, lalu menulis ulang
# core/src/main/java/com/mahfudl/movieverse/core/security/CertificatePins.kt.
#
# Pin yang ditulis = leaf + intermediate + root yang benar-benar dikirim server saat ini,
# ditambah pin root CA cadangan yang sudah ada, supaya aplikasi tetap bisa terhubung ketika
# sertifikat diperpanjang.
#
# Kebutuhan: bash, openssl, python3, akses internet.
# Pemakaian : scripts/update_pins.sh [host] [--check]
#   --check   hanya memeriksa apakah rantai server cocok dengan pin saat ini (exit 1 bila tidak).
set -euo pipefail

HOST="${1:-api.themoviedb.org}"
MODE="${2:-write}"
PORT="${PIN_PORT:-443}"   # hanya diubah saat pengujian lokal
ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
TARGET="$ROOT_DIR/core/src/main/java/com/mahfudl/movieverse/core/security/CertificatePins.kt"
WORK="$(mktemp -d)"
trap 'rm -rf "$WORK"' EXIT

echo ">> Mengambil rantai sertifikat dari $HOST:$PORT ..."
openssl s_client -connect "$HOST:$PORT" -servername "$HOST" -showcerts </dev/null 2>/dev/null \
  | awk -v dir="$WORK" '
      /BEGIN CERTIFICATE/ { n++; out = sprintf("%s/cert%02d.pem", dir, n) }
      out { print > out }
      /END CERTIFICATE/   { close(out); out = "" }'

shopt -s nullglob
CERTS=("$WORK"/cert*.pem)
if [ "${#CERTS[@]}" -eq 0 ]; then
  echo "!! Tidak ada sertifikat yang berhasil diambil dari $HOST. Periksa koneksi internet Anda." >&2
  exit 2
fi

pin_of() {
  openssl x509 -in "$1" -pubkey -noout \
    | openssl pkey -pubin -outform der \
    | openssl dgst -sha256 -binary \
    | openssl enc -base64
}

: > "$WORK/live_pins.txt"
for cert in "${CERTS[@]}"; do
  subject="$(openssl x509 -in "$cert" -noout -subject | sed 's/^subject= *//')"
  pin="$(pin_of "$cert")"
  echo "   sha256/$pin  <-  $subject"
  printf '%s\t%s\n' "$pin" "$subject" >> "$WORK/live_pins.txt"
done

if [ "$MODE" = "--check" ]; then
  python3 -I - "$TARGET" "$WORK/live_pins.txt" <<'PY'
import re, sys
current = set(re.findall(r'"sha256/([^"]+)"', open(sys.argv[1]).read()))
live = [line.split('\t')[0] for line in open(sys.argv[2]).read().splitlines() if line]
matched = [p for p in live if p in current]
if matched:
    print(f"OK: {len(matched)} dari {len(live)} sertifikat server cocok dengan pin aplikasi.")
    sys.exit(0)
print("GAGAL: tidak ada sertifikat server yang cocok dengan pin di CertificatePins.kt.")
print("Jalankan scripts/update_pins.sh tanpa --check untuk memperbarui pin.")
sys.exit(1)
PY
  exit $?
fi

python3 -I - "$TARGET" "$WORK/live_pins.txt" "$HOST" <<'PY'
import re, sys
target, live_file, host = sys.argv[1], sys.argv[2], sys.argv[3]
source = open(target, encoding="utf-8").read()

live = []
for line in open(live_file, encoding="utf-8").read().splitlines():
    if line:
        pin, subject = line.split("\t", 1)
        live.append((pin, subject))

# Pertahankan pin cadangan (root CA) yang sudah ada.
existing = re.findall(r'^\s*(?://\s*(.*)\n\s*)?"sha256/([^"]+)"', source, re.M)
lines, seen = [], set()
lines.append("        // Rantai sertifikat server saat skrip dijalankan (leaf, intermediate, root)")
for pin, subject in live:
    if pin not in seen:
        lines.append(f'        // {subject}')
        lines.append(f'        "sha256/{pin}",')
        seen.add(pin)
backups = [p for _, p in existing if p not in seen]
if backups:
    lines.append("        // Pin cadangan: public key CA (root) yang umum dipakai TMDB")
    for pin in backups:
        lines.append(f'        "sha256/{pin}",')
        seen.add(pin)
lines[-1] = lines[-1].rstrip(",")

body = "\n".join(lines)
new_list = f"    val PINS: List<String> = listOf(\n{body}\n    )"
source = re.sub(r"    val PINS: List<String> = listOf\(.*?\n    \)", new_list, source, flags=re.S)
source = re.sub(r'const val HOST = "[^"]*"', f'const val HOST = "{host}"', source)
open(target, "w", encoding="utf-8").write(source)
print(f"Ditulis: {target} ({len(seen)} pin)")
PY

echo ">> Selesai. Rebuild aplikasi agar pin baru dipakai."
