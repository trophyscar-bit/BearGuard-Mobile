package com.bearguard.mobile.alliancechest

import com.bearguard.mobile.service.BearGuardAccessibilityService
import kotlinx.coroutines.delay

/**
 * matt/2026-08-15: port of Bearguard-Win's AllianceChestRoutine.java. The live game's Chests
 * panel is simpler than the Java version assumed (that version had a per-row individual-gift
 * claim loop plus a conditional "Claim All" and a separate config-gated Honor Chest tap): both
 * tabs here (Loot Chest, Alliance Gift) always expose their own "Claim All" button, so this is
 * two tab-switches + two taps, no loop needed.
 *
 * Live-verified 2026-08-15: Alliance (bottom nav) -> Chests tile -> Alliance Gift tab (15
 * unclaimed) -> Claim All -> Rewards popup (EXP potions, speedups) -> switch to Loot Chest tab
 * (14 unclaimed) -> Claim All -> Rewards popup (resources, speedups) -> confirmed via Alliance's
 * red-dot badge count dropping (34 -> 4) and both tab badges clearing.
 *
 * matt/2026-08-16: two real gaps caught live once the badge count was small (4 items, arriving
 * one at a time from allies over hours) instead of a big batch:
 *  1. With only a few items, the panel shows individual green "Claim" buttons per row instead of
 *     a "Claim All" button -- Claim All isn't there at all, so the old code's blind tap silently
 *     did nothing and reported "n/a". Fixed with a per-row fallback loop, exactly the shape the
 *     Java version's gatherIndividualGifts() already anticipated.
 *  2. A completely separate cumulative "Alliance Points" chest sits in the panel header (progress
 *     bar + a red badge when a milestone is ready) and was never touched at all. Fixed: tap it,
 *     dismiss the Rewards popup if one appears, same OCR-gated-dismiss pattern as everything else.
 */
object AllianceChestRoutine {

    private const val ALLIANCE_NAV_X = 534f
    private const val ALLIANCE_NAV_Y = 1227f

    private const val CHESTS_TILE_X = 527f
    private const val CHESTS_TILE_Y = 668f

    private const val LOOT_CHEST_TAB_X = 190f
    private const val ALLIANCE_GIFT_TAB_X = 527f
    private const val TAB_Y = 400f

    // Claim All sits at a different X per tab -- Alliance Gift's button is pushed right by the
    // "Send Anonymous Alliance Gift" checkbox next to it; Loot Chest's is centered.
    private const val LOOT_CLAIM_ALL_X = 360f
    private const val LOOT_CLAIM_ALL_Y = 1210f
    private const val GIFT_CLAIM_ALL_X = 600f
    private const val GIFT_CLAIM_ALL_Y = 1190f

    private const val REWARD_DISMISS_X = 360f
    private const val REWARD_DISMISS_Y = 640f

    // Cumulative Alliance Points chest, shared header above both tabs.
    private const val POINTS_CHEST_X = 360f
    private const val POINTS_CHEST_Y = 200f

    // The 4 visible gift-row "Claim" buttons -- rows don't reflow when claimed (they just gray
    // out in place), so these fixed Y positions stay valid across a claim pass.
    private data class RowButton(val x: Float, val y: Float, val left: Int, val top: Int, val right: Int, val bottom: Int)
    private val GIFT_ROW_BUTTONS = listOf(
        RowButton(605f, 640f, 555, 615, 655, 660),
        RowButton(605f, 792f, 555, 767, 655, 812),
        RowButton(605f, 944f, 555, 919, 655, 964),
        RowButton(605f, 1096f, 555, 1071, 655, 1116),
    )
    private const val MAX_ROW_CLAIM_ROUNDS = 3

    data class Result(
        val giftClaimed: Boolean,
        val lootClaimed: Boolean,
        val pointsChestClaimed: Boolean = false,
        val failure: String? = null,
    )

    suspend fun run(): Result {
        val service = BearGuardAccessibilityService.instance
            ?: return Result(false, false, failure = "Accessibility service not connected")

        service.bringGameToForeground()

        service.tap(ALLIANCE_NAV_X, ALLIANCE_NAV_Y)
        delay(1800)
        service.tap(CHESTS_TILE_X, CHESTS_TILE_Y)
        delay(1500)

        val pointsChestClaimed = claimIfRewardAppears(service, POINTS_CHEST_X, POINTS_CHEST_Y)

        // Alliance Gift tab first (game opens directly onto whichever tab was last viewed, so
        // explicitly select it rather than assume).
        service.tap(ALLIANCE_GIFT_TAB_X, TAB_Y)
        delay(500)
        var giftClaimed = claimIfRewardAppears(service, GIFT_CLAIM_ALL_X, GIFT_CLAIM_ALL_Y)
        if (claimIndividualGiftRows(service)) giftClaimed = true

        service.tap(LOOT_CHEST_TAB_X, TAB_Y)
        delay(500)
        val lootClaimed = claimIfRewardAppears(service, LOOT_CLAIM_ALL_X, LOOT_CLAIM_ALL_Y)

        // matt/2026-08-15: real bug caught live -- two blind taps at a hardcoded "X (close)"
        // position landed the SECOND tap on the World HUD's "+" (buy gems) button instead of a
        // second panel-close X, after the first tap had already collapsed both the Chests and
        // Alliance panels in one step. Landed on a real-money purchase screen (no purchase made,
        // caught and backed out immediately). GLOBAL_ACTION_BACK avoids overshooting onto a
        // fixed coordinate, but two of them back-to-back on an already-World screen could still
        // do something unintended (e.g. trigger a "Quit game?" dialog) -- so only send the
        // second one if we can confirm we're not on World yet (checks for the World-only "Events"
        // HUD icon via OCR).
        service.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK)
        delay(600)

        val postBackBitmap = service.captureScreenshotSuspend()
        val onWorldAlready = postBackBitmap != null &&
            service.readTextSuspend(postBackBitmap, 620, 155, 715, 190)?.lowercase()?.contains("event") == true
        if (!onWorldAlready) {
            service.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK)
            delay(600)
        }

        return Result(giftClaimed, lootClaimed, pointsChestClaimed)
    }

    /**
     * Taps a claim-shaped button, then only dismisses the Rewards popup if one actually appeared
     * (OCR-gated dismiss -- a blind dismiss tap with nothing to claim would land on whatever's
     * underneath instead, confirmed the hard way earlier this session).
     */
    private suspend fun claimIfRewardAppears(
        service: BearGuardAccessibilityService,
        claimX: Float, claimY: Float,
    ): Boolean {
        service.tap(claimX, claimY)
        delay(1200)

        val bitmap = service.captureScreenshotSuspend() ?: return false
        val bannerText = service.readTextSuspend(bitmap, 230, 285, 490, 355)
        val claimed = bannerText != null && "reward" in bannerText.lowercase()
        if (claimed) {
            service.tap(REWARD_DISMISS_X, REWARD_DISMISS_Y)
            delay(500)
        }
        return claimed
    }

    /**
     * matt/2026-08-16: fallback for when Claim All isn't present -- reads each row's button text
     * and taps only the ones that still say "Claim" (not "Claimed"). No Rewards popup appears for
     * an individual claim (confirmed live -- it's a silent single-item grant), so no dismiss step
     * here. Loops a few rounds since new gifts can arrive from allies between rounds.
     */
    private suspend fun claimIndividualGiftRows(service: BearGuardAccessibilityService): Boolean {
        var claimedAny = false
        repeat(MAX_ROW_CLAIM_ROUNDS) {
            val bitmap = service.captureScreenshotSuspend() ?: return claimedAny
            var tappedThisRound = false
            for (row in GIFT_ROW_BUTTONS) {
                val text = service.readTextSuspend(bitmap, row.left, row.top, row.right, row.bottom)
                    ?.lowercase().orEmpty()
                if ("claim" in text && "claimed" !in text) {
                    service.tap(row.x, row.y)
                    delay(500)
                    tappedThisRound = true
                    claimedAny = true
                }
            }
            if (!tappedThisRound) return claimedAny
            delay(500)
        }
        return claimedAny
    }
}
