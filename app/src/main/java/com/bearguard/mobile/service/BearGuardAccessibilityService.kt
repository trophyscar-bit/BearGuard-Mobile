package com.bearguard.mobile.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Path
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.Display
import java.util.concurrent.Executors
import kotlinx.coroutines.delay
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

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
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
    }

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
        if (instance === this) instance = null
    }

    companion object {
        private const val TAG = "BearGuardMobile"
        private const val TARGET_PACKAGE = "com.gof.global"

        /** matt/2026-08-15: singleton handle so routine/UI code can reach the live service
         * instance without a bind -- same shape as every other Routine class reaching for the
         * one shared automation session on the Windows side. Null until the user has enabled the
         * service in Settings > Accessibility. */
        var instance: BearGuardAccessibilityService? = null
            private set
    }
}
