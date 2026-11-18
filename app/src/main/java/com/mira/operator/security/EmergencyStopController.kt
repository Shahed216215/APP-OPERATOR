package com.mira.operator.security

import android.content.Context

/**
 * Central fail-closed safety state for future Operator execution components.
 *
 * Default state is STOPPED, including after process restart.
 * Reset requires an explicit confirmation from the calling UI.
 *
 * Every future execution adapter must check isExecutionAllowed()
 * immediately before executing an action.
 */
object EmergencyStopController {

    private const val PREFS_NAME = "operator_safety"
    private const val KEY_EMERGENCY_STOP = "emergency_stop"

    @Volatile
    private var forcedStop = true

    /**
     * Returns true when execution must be blocked.
     * Any storage/read failure is treated as STOPPED.
     */
    fun isEmergencyStopEnabled(context: Context): Boolean {
        if (forcedStop) return true

        return try {
            context.applicationContext
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_EMERGENCY_STOP, true)
        } catch (_: Exception) {
            forcedStop = true
            true
        }
    }

    /**
     * Execution is allowed only when the stop state is reliably disabled.
     */
    fun isExecutionAllowed(context: Context): Boolean {
        return !isEmergencyStopEnabled(context)
    }

    /**
     * Activates STOPPED immediately in memory, then persists it.
     * A failed persistence operation never clears the in-memory stop.
     */
    @Synchronized
    fun activate(context: Context): Boolean {
        forcedStop = true

        return try {
            context.applicationContext
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_EMERGENCY_STOP, true)
                .commit()
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Reset is rejected unless the UI supplies explicit user confirmation.
     * A successful reset only enables the safety gate; it does not start
     * an automation engine.
     */
    @Synchronized
    fun resetAfterUserConfirmation(
        context: Context,
        userConfirmed: Boolean
    ): Boolean {
        if (!userConfirmed) return false

        return try {
            val saved = context.applicationContext
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_EMERGENCY_STOP, false)
                .commit()

            if (saved) {
                forcedStop = false
            }

            saved
        } catch (_: Exception) {
            forcedStop = true
            false
        }
    }
}
