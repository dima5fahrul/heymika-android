# HeyMika: handoff

Terakhir diperbarui: 2026-10-31. Update file ini setiap fase selesai atau ada keputusan baru.

## Status

**P0 (design system + system states + CI): selesai 2026-10-29, sudah diverifikasi ulang.**

Hasil gate saat itu: ktlintCheck lolos (semua source set `.kt`), 201 unit test (196 `:core` + 5 `:app`)
0 gagal, JaCoCo `:core` jadi, build debug + 3 dynamic feature + release APK (~4,96 MB) + AAB (~5,87 MB)
berhasil, `check-release-apk.sh` OK (galeri debug tidak masuk release).

Isi P0:
- `HeyMikaTheme` (light-only), Nunito Sans (400-900) di `core/src/main/res/font/`, token warna/ukuran/
  sudut/tone/motion. Akses lewat `HeyMikaTheme.colors|typography|shapes|dimens|tones|reducedMotion`.
- Modifier komik di `core/ui/modifier/`: `comicSurface`, `subtleSurface`, `comicFocusRing`,
  `comicPressSurface`, `pressTranslate`.
- Komponen di `core/ui/component/{foundation,input,selection,navigation,overlay,picker,timer,display,mascot,state}/`:
  `ComicButton` (Primary/Secondary/Outline/Ghost/Danger/Tonal; Large/Medium/Small), input & password,
  top bar, `HeyMikaBottomNavBar`, dialog, bottom sheet, snackbar, picker, `ResendCooldownButton`,
  `CountdownTimerPill`, pill/chip/accordion/progress/skeleton, `MikaAvatar` (placeholder), `MikaSpeechBubble`,
  `MikaErrorState`/`MikaEmptyState`/`MikaLoadingState`, `OfflineBanner`.
- Domain/data: `AppError`, `Quota`, `MikaPose`, `ErrorMapper`, `ConnectivityObserver`, `MarkupParser` +
  `RichText`, `MaxLength`.
- Galeri debug "HeyMika DS" (`app/src/debug/.../debug/gallery/`).
- CI `.github/workflows/ci.yml`, `docs/ci.md`, `scripts/ci/check-release-apk.sh`.
- Rename `AiLearn*` ke `HeyMika*` (app, root composable, nav host). Dark mode dihapus.

## Menunggu jawaban user (P1 belum boleh mulai)

1. **Fidelity desain.** Implementasi sekarang ikut DESIGN.md secara ketat: kartu besar (mis. kartu error)
   pakai border navy 3dp + bayangan oranye tegas, tombol oranye (Secondary) punya border navy. Mockup PNG
   lebih lembut: kartu putih border tipis, tombol "Tanya Mika" di Beranda tanpa border. Rekomendasi: ikut
   PNG; gaya komik tebal hanya di tempat yang tebal di mockup (mis. kartu rekomendasi di hero Beranda).
   Perubahan cukup di token/modifier (`comicSurface`/`subtleSurface`, varian `ComicButton`), bukan
   tulis ulang komponen. Bandingkan dengan `docs/mockups/stitch/beranda_heymika_mobile/screen.png` dan
   `docs/mockups/stitch/system_states_error_handling_heymika_mobile/screen.png`.
2. **Go-ahead P1.**

## Berikutnya: P1

Lingkup lengkap di plan bagian "P1 — Navigation contract, secure session, sign-in, verification &
onboarding". Layar: login, register, verifikasi_email, onboarding.

Penyesuaian karena keputusan PO (plan bagian 0), mengalahkan isi P1 di plan:
- Tidak ada backend: auth/session/onboarding lewat fake data source + fixture JSON, interface sama
  dengan remote nanti.
- Google sign-in (Credential Manager) ditunda sampai backend ada.
- `AuthSplitShell` untuk tablet ditunda ke v2 (v1 phone-only).
- App Link diuji dengan host placeholder lewat `adb`; `assetlinks.json` cukup template.

Rename yang memang dijadwalkan di P1: `AiLearnDatabase`/`AiLearn.db` menjadi `HeyMikaDatabase`/`HeyMika.db`
(SQLCipher), DataStore `ailearn_settings` menjadi `heymika_settings`, fallback key `ailearn.api.base.url`
di `core/build.gradle.kts` dihapus. Juga: `FeatureInstallDialog` diganti `FeatureDestinationHost`.

## Masalah yang diketahui

- Home lama menampilkan kartu "Koneksi Internet Terputus" karena masih fetch ke host contoh. Hilang saat
  data dummy masuk (P1). Bottom nav lama 3 tab (Beranda/Favorit/Pengaturan) baru diganti di P3.
- Teks kuota hard-code "00:00 WIB" (belum pakai `resetAt`).
- Tombol Tonal disabled dan `ComicCard` disabled terlalu mirip versi enabled.
- DatePicker pakai `remember`, belum `rememberSaveable`.
- `MainDispatcherRule` hanya ada di test `:core`; modul lain perlu test fixtures bersama.
- Temuan review P0 yang sengaja dilewati: lingkaran ikon lonceng di top bar, warna badge, mapping TLS ke
  Offline, testFixtures.

## Butuh dari user

- Aset maskot Mika asli per pose (PNG/WebP/SVG). Sekarang kucing vektor sementara; link gambar di mockup 403.
- Konfirmasi aturan skor password STRONG (`PasswordPolicy`).
- SM-2 atau FSRS untuk flashcard (dibutuhkan di P9).

## Log keputusan

- 2026-10-28: struktur meniru Capstone Android Expert dengan Compose; package `com.ai.heymika`; nama "HeyMika".
- 2026-10-28 (PO, plan bagian 0): fitur STORY dibuang; tanpa backend (dummy + clean architecture);
  bottom nav ikut mockup Beranda; primary `#0075E8` / primaryText `#005AB5`; semua layar mockup dulu,
  pembayaran di akhir (P11); tablet/voice/TTS/edit catatan/ekspor dll. masuk v2.
- 2026-10-29: P0 selesai dan diverifikasi.
- 2026-10-31: konteks dipindah ke repo (`CLAUDE.md`, file ini, mockup di `docs/mockups/stitch/`) supaya
  bisa dilanjutkan di device lain.

## Cara kerja yang sudah terbukti (P0)

Satu workflow multi-agent per fase: agent fondasi menulis kontrak (`docs/design-system/CONTRACT.md`,
termasuk peta kepemilikan file) -> builder paralel per area file -> integrator -> 2 reviewer -> fixer.
Setelah itu sesi utama menjalankan semua gate sendiri dan mengecek screenshot sebelum lapor ke user.
