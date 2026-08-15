package com.bearguard.mobile.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Bitmap
import android.graphics.Path
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.Display
import java.util.concurrent.Executors

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
        Log.i(TAG, "BearGuard Mobile accessibility service connected -- ready to capture/tap on ${TARGET_PACKAGE}.")

        // matt/2026-08-15: one-shot self-test on connect -- proves the actual capture API works
        // against the live game screen (not just that the service registered). Temporary; comes
        // out once real routine logic exists to exercise this properly.
        captureScreenshot { bitmap ->
            if (bitmap != null) {
                Log.i(TAG, "SELF-TEST capture OK: ${bitmap.width}x${bitmap.height}")
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

    companion object {
        private const val TAG = "BearGuardMobile"
        private const val TARGET_PACKAGE = "com.gof.global"
    }
}
