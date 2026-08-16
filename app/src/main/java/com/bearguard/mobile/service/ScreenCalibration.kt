package com.bearguard.mobile.service

/**
 * matt/2026-08-16: "when the tablet arrives, get its real wm size/density and
 * recalibrate." The tablet turned out to be a Nexall N90 (1200x2000 @240dpi, Android
 * 16) -- NOT the Galaxy Tab A16 originally expected, and NOT the same resolution as
 * the MuMu emulator every routine's coordinates were calibrated against (720x1280
 * @320dpi). Rather than hand-recalibrate every tap/OCR-crop coordinate in every
 * Routine file for this specific device (and then again for whatever comes next),
 * this is a transparent scaling layer: every Routine keeps writing coordinates in the
 * original 720x1280 REFERENCE space (the MuMu-calibrated numbers already in the
 * codebase), and BearGuardAccessibilityService.tap()/readTextSuspend() scale them to
 * the ACTUAL connected device's resolution right before use.
 *
 * This assumes the game's own UI scales proportionally to fill the screen (true for
 * essentially all mobile game UIs, including Whiteout Survival) -- a linear per-axis
 * scale is not pixel-perfect when the aspect ratio differs (MuMu's 720:1280 = 0.5625
 * vs the N90's 1200:2000 = 0.6, a ~6.7% difference), so taps near the far edges of the
 * screen carry more error than taps near center. Good enough to unblock every routine
 * without a full recalibration pass; a routine whose taps start missing near a screen
 * edge on this device is the signal to give that one specific coordinate a real
 * override rather than trusting the linear scale further.
 *
 * actualWidth/actualHeight self-update from every real screenshot's bitmap dimensions
 * (see captureScreenshot in BearGuardAccessibilityService) -- no per-device constant
 * to maintain, this works automatically for any future device or emulator too.
 */
object ScreenCalibration {
    const val REFERENCE_WIDTH = 720f
    const val REFERENCE_HEIGHT = 1280f

    @Volatile
    var actualWidth: Int = REFERENCE_WIDTH.toInt()
        private set

    @Volatile
    var actualHeight: Int = REFERENCE_HEIGHT.toInt()
        private set

    val scaleX: Float get() = actualWidth / REFERENCE_WIDTH
    val scaleY: Float get() = actualHeight / REFERENCE_HEIGHT

    /** Called from every successful screenshot capture -- keeps calibration accurate
     * even across an orientation change or a switch to a different connected device,
     * with no explicit setup step required. */
    fun updateFromScreenshot(width: Int, height: Int) {
        if (width <= 0 || height <= 0) return
        if (width != actualWidth || height != actualHeight) {
            android.util.Log.i("BearGuardMobile",
                "ScreenCalibration: actual resolution ${width}x$height " +
                    "(reference ${REFERENCE_WIDTH.toInt()}x${REFERENCE_HEIGHT.toInt()}, " +
                    "scale ${width / REFERENCE_WIDTH}x${height / REFERENCE_HEIGHT}).")
        }
        actualWidth = width
        actualHeight = height
    }

    /** Reference-space (720x1280) x,y -> actual device pixel x,y. */
    fun scalePoint(x: Float, y: Float): Pair<Float, Float> = (x * scaleX) to (y * scaleY)

    /** Reference-space (720x1280) crop box -> actual device pixel crop box, for OCR. */
    fun scaleBox(left: Int, top: Int, right: Int, bottom: Int): IntArray = intArrayOf(
        (left * scaleX).toInt(), (top * scaleY).toInt(),
        (right * scaleX).toInt(), (bottom * scaleY).toInt()
    )
}
