package com.bearguard.mobile.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Path
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.Display
import com.bearguard.mobile.scheduler.SchedulerPrefs
import com.bearguard.mobile.scheduler.TaskRegistry
import java.util.concurrent.Executors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * matt/2026-08-15: the whole automation engine lives here. This mirrors the two primitives
 * everything in the Windows Bearguard app is built on (tapPoint / screencap via ADB) -- here
 * they're native Android APIs instead:
 *
 *  - captureScreenshot(): AccessibilityService.takeScreenshot() (API 30+). No MediaProjection
 *    "start recording?" dialog, no repeated permission prompts -- just works once the service is
 *    enabled in Settings > Accessibility.
 *  - tap(x, y): dispatchGesture() with a single-point Path, same idea as an ADB `input tap`.
 *
 * Scoped to Whiteout Survival only via packageNames in accessibility_service_config.xml -- this
 * service literally cannot see or touch any other app's screen.
 *
 * Nothing here does any WS-specific automation yet. Once the tablet's real resolution is
 * calibrated, the *Routine-equivalent logic gets built on top of these two primitives, same
 * shape as every Routine class in the Windows app.
 */
class BearGuardAccessibilityService : AccessibilityService() {

    private val executor = Executors.newSingleThreadExecutor()
    private val serviceScope = CoroutineScope(SupervisorJob())
    private var engineJob: Job? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        _connected.value = true
        Log.i(TAG, "BearGuard Mobile accessibility service connected -- ready to capture/tap on ${TARGET_PACKAGE}.")

        // matt/2026-08-15: one-shot self-test on connect -- proves the actual capture API works
        // against the live game screen (not just that the service registered). Temporary; comes
        // out once real routine logic exists to exercise this properly.
        captureScreenshot { bitmap ->
            if (bitmap != null) {
                Log.i(TAG, "SELF-TEST capture OK: ${bitmap.width}x${bitmap.height}")
                // Second self-test, chained: OCR the Power HUD crop -- the exact same
                // (130,48)-(272,96) region bg_telemetry.java already proved out on the Windows
                // side against this identical 720x1280 MuMu resolution. Real proof the OCR
                // primitive works, not a synthetic test image.
                OcrHelper.readText(bitmap, 130, 48, 272, 96) { text ->
                    Log.i(TAG, "SELF-TEST OCR (Power HUD crop) raw text: '$text'")
                }
            } else {
                Log.w(TAG, "SELF-TEST capture FAILED")
            }
        }

        // matt/2026-08-15: resume automatically if the engine was left running before the
        // service got torn down (app update, process death) -- mirrors Bearguard-Win's
        // --resume-queue relaunch behavior instead of silently going quiet until the user
        // remembers to hit Start again.
        serviceScope.launch {
            if (SchedulerPrefs(this@BearGuardAccessibilityService).engineRunning.first()) {
                Log.i(TAG, "Engine was running before restart -- resuming.")
                startEngine()
            }
        }
    }

    /**
     * matt/2026-08-15: "I want this to mirror the Windows version... a toggle button to turn
     * whatever it is on and off." This is the whole engine loop -- Bearguard-Win's
     * ScheduleService equivalent. Every ENGINE_TICK_MS, walks every registered task; a task
     * whose toggle is on and whose next-run time has arrived gets run, one at a time (never
     * concurrently -- they all share the one game screen). No-op while paused.
     */
    fun startEngine() {
        if (engineJob?.isActive == true) return
        val prefs = SchedulerPrefs(this)
        engineJob = serviceScope.launch {
            prefs.setEngineRunning(true)
            Log.i(TAG, "Engine started.")
            while (true) {
                for (task in TaskRegistry.all) {
                    if (!prefs.enabled(task.key).first()) continue
                    val nextRunAt = prefs.nextRunAt(task.key).first()
                    val now = System.currentTimeMillis()
                    if (now < nextRunAt) continue

                    Log.i(TAG, "Engine running task: ${task.displayName}")
                    val outcome = try {
                        task.runOnce()
                    } catch (e: Exception) {
                        Log.w(TAG, "Task ${task.displayName} threw: ${e.message}")
                        com.bearguard.mobile.scheduler.TaskOutcome(
                            "Error: ${e.message}", ENGINE_ERROR_RETRY_MS, isError = true
                        )
                    }
                    Log.i(TAG, "Engine task ${task.displayName} result: ${outcome.summary}")
                    prefs.setLastResult(task.key, outcome.summary)
                    prefs.setNextRunAt(task.key, System.currentTimeMillis() + outcome.nextRunDelayMs)
                }
                delay(ENGINE_TICK_MS)
            }
        }
    }

    fun stopEngine() {
        engineJob?.cancel()
        engineJob = null
        serviceScope.launch { SchedulerPrefs(this@BearGuardAccessibilityService).setEngineRunning(false) }
        Log.i(TAG, "Engine stopped.")
    }

    fun isEngineRunning(): Boolean = engineJob?.isActive == true

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Intentionally empty for now -- no WS-specific logic yet. Routines will hook in here
        // once real calibration exists.
    }

    override fun onInterrupt() {
        Log.w(TAG, "Accessibility service interrupted.")
    }

    /** Captures the current screen. Callback receives the bitmap, or null on failure. */
    fun captureScreenshot(onResult: (Bitmap?) -> Unit) {
        takeScreenshot(
            Display.DEFAULT_DISPLAY,
            executor,
            object : TakeScreenshotCallback {
                override fun onSuccess(result: ScreenshotResult) {
                    val bitmap = Bitmap.wrapHardwareBuffer(result.hardwareBuffer, result.colorSpace)
                    result.hardwareBuffer.close()
                    onResult(bitmap)
                }

                override fun onFailure(errorCode: Int) {
                    Log.w(TAG, "Screenshot capture failed, errorCode=$errorCode")
                    onResult(null)
                }
            }
        )
    }

    /** Single tap at (x, y) in screen pixel coordinates -- the Android analogue of `adb shell input tap`. */
    fun tap(x: Float, y: Float, durationMs: Long = 60L) {
        val path = Path().apply { moveTo(x, y) }
        val stroke = GestureDescription.StrokeDescription(path, 0, durationMs)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()
        dispatchGesture(gesture, null, null)
    }

    /** Suspend wrapper over [captureScreenshot] -- for routine code written as a straight-line
     * coroutine instead of nested callbacks, same readability the Windows Routine classes get for
     * free from being synchronous. */
    suspend fun captureScreenshotSuspend(): Bitmap? = suspendCancellableCoroutine { cont ->
        captureScreenshot { cont.resume(it) }
    }

    /** Suspend wrapper over [OcrHelper.readText]. */
    suspend fun readTextSuspend(source: Bitmap, left: Int, top: Int, right: Int, bottom: Int): String? =
        suspendCancellableCoroutine { cont ->
            OcrHelper.readText(source, left, top, right, bottom) { cont.resume(it) }
        }

    /**
     * matt/2026-08-15: real bug caught during Chief Order's first live test -- unlike the
     * Windows app (where the game IS the whole desktop), BearGuard Mobile's own UI is what's in
     * the foreground when the user taps "Run", so every dispatched gesture was landing on
     * BearGuard Mobile's own screen instead of the game. Every routine needs WS actually in
     * front before it starts tapping game coordinates.
     *
     * Launches Whiteout Survival's real activity (com.gof.global/com.unity3d.player.
     * MyMainPlayerActivity, confirmed live via `pm resolve-activity`) and gives it a moment to
     * come to the foreground.
     */
    suspend fun bringGameToForeground(waitMs: Long = 3000L) {
        val intent = packageManager.getLaunchIntentForPackage(TARGET_PACKAGE)
            ?: Intent().setClassName(TARGET_PACKAGE, "com.unity3d.player.MyMainPlayerActivity")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        startActivity(intent)
        delay(waitMs)
    }

    override fun onDestroy() {
        super.onDestroy()
        engineJob?.cancel()
        if (instance === this) instance = null
        _connected.value = false
    }

    companion object {
        private const val TAG = "BearGuardMobile"
        private const val TARGET_PACKAGE = "com.gof.global"
        private const val ENGINE_TICK_MS = 30_000L
        private const val ENGINE_ERROR_RETRY_MS = 5 * 60_000L

        /** matt/2026-08-15: singleton handle so routine/UI code can reach the live service
         * instance without a bind -- same shape as every other Routine class reaching for the
         * one shared automation session on the Windows side. Null until the user has enabled the
         * service in Settings > Accessibility. */
        var instance: BearGuardAccessibilityService? = null
            private set

        // matt/2026-08-16: real bug caught live -- every screen was reading `instance != null`
        // as a plain one-time val, so Compose never re-checked it once the Activity's first
        // composition happened to land before the service finished connecting (a real race --
        // service connection is async and routinely lags a beat behind Activity startup). Every
        // "toggle stays greyed out" / "stale status" confusion this session traced back to this
        // one thing. A StateFlow makes it observable: collectAsState() actually recomposes when
        // the service connects or disconnects, instead of freezing whatever was true at first
        // paint.
        private val _connected = MutableStateFlow(false)
        val connected: StateFlow<Boolean> = _connected
    }
}
