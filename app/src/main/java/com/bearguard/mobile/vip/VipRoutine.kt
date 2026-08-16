package com.bearguard.mobile.vip

import com.bearguard.mobile.service.BearGuardAccessibilityService
import kotlinx.coroutines.delay

/**
 * matt/2026-08-15: port of Bearguard-Win's VipRoutine.java -- FREE daily claims only. The Java
 * version also has a monthly-VIP real-money purchase flow (buyMonthlyVip, off by default); that
 * path is deliberately NOT ported here at all -- BearGuard Mobile never initiates a purchase.
 *
 * Live-verified 2026-08-15: VIP badge tap opens the VIP panel directly (no intermediate menu
 * template needed on this account). "VIP 7 Daily Free Bundle" claimed (badge -> countdown timer),
 * then the VIP-points chest icon (top-right, red-dot) claimed separately: "VIP points obtained:
 * 500" reward screen, confirmed via the on-screen text and the daily sign-in streak counter (36).
 */
object VipRoutine {

    private const val VIP_BADGE_X = 480f
    private const val VIP_BADGE_Y = 68f

    private const val DAILY_BUNDLE_CLAIM_X = 580f
    private const val DAILY_BUNDLE_CLAIM_Y = 825f

    private const val VIP_POINTS_CHEST_X = 625f
    private const val VIP_POINTS_CHEST_Y = 283f

    private const val REWARD_REVEAL_TAP_X = 360f
    private const val REWARD_REVEAL_TAP_Y = 640f

    data class Result(
        val dailyBundleClaimed: Boolean,
        val pointsClaimed: Boolean,
        val failure: String? = null,
    )

    suspend fun run(): Result {
        val service = BearGuardAccessibilityService.instance
            ?: return Result(false, false, "Accessibility service not connected")

        service.bringGameToForeground()

        service.tap(VIP_BADGE_X, VIP_BADGE_Y)
        delay(1800)

        var bitmap = service.captureScreenshotSuspend()
            ?: return Result(false, false, "Screen capture failed after opening VIP panel")

        // Daily Free Bundle: the badge reads "Claim" when ready, a countdown once claimed --
        // OCR the label region rather than assume it's always there.
        var bundleLabel = service.readTextSuspend(bitmap, 500, 800, 665, 850)
        var dailyBundleClaimed = false
        if (bundleLabel != null && "claim" in bundleLabel.lowercase()) {
            service.tap(DAILY_BUNDLE_CLAIM_X, DAILY_BUNDLE_CLAIM_Y)
            delay(900)
            service.tap(REWARD_REVEAL_TAP_X, REWARD_REVEAL_TAP_Y)
            delay(900)
            dailyBundleClaimed = true
        }

        // VIP points chest: a red notification dot sits on the icon when unclaimed. Re-capture --
        // the previous claim's reveal popup changed what's on screen.
        delay(500)
        bitmap = service.captureScreenshotSuspend()
            ?: return Result(dailyBundleClaimed, false, "Screen capture failed before points chest check")

        val hasRedDot = redDotPresent(bitmap, 615, 270, 645, 300)
        var pointsClaimed = false
        if (hasRedDot) {
            service.tap(VIP_POINTS_CHEST_X, VIP_POINTS_CHEST_Y)
            delay(900)
            service.tap(REWARD_REVEAL_TAP_X, REWARD_REVEAL_TAP_Y)
            delay(900)
            pointsClaimed = true
        }

        service.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK)
        delay(500)

        return Result(dailyBundleClaimed, pointsClaimed)
    }

    /**
     * Same technique as Bearguard-Win's CustomArmamentChestRoutine -- a solid, non-animating red
     * notification dot is far more reliable than OCR/template matching against a busy, sometimes-
     * animated background. Pure pixel-color scan, no ML Kit involved.
     */
    private fun redDotPresent(bitmap: android.graphics.Bitmap, left: Int, top: Int, right: Int, bottom: Int): Boolean {
        var count = 0
        val maxX = minOf(right, bitmap.width)
        val maxY = minOf(bottom, bitmap.height)
        for (y in top until maxY) {
            for (x in left until maxX) {
                val pixel = bitmap.getPixel(x, y)
                val r = (pixel shr 16) and 0xFF
                val g = (pixel shr 8) and 0xFF
                val b = pixel and 0xFF
                if (r > 180 && g < 80 && b < 80) count++
            }
        }
        return count >= 15
    }
}
