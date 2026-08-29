# Phone Info — one-tap launcher for Android's *#*#4636#*#* screen

## What this is
A single-Activity app. Tapping its icon opens Android's built-in
"Phone information" testing screen (the same screen `*#*#4636#*#*`
opens) and then immediately closes itself, so Back goes straight to
the home screen. It does not touch any settings itself — it just
opens Android's own UI so you can pick NR/LTE modes manually.

## How it works (verified against AOSP source, not guessed)
- The `*#*#4636#*#*` dialer code has historically worked by
  broadcasting `android.provider.Telephony.SECRET_CODE`. Since
  Android 8 (Oreo) that broadcast is a **protected broadcast** sent
  only by the system phone process — third-party apps are not
  allowed to send it (confirmed in the AOSP manifest for
  `packages/services/Telephony`).
- What that broadcast (and the dialer code) ultimately opens is a
  plain, ordinary Activity: `com.android.phone.settings.RadioInfo`,
  package `com.android.phone`.
- In the current AOSP source for that package, this Activity is
  declared:
  ```xml
  <activity
      android:name=".settings.RadioInfo"
      android:label="@string/phone_info_label"
      android:exported="true"
      android:theme="@style/RadioInfoTheme">
      <intent-filter>
          <action android:name="android.intent.action.MAIN" />
          <category android:name="android.intent.category.DEVELOPMENT_PREFERENCE" />
      </intent-filter>
  </activity>
  ```
  `exported="true"`, and — unlike several neighboring activities in
  the same file (EUICC screens, OTASP) — **no `android:permission`
  attribute**. That means any app can start it directly.
- Starting another app's exported Activity by explicit component name
  does not require a `<queries>` declaration under Android's
  package-visibility rules (Android's own developer docs are explicit
  about this), so no `<queries>` element is needed either.
- Moto G34 5G ships close to stock Android with Google Mobile
  Services, which is why this repo doesn't fork `com.android.phone`
  or the Dialer.

So the app just does:
```kotlin
Intent().apply {
    setClassName("com.android.phone", "com.android.phone.settings.RadioInfo")
}.let { startActivity(it) }
```

## Try the zero-APK route first
Some launchers (stock AOSP/Pixel launcher, Nova, Lawnchair) expose a
built-in **"Activities"** widget: long-press the home screen → Widgets
→ look for "Activities" → drag it to the home screen → it lists every
activity on the device, including `RadioInfo` under the Phone /
Telephony app → select it → you get a home-screen icon with no APK
at all. Whether Motorola's own launcher on the G34 includes this
widget isn't something I can verify without the device in hand — it's
worth trying for 30 seconds before building the APK below.

## Permissions requested: none
`AndroidManifest.xml` has zero `<uses-permission>` lines. No
INTERNET, no PHONE_STATE, no storage, nothing. Concretely:
- **Internet access:** impossible. Without the `INTERNET` permission,
  the Linux user/group Android assigns this app's process is not in
  the group allowed to open network sockets — this is a kernel-level
  block, not just a manifest formality.
- **Personal data collection/transmission:** none. The app has no
  analytics, crash reporting, or network code of any kind, and no
  permission to read contacts, location, SMS, etc. even if it wanted
  to.

## Components declared
Exactly one: `MainActivity` (the launcher activity above). No
services, receivers, or providers.

## What could break this in the future
- Motorola could theoretically fork/rename `com.android.phone` on a
  future build (uncommon for Motorola's near-stock software, but not
  impossible on any OEM skin).
- Google has moved this Activity's package once before (it lived in
  `com.android.settings` prior to roughly the Lollipop/Marshmallow
  era, then moved to `com.android.phone`). A similar future move
  would break the explicit component name.
- Google could add an `android:permission` requirement to `RadioInfo`
  in a future AOSP security pass — it hasn't as of the current AOSP
  `master` branch, but it's a component that has had security
  attention before (it can toggle radio/network settings from its own
  UI, which is why some *neighboring* activities in the same file do
  require `MODIFY_PHONE_STATE`).
- If any of that happens, the app will show a toast and do nothing
  destructive — see the `catch (ActivityNotFoundException)` block in
  `MainActivity.kt`. The fallback is always: dial `*#*#4636#*#*`
  manually.

## Build & install (no ADB required for the final app)
1. Open this folder in Android Studio (`File → Open`).
2. Let Gradle sync. If Android Studio asks to update the Android
   Gradle Plugin/Kotlin plugin versions in `build.gradle`, accept —
   the versions here are current as of writing but Android Studio's
   bundled tooling moves fast.
3. **Build → Build Bundle(s)/APK(s) → Build APK(s).**
4. Click "locate" in the notification, or find the file at
   `app/build/outputs/apk/debug/app-debug.apk`.
5. Copy that file to the Moto G34 any way you like (email it to
   yourself, upload to Drive, plain USB file copy — no ADB).
6. On the phone, tap the APK file to install it. Android will ask you
   to allow installs from whatever app you used to open the file —
   that's the normal one-time sideloading prompt, not specific to
   this app. Play Protect may show a generic "unrecognized app"
   notice for any unsigned/sideloaded app; that's expected.
7. Tap the "Phone Info" icon.

If you'd rather run it straight from Android Studio over USB, that
works too (Run ▶ with the phone connected and USB debugging on) — but
that's a development-time convenience, not a requirement of the app
itself, which needs no ADB and no debugging access to function.
