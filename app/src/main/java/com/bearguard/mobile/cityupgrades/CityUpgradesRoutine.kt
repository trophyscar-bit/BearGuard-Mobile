package com.bearguard.mobile.cityupgrades

import com.bearguard.mobile.service.BearGuardAccessibilityService
import kotlinx.coroutines.delay

/**
 * matt/2026-08-15: port of Bearguard-Win's UpgradeBuildingsRoutine.java -- scoped down to a real,
 * verifiable v1. The Java version is genuinely the largest/most stateful routine in the codebase:
 * it reads two construction-queue slots via multi-pass OCR, picks the game's own recommended
 * building, branches on Survivor Building / City Building / New Building / production-blocked,
 * replenishes missing resources, and coordinates a cross-task "production consumer" reservation
 * so this routine and a Training/Research routine don't fight over the same shared queue slot.
 *
 * BearGuard Mobile has no Training/Research routine yet, so that whole reservation system has
 * nothing to coordinate with -- deliberately not ported. What IS ported and real: opening the
 * left-menu City tab and reading both queue slots' live status (Idle / Busy+time-remaining /
 * Not Purchased / Unknown), same as the Java version's core `inspectAllQueues()`.
 *
 * matt/2026-08-15, live-verified: left-menu edge swipe -> City tab (already the default tab) ->
 * both queue rows read correctly via OCR (Furnace Upgrading "5d 18:11:20", Research Center
 * Upgrading "1d 13:25:41" -- both BUSY, matching what was actually on screen).
 *
 * NOT yet verified: the actual "start the next upgrade" flow (tap an idle queue -> find the
 * Upgrade button -> replenish resources -> confirm -> alliance help) -- matt's account had zero
 * idle queues in the session this was built, so there was nothing to open live. That flow is not
 * implemented here yet; this v1 only reads and reports state. Flagged honestly rather than
 * shipping unverified tap coordinates for a screen that was never actually seen.
 */
object CityUpgradesRoutine {

    private const val LEFT_MENU_EDGE_X = 6f
    private const val LEFT_MENU_EDGE_Y = 550f

    private const val CITY_TAB_X = 115f
    private const val CITY_TAB_Y = 270f

    // matt/2026-08-15: real bug caught live -- the Windows version's own crop heights (21px and
    // 24px) are both under ML Kit's hard InputImage minimum of 32x32, so every OCR call on these
    // crops failed outright ("InputImage width and height should be at least 32!"), silently
    // returning null every time (logged as a warning, not surfaced to the caller) -- that's why
    // both queues always read UNKNOWN regardless of the panel's real state. Tesseract (the
    // Windows OCR engine) has no such minimum, so this never showed up over there. Padded a few
    // px top/bottom to clear the floor.
    private const val QUEUE1_L = 95
    private const val QUEUE1_T = 371
    private const val QUEUE1_R = 358
    private const val QUEUE1_B = 404

    private const val QUEUE2_L = 95
    private const val QUEUE2_T = 444
    private const val QUEUE2_R = 358
    private const val QUEUE2_B = 480

    // matt/2026-08-15: real bug caught live -- GLOBAL_ACTION_BACK does NOT close this slide-out
    // panel (Unity doesn't treat it as a dismissible modal); instead it fell through to the
    // World screen's hardware-back handler and opened a "Quit game?" confirmation dialog.
    // Cancelled immediately, no harm done. The panel has its own visible collapse arrow on its
    // right edge -- tapping that is the real dismiss action. Second bug caught on the same
    // retest: one tap only collapses the full panel down to a persistent narrow sliver (still
    // showing a bit of the Wilderness tab), not fully hidden -- the arrow stays in the same
    // screen position in that sliver state, so a second tap at the same coordinate fully hides
    // it back to the clean default World view.
    private const val COLLAPSE_ARROW_X = 455f
    private const val COLLAPSE_ARROW_Y = 548f

    enum class QueueStatus { IDLE, BUSY, NOT_PURCHASED, UNKNOWN }
    data class QueueState(val status: QueueStatus, val rawText: String?)
    data class Result(val queue1: QueueState?, val queue2: QueueState?, val failure: String? = null)

    suspend fun run(): Result {
        val service = BearGuardAccessibilityService.instance
            ?: return Result(null, null, "Accessibility service not connected")

        service.bringGameToForeground()

        service.tap(LEFT_MENU_EDGE_X, LEFT_MENU_EDGE_Y)
        delay(1200)
        service.tap(CITY_TAB_X, CITY_TAB_Y)
        delay(600)

        val bitmap = service.captureScreenshotSuspend()
            ?: return Result(null, null, "Screen capture failed after opening City queue panel")

        val q1 = inspectQueue(service, bitmap, QUEUE1_L, QUEUE1_T, QUEUE1_R, QUEUE1_B)
        val q2 = inspectQueue(service, bitmap, QUEUE2_L, QUEUE2_T, QUEUE2_R, QUEUE2_B)

        service.tap(COLLAPSE_ARROW_X, COLLAPSE_ARROW_Y)
        delay(500)
        service.tap(COLLAPSE_ARROW_X, COLLAPSE_ARROW_Y)
        delay(500)

        return Result(q1, q2)
    }

    private suspend fun inspectQueue(
        service: BearGuardAccessibilityService,
        bitmap: android.graphics.Bitmap,
        left: Int, top: Int, right: Int, bottom: Int,
    ): QueueState {
        val text = service.readTextSuspend(bitmap, left, top, right, bottom)?.trim()
        val lowered = text?.lowercase().orEmpty()

        return when {
            text.isNullOrBlank() -> QueueState(QueueStatus.UNKNOWN, text)
            "idle" in lowered -> QueueState(QueueStatus.IDLE, text)
            "purchase" in lowered || "queue" in lowered -> QueueState(QueueStatus.NOT_PURCHASED, text)
            Regex("""\d""").containsMatchIn(text) -> QueueState(QueueStatus.BUSY, text)
            else -> QueueState(QueueStatus.UNKNOWN, text)
        }
    }
}
