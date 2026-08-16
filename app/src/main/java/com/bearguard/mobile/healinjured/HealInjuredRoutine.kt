package com.bearguard.mobile.healinjured

import com.bearguard.mobile.service.BearGuardAccessibilityService
import kotlinx.coroutines.delay

/**
 * matt/2026-08-15: port of Bearguard-Win's HealInjuredRoutine.java. Coordinates carried over
 * directly from the Windows version's own doc comment (live-calibrated at the same 720x1280
 * resolution this session has used throughout): World-map heal icon over My City, panel title/
 * Severely-Injured-count/Quick-Select/primary-action/close positions, all unchanged.
 *
 * Windows version's own doc is explicit that only the icon -> Quick Select -> Heal -> Help path
 * was live-verified there; the idle/no-injuries path was written from screenshots, not
 * separately confirmed. This port's own live test (2026-08-15, matt's account currently has zero
 * injured troops) exercises exactly that idle path: tapping where the heal icon would be, with
 * no icon actually present, and confirming nothing opens and nothing gets mis-tapped.
 */
object HealInjuredRoutine {

    private const val HEAL_ICON_X = 560f
    private const val HEAL_ICON_Y = 1035f

    private const val PANEL_TITLE_L = 230
    private const val PANEL_TITLE_T = 240
    private const val PANEL_TITLE_R = 460
    private const val PANEL_TITLE_B = 280

    private const val INJURED_L = 170
    private const val INJURED_T = 305
    private const val INJURED_R = 340
    private const val INJURED_B = 355

    private const val QUICK_SELECT_X = 134f
    private const val QUICK_SELECT_Y = 850f

    private const val PRIMARY_ACTION_X = 517f
    private const val PRIMARY_ACTION_Y = 850f

    private const val PANEL_CLOSE_X = 598f
    private const val PANEL_CLOSE_Y = 257f

    sealed class Result {
        data object NothingInjured : Result()
        data class HealingStarted(val injuredCount: Int) : Result()
        data class Failed(val reason: String) : Result()
    }

    suspend fun run(): Result {
        val service = BearGuardAccessibilityService.instance
            ?: return Result.Failed("Accessibility service not connected")

        service.bringGameToForeground()

        service.tap(HEAL_ICON_X, HEAL_ICON_Y)
        delay(1500)

        var bitmap = service.captureScreenshotSuspend()
            ?: return Result.Failed("Screen capture failed after tapping heal icon")

        val title = service.readTextSuspend(
            bitmap, PANEL_TITLE_L, PANEL_TITLE_T, PANEL_TITLE_R, PANEL_TITLE_B
        )
        if (title == null || "heal" !in title.lowercase()) {
            // Icon wasn't there / panel didn't open -- nothing currently injured. Nothing else
            // was tapped, so there's nothing to back out of.
            return Result.NothingInjured
        }

        val injuredText = service.readTextSuspend(
            bitmap, INJURED_L, INJURED_T, INJURED_R, INJURED_B
        )
        val injuredCount = injuredText
            ?.substringBefore("/")
            ?.replace(",", "")
            ?.trim()
            ?.toIntOrNull()

        if (injuredCount == null || injuredCount == 0) {
            service.tap(PANEL_CLOSE_X, PANEL_CLOSE_Y)
            delay(700)
            return Result.NothingInjured
        }

        service.tap(QUICK_SELECT_X, QUICK_SELECT_Y)
        delay(1000)
        // Shared button position: first tap starts healing ("Heal"), second tap (now showing
        // "Help") requests alliance assistance on the timer.
        service.tap(PRIMARY_ACTION_X, PRIMARY_ACTION_Y)
        delay(1000)
        service.tap(PRIMARY_ACTION_X, PRIMARY_ACTION_Y)
        delay(1000)

        service.tap(PANEL_CLOSE_X, PANEL_CLOSE_Y)
        delay(700)

        return Result.HealingStarted(injuredCount)
    }
}
