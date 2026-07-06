# Coffee Pulse Timer Performance + Haptics Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make the Android brew timer phase, countdown, ring, notification, and haptics derive from one pause-safe monotonic snapshot model while replacing continuous timer animation with bounded motion.

**Architecture:** `TimerEngine` owns deterministic clock-based snapshots and one-shot boundary cues. `BrewViewModel` and `BrewTimerService` consume the same snapshot API, while haptic waveforms are pure pattern definitions wrapped by Android vibrator API fallbacks.

**Tech Stack:** Kotlin, Jetpack Compose Material 3, Coroutines/StateFlow, Android `VibratorManager`/`Vibrator`, `VibrationEffect`, JUnit.

---

### Task 1: Timer Snapshot Model

**Files:**
- Modify: `android/app/src/main/java/com/damacus/coffeepulse/domain/TimerEngine.kt`
- Modify: `android/app/src/main/java/com/damacus/coffeepulse/domain/model/TimerSession.kt`
- Test: `android/app/src/test/java/com/damacus/coffeepulse/domain/TimerEngineTest.kt`

- [x] Add failing tests for exact phase boundaries, ceil display seconds, pause/resume accounting, and one-shot boundary cues.
- [x] Add pause-safe session fields and `TimerEngine.snapshot(session, nowMillis)`.
- [x] Keep `TimerEngine.tick()` as a compatibility wrapper over the snapshot model.
- [x] Verify with `env JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew :app:testDebugUnitTest --no-daemon --rerun-tasks --stacktrace`.

### Task 2: App and Service Integration

**Files:**
- Modify: `android/app/src/main/java/com/damacus/coffeepulse/ui/BrewViewModel.kt`
- Modify: `android/app/src/main/java/com/damacus/coffeepulse/service/BrewTimerService.kt`
- Modify: `android/app/src/main/java/com/damacus/coffeepulse/data/ConfigRepository.kt`

- [x] Update ViewModel and foreground service tick loops to call `snapshot()` with `System.currentTimeMillis()`.
- [x] Persist pause/cue/progress snapshot fields so restored sessions do not replay cues.
- [x] Keep transition haptics in `BrewTimerService` to avoid ViewModel/service double-fire.

### Task 3: Lean TimerRing Motion

**Files:**
- Modify: `android/app/src/main/java/com/damacus/coffeepulse/ui/components/TimerRing.kt`

- [x] Remove `rememberInfiniteTransition` from normal running state.
- [x] Keep phase color, tick bounce, final-count emphasis, glow, and spark as bounded state-driven animations.
- [x] Keep bounce as `graphicsLayer` scale so layout dimensions do not change.

### Task 4: Semantic Haptics

**Files:**
- Create: `android/app/src/main/java/com/damacus/coffeepulse/sensory/BrewHapticPattern.kt`
- Modify: `android/app/src/main/java/com/damacus/coffeepulse/sensory/BrewHaptics.kt`
- Test: `android/app/src/test/java/com/damacus/coffeepulse/sensory/BrewHapticPatternTest.kt`

- [x] Add pure haptic cues for `PourStart`, `StopPourRelax`, `Bloom`, and `Finish`.
- [x] Use `VibratorManager.defaultVibrator` on API 31+ and legacy `Vibrator` on API 29-30.
- [x] Fall back to timing-only waveforms when amplitude control is unavailable.
- [x] Add cancel support for reset/stop paths.

### Task 5: Verification

**Files:**
- No source changes expected.

- [x] Run unit tests from `android/`.
- [x] Run debug assemble from `android/`.
- [ ] Manual device check: phase boundary sync, no extra pause/resume haptics, reset cancels vibration, and before/after `gfxinfo framestats`.
