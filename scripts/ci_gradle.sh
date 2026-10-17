#!/usr/bin/env bash
# Menjalankan ./gradlew di CI. Jika gagal, penyebabnya ("What went wrong", error kompilasi,
# test yang gagal, class R8 yang hilang) ditulis sebagai annotation GitHub Actions sehingga
# langsung terlihat di halaman run tanpa membuka log. Log lengkap disimpan di ci-logs/.
#
# Pemakaian: bash scripts/ci_gradle.sh <task gradle dan opsi...>
set -uo pipefail

mkdir -p ci-logs
LOG="ci-logs/gradle-$(date +%s)-$$.log"

./gradlew --console=plain "$@" 2>&1 | tee "$LOG"
status=${PIPESTATUS[0]}

if [ "$status" -ne 0 ]; then
  summary="$(
    {
      grep -E -A20 'What went wrong' "$LOG" | head -60
      echo '--- baris error ---'
      grep -E '^(e: |ERROR|error:|AAPT)|Missing class|ERROR:' "$LOG" | head -25
      echo '--- test gagal ---'
      grep -E -A6 ' FAILED$' "$LOG" | head -40
    } | cut -c1-300
  )"
  # Karakter khusus perintah workflow harus di-escape agar annotation tidak terpotong.
  escaped="${summary//'%'/'%25'}"
  escaped="${escaped//$'\r'/'%0D'}"
  escaped="${escaped//$'\n'/'%0A'}"
  echo "::error title=Gradle gagal: $*::${escaped}"
fi
exit "$status"
