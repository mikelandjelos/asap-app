# Accepted Android PoC baseline

Status: Accepted and frozen under completed T-004.

Implementation status: T-005/S1 realized the SDK, wrapper, AGP, AppCompat, ConstraintLayout, and initial test portions of this baseline. T-005/S2 realized Google Code Scanner 16.1.0, `barcode_ui` metadata, auto-zoom, the restricted EAN/UPC format set, and scanner outcome handling. Physical-device validation remains pending T-005/S3.

Last researched: 2026-09-04 from official Android, Google ML Kit, AndroidX, Gradle, and upstream library documentation.

## Frozen minimal baseline

| Concern | Accepted version | Reason |
| --- | --- | --- |
| Android Gradle Plugin | `9.3.2` | Stable patched 9.3 line, supported by Android Studio Quail 4, supports API 37, and avoids adopting AGP 9.4 immediately after release. |
| Gradle Wrapper | `9.5.0` | Required/default Gradle version for AGP 9.3. |
| Gradle runtime JDK | OpenJDK `21` | Already installed, LTS, and supported by Gradle 9.5; AGP requires at least JDK 17. Do not add a global Gradle installation. |
| Java source/target | `17` | Conservative Android language-bytecode baseline matching AGP's documented default JDK level while Gradle itself runs on JDK 21. |
| `compileSdk` / `targetSdk` | `36` / `36` | Stable Android 16 API used by the verified phone; avoids preview Android 17/API 37 behavior. |
| `minSdk` | `23` | Exact Google Code Scanner minimum and the minimum of the optional current Material Views release. |
| SDK Build Tools | `36.0.0` | AGP 9.3 default; Android Platform 36 and Build Tools 36.0.0 would need installation during a later approved setup subtask. |
| Scanner | `com.google.android.gms:play-services-code-scanner:16.1.0` | Official permissionless Google Code Scanner artifact; version 16.1.0 includes auto-zoom. |
| XML compatibility UI | `androidx.appcompat:appcompat:1.8.0` | Current stable AppCompat release for Java/XML Views. |
| XML layout | `androidx.constraintlayout:constraintlayout:2.2.2` | Current stable flexible XML layout library. |
| Local tests | `junit:junit:4.13.2` | Latest stable JUnit 4 line and the standard Java local-test baseline in Android guidance. |
| Instrumented tests | `androidx.test.ext:junit:1.3.0` and `androidx.test.espresso:espresso-core:3.7.0` | Current stable AndroidX JUnit extension and Espresso core. |

Repositories should be limited initially to `google()` and `mavenCentral()`, with exact versions rather than dynamic selectors.

## Scanner integration constraints

- Google Code Scanner delegates camera UI and processing to Google Play services, so the app does not request camera permission.
- The scanner module is unbundled. Add the `com.google.mlkit.vision.DEPENDENCIES=barcode_ui` application metadata for Play Store install-time delivery; retain a first-use download/error state because sideloaded debug builds may still need module download.
- Restrict accepted barcode formats in the PoC when the target product formats are known; this may improve scanning speed.
- Direct ML Kit Barcode Scanning (`com.google.android.gms:play-services-mlkit-barcode-scanning:18.3.1`) and CameraX are explicitly excluded from the initial dependency set. They remain an upgrade path for a custom preview, overlays, or continuous scanning.

## Deferred alternatives and trade-offs

### Latest toolchain: AGP 9.4.0 and Gradle 9.6.0

This pair is supported by the installed Quail 4 IDE and is the newest stable toolchain. It is not the S1 recommendation because AGP 9.4 was released only days before this research and provides no needed capability for the first PoC. It can replace 9.3.2/9.5.0 if using the newest versions is preferred over the slightly more established patch line.

### Android 17/API 37

Platform and Build Tools 37 are already installed, and AGP 9.3/9.4 support API 37. Android 17 is still presented as a preview opt-in in the official setup documentation, while the physical test phone runs stable Android 16/API 36. The recommendation is therefore to install Platform 36 and Build Tools 36.0.0 later and defer API 37 targeting.

### Material Components for Views 1.14.0

`com.google.android.material:material:1.14.0` is the current stable Views library and now requires `minSdk 23`, but Google placed it in maintenance mode after moving new Material development to Compose. It is optional: add it only when the first custom ASAP screen needs a Material widget or theme that AppCompat and XML layouts do not provide. This preserves the accepted Java/XML direction without adding an unnecessary initial dependency.

## Accepted S2 decisions

T-004/S2 freezes the following choices from the accepted S1 recommendation:

1. AGP 9.3.2 + Gradle 9.5.0 versus AGP 9.4.0 + Gradle 9.6.0.
2. Stable `compileSdk`/`targetSdk 36`, `minSdk 23`, and Java source/target 17 with Gradle on JDK 21.
3. The three required production dependencies: Code Scanner 16.1.0, AppCompat 1.8.0, and ConstraintLayout 2.2.2.
4. The minimal JUnit/Espresso test baseline.
5. Deferring Material Views, direct ML Kit Barcode Scanning, and CameraX.

These decisions were made before implementation. Their current realization is tracked in `docs/PROJECT_STATUS.md`; scanner runtime validation remains outside this baseline document.

## Official sources

- [Google Code Scanner for Android](https://developers.google.com/ml-kit/vision/barcode-scanning/code-scanner)
- [Android Gradle Plugin and Gradle compatibility](https://developer.android.com/build/releases/about-agp)
- [AGP 9.3 release and compatibility notes](https://developer.android.com/build/releases/agp-9-3-0-release-notes)
- [AGP 9.4 release and compatibility notes](https://developer.android.com/build/releases/agp-9-4-0-release-notes)
- [Android 16 SDK setup](https://developer.android.com/about/versions/16/setup-sdk)
- [Android 17 SDK setup](https://developer.android.com/about/versions/17/setup-sdk)
- [Gradle Java compatibility matrix](https://docs.gradle.org/current/userguide/compatibility.html)
- [AndroidX stable versions](https://developer.android.com/jetpack/androidx/versions)
- [AppCompat releases](https://developer.android.com/jetpack/androidx/releases/appcompat)
- [ConstraintLayout releases](https://developer.android.com/jetpack/androidx/releases/constraintlayout)
- [AndroidX Test releases](https://developer.android.com/jetpack/androidx/releases/test)
- [JUnit 4 releases](https://github.com/junit-team/junit4/releases)
- [Material Components for Android releases](https://github.com/material-components/material-components-android/releases)
