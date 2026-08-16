package com.bearguard.mobile.deals

import com.bearguard.mobile.service.BearGuardAccessibilityService
import kotlinx.coroutines.delay

/**
 * matt/2026-08-15: port of Bearguard-Win's DailyDealsFreeChestRoutine.java. The cart icon
 * (top-right HUD, next to VIP badge) opens the Shop panel directly onto the Daily Deals tab --
 * no tab-switch needed, unlike the Java version's SHOP_TAB_DAILY_DEALS search (that template
 * step is simply not required on this account/version; the free chest badge is visible the
 * instant the panel opens). ONLY ever taps the free "Free" chest badge -- everything else on
 * this tab is a paid pack, never touched.
 *
 * Live-verified 2026-08-15: cart tap -> Daily Deals tab (already showing) -> Free badge tap ->
 * "Claimed" reveal (100 gems, confirmed via balance going 73,851 -> 73,951) -> tap-anywhere to
 * exit -> back to World.
 */
object DailyDealsRoutine {

    private const val CART_ICON_X = 660f
    private const val CART_ICON_Y = 68f

    private const val FREE_CHEST_X = 85f
    private const val FREE_CHEST_Y = 420f

    private const val REWARD_REVEAL_TAP_X = 360f
    private const val REWARD_REVEAL_TAP_Y = 640f

    sealed class Result {
        /** Free chest badge was there and got claimed. */
        data object Claimed : Result()
        /** Panel opened fine, but the free badge wasn't there -- already claimed today. */
        data object AlreadyClaimed : Result()
        data class Failed(val reason: String) : Result()
    }

    suspend fun run(): Result {
        val service = BearGuardAccessibilityService.instance
            ?: return Result.Failed("Accessibility service not connected")

        service.bringGameToForeground()

        service.tap(CART_ICON_X, CART_ICON_Y)
        delay(1800)

        val bitmap = service.captureScreenshotSuspend()
            ?: return Result.Failed("Screen capture failed after opening Shop")

        // The free chest's "Free" label sits directly under the badge at roughly
        // (55,440)-(120,465) -- reading it tells us whether the badge is still there without
        // guessing from a blank tap.
        val label = service.readTextSuspend(bitmap, 40, 430, 140, 470)
        if (label == null || "free" !in label.lowercase()) {
            service.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK)
            delay(500)
            return Result.AlreadyClaimed
        }

        service.tap(FREE_CHEST_X, FREE_CHEST_Y)
        delay(900)
        service.tap(REWARD_REVEAL_TAP_X, REWARD_REVEAL_TAP_Y)
        delay(900)

        service.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK)
        delay(500)

        return Result.Claimed
    }
}
