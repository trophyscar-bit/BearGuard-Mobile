package com.bearguard.mobile.scheduler

import com.bearguard.mobile.alliancechest.AllianceChestRoutine
import com.bearguard.mobile.chieforder.ChiefOrderRoutine
import com.bearguard.mobile.cityupgrades.CityUpgradesRoutine
import com.bearguard.mobile.deals.DailyDealsRoutine
import com.bearguard.mobile.mail.MailRewardsRoutine
import com.bearguard.mobile.vip.VipRoutine
import java.util.concurrent.TimeUnit

/**
 * matt/2026-08-15: one adapter per ported routine, translating each screen-specific Result type
 * into a RoutineTask the scheduler can drive generically. Reschedule intervals mirror each
 * routine's Windows-side cadence where that's known (Deals/VIP daily, Mail hourly); where the
 * Java version's own interval wasn't directly portable (City Upgrades' timer-aware reschedule
 * needs a parsed Duration this v1 doesn't have yet), a reasonable fixed poll is used instead and
 * flagged honestly, not presented as a faithful match.
 *
 * Get Giftcodes is deliberately NOT in this registry -- it needs a player ID entered and a human
 * glancing at which codes are worth redeeming, not a blind unattended loop. Stays a manual screen.
 */
object TaskRegistry {

    private val HOUR = TimeUnit.HOURS.toMillis(1)
    private val DAY = TimeUnit.DAYS.toMillis(1)
    private val MIN = TimeUnit.MINUTES.toMillis(1)

    val all: List<RoutineTask> = listOf(
        object : RoutineTask {
            override val key = "city_upgrades"
            override val displayName = "City Upgrades"
            override suspend fun runOnce(): TaskOutcome {
                val r = CityUpgradesRoutine.run()
                if (r.failure != null) return TaskOutcome(r.failure, 5 * MIN, isError = true)
                val summary = "Queue 1: ${r.queue1?.status ?: "?"} · Queue 2: ${r.queue2?.status ?: "?"}"
                // matt/2026-08-15: not timer-aware yet (no Duration parser for "5d 17:59:20" built
                // in this v1) -- fixed 30-minute poll instead of computing the real wake time.
                return TaskOutcome(summary, 30 * MIN)
            }
        },
        object : RoutineTask {
            override val key = "deals"
            override val displayName = "Deals"
            override suspend fun runOnce(): TaskOutcome {
                return when (val r = DailyDealsRoutine.run()) {
                    is DailyDealsRoutine.Result.Claimed -> TaskOutcome("Claimed", DAY)
                    is DailyDealsRoutine.Result.AlreadyClaimed -> TaskOutcome("Already claimed today", DAY)
                    is DailyDealsRoutine.Result.Failed -> TaskOutcome(r.reason, 10 * MIN, isError = true)
                }
            }
        },
        object : RoutineTask {
            override val key = "vip"
            override val displayName = "VIP"
            override suspend fun runOnce(): TaskOutcome {
                val r = VipRoutine.run()
                if (r.failure != null) return TaskOutcome(r.failure, 10 * MIN, isError = true)
                val summary = "Bundle: ${if (r.dailyBundleClaimed) "claimed" else "n/a"} · " +
                    "Points: ${if (r.pointsClaimed) "claimed" else "n/a"}"
                return TaskOutcome(summary, DAY)
            }
        },
        object : RoutineTask {
            override val key = "mail_rewards"
            override val displayName = "Mail Rewards"
            override suspend fun runOnce(): TaskOutcome {
                val r = MailRewardsRoutine.run()
                if (r.failure != null) return TaskOutcome(r.failure, 10 * MIN, isError = true)
                return TaskOutcome("Processed ${r.tabsProcessed} tab(s)", HOUR)
            }
        },
        object : RoutineTask {
            override val key = "alliance_chests"
            override val displayName = "Alliance"
            override suspend fun runOnce(): TaskOutcome {
                val r = AllianceChestRoutine.run()
                if (r.failure != null) return TaskOutcome(r.failure, 10 * MIN, isError = true)
                val summary = "Gift: ${if (r.giftClaimed) "claimed" else "n/a"} · " +
                    "Loot: ${if (r.lootClaimed) "claimed" else "n/a"}"
                return TaskOutcome(summary, HOUR)
            }
        },
        object : RoutineTask {
            override val key = "chief_order"
            override val displayName = "Chief Order"
            override suspend fun runOnce(): TaskOutcome {
                // matt/2026-08-15: one task key covers all three order types -- each is checked
                // in turn, matching the spirit of Windows running one DelayedTask per type on its
                // own cooldown, without building three separate schedule slots for a v1.
                val results = ChiefOrderRoutine.ChiefOrderType.entries.map { type ->
                    type to ChiefOrderRoutine.run(type)
                }
                val summary = results.joinToString(" · ") { (type, result) ->
                    val label = when (result) {
                        is ChiefOrderRoutine.Result.Enacted -> "enacted"
                        is ChiefOrderRoutine.Result.Scheduled -> result.cover
                        is ChiefOrderRoutine.Result.Failed -> result.reason
                    }
                    "${type.description}: $label"
                }
                val anyFailed = results.any { it.second is ChiefOrderRoutine.Result.Failed }
                return TaskOutcome(summary, 30 * MIN, isError = anyFailed)
            }
        },
    )
}
