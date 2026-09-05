# Working workflow

## Session loop

1. **Orient:** read the files listed in `AGENTS.md` and inspect the working tree.
2. **Select:** identify the first pending task in `TODO.md`, unless the user explicitly chooses another task.
3. **Plan:** divide the task into reasonable subtasks; define deliverables, acceptance evidence, verification, affected documentation, dependencies, and exclusions in `docs/PLANS.md`.
4. **Review:** iterate on the plan with the user. Do not implement anything while the plan is merely proposed.
5. **Approve:** record explicit task-plan approval. Obtain separate explicit approval for the first subtask before executing it.
6. **Execute one subtask:** keep changes within the approved scope.
7. **Verify and synchronize:** perform the subtask's checks and update every affected documentation surface as part of that same atomic subtask.
8. **Stop:** report evidence and wait for explicit approval before starting the next subtask.
9. **Close:** after the final subtask, ask the user to accept the task before considering any subsequent TODO item.

## Approval-state vocabulary

- **Proposed:** open for discussion; no implementation is authorized.
- **Plan approved:** the task decomposition is accepted, but subtasks still require individual execution approval.
- **Subtask approved:** exactly the named subtask may be executed.
- **Awaiting acceptance:** implementation and verification are complete; user confirmation is required before progressing.
- **Accepted:** the user accepted the subtask or task result.

Do not infer approval from silence, general encouragement, or approval of a different task/subtask.

## No-documentation-lag invariant

- No implementation subtask is complete while its documentation is stale.
- Documentation synchronization happens per subtask, not only at task closure.
- Each plan must list affected documentation before implementation is approved.
- If work is interrupted between implementation and documentation, the next action is reconciliation; new implementation remains blocked.
- Use repository evidence to correct stale claims immediately. Never preserve a known inaccuracy merely because it was previously documented.

## Definition of done

A task may be checked off only when:

- the requested artifact or behavior exists in the repository;
- relevant verification passes;
- every affected documentation surface exactly reflects what was built, including limitations and unimplemented behavior;
- new architectural or technology decisions are recorded;
- the next session can understand remaining work without relying on chat history.

## LaTeX builds

Run these commands from the repository root:

```sh
mkdir -p build

pdflatex -interaction=nonstopmode -halt-on-error \
  -output-directory=build report/report.tex
pdflatex -interaction=nonstopmode -halt-on-error \
  -output-directory=build report/report.tex

lualatex -interaction=nonstopmode -halt-on-error \
  -output-directory=build presentation/asap-presentation.tex
lualatex -interaction=nonstopmode -halt-on-error \
  -output-directory=build presentation/asap-presentation.tex
```

The report also supports LuaLaTeX. Two passes are used so tables of contents, references, and Beamer frame totals settle correctly.

## Android workstation

The verified user-scoped installation uses:

- Android Studio: `/home/mih/.local/opt/android-studio`
- Android SDK: `/home/mih/Android/Sdk`
- command symlinks: `/home/mih/.local/bin`

No global Android environment variable is required. Use the explicit SDK root for reproducible package queries, and disable Android CLI metrics in scripted checks:

```sh
java -version
javac -version
studio --version
android --no-metrics --version
android --no-metrics --sdk=/home/mih/Android/Sdk sdk list
adb version
```

`sdkmanager` remains available for compatibility but reports itself deprecated; prefer the `android sdk` commands. Do not install standalone Gradle globally: the Android project uses its committed Gradle Wrapper.

### Physical Android runtime

The verified runtime is a Samsung SM-A566B physical device running Android 16/API 36. On first connection, unlock the phone, enable Developer options and USB debugging, select File transfer / Android Auto rather than USB tethering, and accept the host RSA prompt. Verify the connection and required MVP capabilities without persisting the device serial number:

```sh
adb devices -l
adb get-state
adb shell getprop ro.build.version.release
adb shell getprop ro.build.version.sdk
adb shell getprop ro.product.model
adb shell pm path com.google.android.gms
adb shell dumpsys package com.google.android.gms | rg -m 1 'versionName='
adb shell pm list features | rg 'camera|autofocus|flash'
```

The verified device has enabled Google Play services and declares rear/front camera, autofocus, and flash features, making it a suitable target for the implemented Google Code Scanner experiment. ADB visibility verifies deployment readiness and declared hardware capabilities; actual scanning behavior remains unverified until separately approved T-005/S3. No emulator/AVD is currently configured.

### Accepted PoC build baseline

The Android project uses the exact baseline in `docs/ANDROID_BASELINE.md`: AGP 9.3.2, Gradle Wrapper 9.5.0, Gradle on OpenJDK 21, Java source/target 17, `compileSdk 36`, `targetSdk 36`, `minSdk 23`, and Build Tools 36.0.0. Configure only `google()` and `mavenCentral()` repositories and never use dynamic dependency versions.

Required production coordinates are Code Scanner 16.1.0, AppCompat 1.8.0, and ConstraintLayout 2.2.2. The initial tests use JUnit 4.13.2, AndroidX JUnit 1.3.0, and Espresso 3.7.0. Material Views, direct ML Kit Barcode Scanning, and CameraX remain deferred.

Platform 36, Build Tools 36.0.0, the Gradle Wrapper, all accepted UI/test dependencies, and Google Code Scanner 16.1.0 are installed or resolved.

### Android shell build

Run the repository-contained wrapper from the Android project directory. `ANDROID_HOME` is scoped to the command because no global Android environment variable is required:

```sh
cd android
ANDROID_HOME=/home/mih/Android/Sdk ./gradlew clean testDebugUnitTest lintDebug assembleDebug --warning-mode all
```

The debug APK is generated at `android/app/build/outputs/apk/debug/app-debug.apk`; all generated build output and machine-local configuration are ignored. The committed Gradle distribution checksum is `553c78f50dafcd54d65b9a444649057857469edf836431389695608536d6b746`, and the wrapper JAR checksum is `497c8c2a7e5031f6aa847f88104aa80a93532ec32ee17bdb8d1d2f67a194a9c7`.

For S2, seven unit tests, lint, and debug assembly pass; lint reports zero findings. APK inspection confirms application ID `rs.ac.ni.elfak.asap`, min SDK 23, target SDK 36, `barcode_ui` module metadata, and no application camera permission. The scanner dependency contributes internet and network-state permissions for its unbundled Google Play services flow. Do not install it on a device until T-005/S3 is approved.

## Documentation synchronization guide

| Change | Also update |
| --- | --- |
| Task completed or scope discovered | `TODO.md` |
| Task/subtask planned, approved, completed, or accepted | `docs/PLANS.md` |
| Capability or project phase changed | `docs/PROJECT_STATUS.md` |
| Technology, architecture, API, model, or process chosen | `docs/DECISIONS.md` |
| Architecture or data flow changed | `docs/ARCHITECTURE.md`, PlantUML source, report, presentation |
| User feedback or evaluation performed | report sections 4–5, presentation, project status |
| Any meaningful session ends | `docs/SESSION_HANDOFF.md` |
