#!/usr/bin/env bash
# Fails when the release APK ships the debug design gallery (plan P0 release-dex check).
# Usage: scripts/ci/check-release-apk.sh [path/to/app-release.apk]
set -euo pipefail

apk="${1:-$(ls app/build/outputs/apk/release/*.apk | head -n 1)}"
sdk="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-}}"
analyzer="$sdk/cmdline-tools/latest/bin/apkanalyzer"
if [ ! -x "$analyzer" ]; then
  echo "apkanalyzer not found under \$ANDROID_HOME ($sdk)" >&2
  exit 2
fi

packages="$("$analyzer" dex packages --defined-only "$apk")"
if [ "$(grep -c '^C ' <<< "$packages" || true)" -eq 0 ]; then
  echo "No classes listed for $apk; is it a valid APK?" >&2
  exit 2
fi
if leaked="$(grep -E 'com\.ai\.heymika\.debug|DesignGallery' <<< "$packages")"; then
  head -n 20 <<< "$leaked"
  echo "::error::$apk contains debug gallery classes" >&2
  exit 1
fi

manifest="$("$analyzer" manifest print "$apk")"
if leaked="$(grep -E 'DesignGallery|com\.ai\.heymika\.debug' <<< "$manifest")"; then
  head -n 20 <<< "$leaked"
  echo "::error::$apk declares the debug gallery in its manifest" >&2
  exit 1
fi
launchers="$(grep -c 'android.intent.category.LAUNCHER' <<< "$manifest" || true)"
if [ "$launchers" -ne 1 ]; then
  echo "::error::$apk has $launchers LAUNCHER entries; only MainActivity may have one" >&2
  exit 1
fi

echo "OK: $apk has no debug gallery classes and one launcher activity"
