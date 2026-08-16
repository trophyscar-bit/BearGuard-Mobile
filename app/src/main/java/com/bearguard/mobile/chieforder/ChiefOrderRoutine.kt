package com.bearguard.mobile.chieforder

import com.bearguard.mobile.service.BearGuardAccessibilityService
import kotlinx.coroutines.delay

/**
 * matt/2026-08-15: port of Bearguard-Win's ChiefOrderRoutine.java. The two template-matching
 * steps (open the menu, find the Enact button) are replaced with hardcoded/live-calibrated tap
 * points instead of building OpenCV-style template matching for Android -- same substitution
 * pattern used elsewhere this session (Life Essence taps, Labyrinth ratios). Everything else
 * (shelf-slot OCR, cover-text state reading) is a faithful line-for-line port, since it never
 * depended on template matching in the first place -- reused as-is.
 *
 * All coordinates below were live-verified against MuMu's 720x1280 capture on 2026-08-15:
 *  - menu button (scales-of-justice icon, right-side World HUD stack): (664, 956)
 *  - SHELF_SLOTS: copied verbatim from the Java source (same 720x1280 calibration)
 *  - shelf tap for "Urgent Mobilization" -> detail page -> Enact button (360, 923): confirmed
 *    live, order actually enacted (star currency dropped 7.3M -> 7.2M, cover flipped to Active).
 */
object ChiefOrderRoutine {

    enum class ChiefOrderType(val description: String, val cooldownHours: Int) {
        RUSH_JOB("Rush Job", 24),
        URGENT_MOBILIZATION("Urgent Mobilization", 8),
        PRODUCTIVITY_DAY("Productivity Day", 12),
    }

    /** Menu-access button on the base World/City screen -- the scales-of-justice icon in the
     * right-side HUD icon stack. */
    private const val MENU_BUTTON_X = 664f
    private const val MENU_BUTTON_Y = 956f

    /** One position on the 2x3 Chief Order shelf: cover crop (for state OCR), label crop (for
     * identifying which order this is), and the tap point to open it. */
    private data class OrderSlot(
        val coverTL: Pair<Int, Int>, val coverBR: Pair<Int, Int>,
        val labelTL: Pair<Int, Int>, val labelBR: Pair<Int, Int>,
        val tapX: Float, val tapY: Float,
    )

    private val SHELF_SLOTS = listOf(
        OrderSlot(120 to 290, 320 to 370, 110 to 452, 335 to 494, 220f, 330f),
        OrderSlot(395 to 290, 600 to 370, 385 to 452, 610 to 494, 497f, 330f),
        OrderSlot(120 to 610, 320 to 690, 110 to 775, 335 to 817, 220f, 650f),
        OrderSlot(395 to 610, 600 to 690, 385 to 775, 610 to 817, 497f, 650f),
        OrderSlot(120 to 930, 320 to 1010, 105 to 1098, 335 to 1140, 220f, 970f),
        OrderSlot(395 to 930, 600 to 1010, 385 to 1098, 610 to 1140, 497f, 970f),
    )

    /** Enact button on an order's detail page -- same position regardless of which order was
     * opened (single-column detail layout). Live-verified 2026-08-15. */
    private const val ENACT_BUTTON_X = 360f
    private const val ENACT_BUTTON_Y = 923f

    sealed class Result {
        /** Order was open, Enact was pressed, done. */
        data object Enacted : Result()
        /** Cover showed a live countdown (Active or On cooldown); caller should re-check later. */
        data class Scheduled(val cover: String) : Result()
        /** Something didn't line up -- service unavailable, menu didn't open, label not found, etc. */
        data class Failed(val reason: String) : Result()
    }

    /** Runs one pass: open the Chief Order menu, locate [type] on the shelf, and enact it if
     * available. Caller is responsible for getting back to the World/City screen first --
     * mirrors getRequiredStartLocation() == HOME on the Windows side. */
    suspend fun run(type: ChiefOrderType): Result {
        val service = BearGuardAccessibilityService.instance
            ?: return Result.Failed("Accessibility service not connected")

        // Bring WS to the foreground first -- BearGuard Mobile's own screen is what's showing
        // right now (the user just pressed Run inside it), and taps go to whatever's on top.
        service.bringGameToForeground()

        service.tap(MENU_BUTTON_X, MENU_BUTTON_Y)
        delay(3000)

        val slot = locateSlotByLabel(service, type) ?: run {
            // Menu may not have opened, or this order type isn't on the shelf this account has.
            return Result.Failed("${type.description} label not found on the shelf")
        }

        // Small gap before the next takeScreenshot() call -- back-to-back calls trip Android's
        // capture rate limit (see locateSlotByLabel's note).
        delay(500)
        val cover = readCoverText(service, slot.coverTL, slot.coverBR)
        val lowered = cover?.lowercase().orEmpty()

        if ("active" in lowered) {
            return Result.Scheduled(cover ?: "Active")
        }
        if ("cooldown" in lowered) {
            return Result.Scheduled(cover ?: "On cooldown")
        }

        // Clean cover -- available. Open it.
        service.tap(slot.tapX, slot.tapY)
        delay(1500)

        service.tap(ENACT_BUTTON_X, ENACT_BUTTON_Y)
        delay(1000)

        // Back out of the detail page.
        service.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK)
        delay(1500)

        return Result.Enacted
    }

    /**
     * matt/2026-08-15: real bug caught during first live test -- takeScreenshot() is rate
     * limited (errorCode=3, ERROR_TAKE_SCREENSHOT_INTERVAL_TIME_SHORT) when called back-to-back,
     * so capturing once per slot (6 calls in a tight loop) meant only the first ever succeeded.
     * One capture, six crops -- same as a person just looking at one photo of the whole shelf.
     */
    private suspend fun locateSlotByLabel(
        service: BearGuardAccessibilityService,
        type: ChiefOrderType,
    ): OrderSlot? {
        val bitmap = service.captureScreenshotSuspend() ?: return null
        val needle = type.description.split(" ").first().lowercase()
        for (slot in SHELF_SLOTS) {
            val label = service.readTextSuspend(
                bitmap, slot.labelTL.first, slot.labelTL.second, slot.labelBR.first, slot.labelBR.second
            )
            if (label != null && needle in label.lowercase()) {
                return slot
            }
        }
        return null
    }

    private suspend fun readCoverText(
        service: BearGuardAccessibilityService,
        tl: Pair<Int, Int>, br: Pair<Int, Int>,
    ): String? {
        val bitmap = service.captureScreenshotSuspend() ?: return null
        return service.readTextSuspend(bitmap, tl.first, tl.second, br.first, br.second)
    }
}
