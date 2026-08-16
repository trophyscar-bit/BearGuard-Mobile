package com.bearguard.mobile.mail

import com.bearguard.mobile.service.BearGuardAccessibilityService
import kotlinx.coroutines.delay

/**
 * matt/2026-08-15: port of Bearguard-Win's MailRewardsRoutine.java. The live game has a much
 * simpler flow than the Java version assumed (that version scrolled through individual mail rows
 * hunting a per-row claim badge, tab by tab, with an overflow-scroll loop): the current mail panel
 * has a single "Read & Claim All" button per tab that sweeps every reward in that tab in one tap,
 * no scrolling needed. Also has more tabs now -- Wars and Starred were added alongside the
 * original Alliance/System/Reports.
 *
 * Live-verified 2026-08-15: mail icon (envelope, world HUD) -> System tab (4 unclaimed: Chief's
 * Approval Rating, Arena of Glory, Hall of Chiefs, Brothers in Arms) -> Read & Claim All ->
 * Rewards popup (1,100 gems + key + speedups + resources) -> confirmed via red-dot badges
 * clearing. Alliance tab claimed separately (200 gems + 1,000 coins). Confirmed tapping Read &
 * Claim All with nothing new to claim is a safe no-op -- no popup, nothing mis-tapped.
 *
 * matt/2026-08-15: real bug caught live -- Reports and Starred tabs do NOT have a Read & Claim
 * All button (they're plain lists: battle reports / flagged mail, nothing to claim). Their bottom
 * bar is Delete/Star/Share instead. The blind tap at the claim-button position landed on a mail
 * row there, opening a battle report, and the follow-up dismiss-tap landed on that report's Share
 * button -- one step from actually opening a "forward this to a player" dialog. Restricted to the
 * three tabs confirmed to have the real claim button: Wars, Alliance, System.
 *
 * matt/2026-08-15: second real bug caught live, on the corrected 3-tab version -- the blind
 * "dismiss the Rewards popup" tap assumed a popup always appears after tapping Claim All. On a
 * tab with nothing new (silent no-op, no popup), that tap landed straight on whatever mail row
 * sits at that Y position instead, opening a battle report. Fixed by only dismissing when a
 * popup is actually confirmed on screen -- OCR the "Rewards" banner text first.
 */
object MailRewardsRoutine {

    private const val MAIL_ICON_X = 664f
    private const val MAIL_ICON_Y = 1049f

    private val TAB_Y = 120f
    private val TABS = listOf(
        "Wars" to 87f,
        "Alliance" to 222f,
        "System" to 360f,
    )

    private const val CLAIM_ALL_X = 553f
    private const val CLAIM_ALL_Y = 1240f

    private const val REWARD_REVEAL_TAP_X = 360f
    private const val REWARD_REVEAL_TAP_Y = 640f

    data class Result(val tabsProcessed: Int, val failure: String? = null)

    suspend fun run(): Result {
        val service = BearGuardAccessibilityService.instance
            ?: return Result(0, "Accessibility service not connected")

        service.bringGameToForeground()

        service.tap(MAIL_ICON_X, MAIL_ICON_Y)
        delay(1500)

        var processed = 0
        for ((_, x) in TABS) {
            service.tap(x, TAB_Y)
            delay(400)
            service.tap(CLAIM_ALL_X, CLAIM_ALL_Y)
            delay(700)

            // Only dismiss if a Rewards popup actually appeared -- when there's nothing new,
            // Claim All is a silent no-op with no popup, and a blind dismiss-tap here would land
            // straight on a mail row underneath instead (confirmed live).
            val bitmap = service.captureScreenshotSuspend()
            if (bitmap != null) {
                val bannerText = service.readTextSuspend(bitmap, 230, 285, 490, 355)
                if (bannerText != null && "reward" in bannerText.lowercase()) {
                    service.tap(REWARD_REVEAL_TAP_X, REWARD_REVEAL_TAP_Y)
                    delay(500)
                }
            }
            processed++
        }

        service.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK)
        delay(500)

        return Result(processed)
    }
}
