package com.bearguard.mobile.scheduler

/**
 * matt/2026-08-15: "I want this to mirror the Windows version... a toggle button to turn
 * whatever it is on and off, not just a run button." This is the Android equivalent of
 * Bearguard-Win's DelayedTask: a task has a key, runs once, and reports how long to wait before
 * running again -- the engine loop (BearGuardScheduler) drives it automatically once its toggle
 * is on, exactly like ScheduleService running every enabled TpDailyTaskEnum on the Windows side.
 * No manual "Run" button on the module screens anymore -- flip the switch, the engine does it.
 */
interface RoutineTask {
    val key: String
    val displayName: String

    /** Runs one pass and reports what happened + how long until the next pass. */
    suspend fun runOnce(): TaskOutcome
}

data class TaskOutcome(
    val summary: String,
    val nextRunDelayMs: Long,
    val isError: Boolean = false,
)
