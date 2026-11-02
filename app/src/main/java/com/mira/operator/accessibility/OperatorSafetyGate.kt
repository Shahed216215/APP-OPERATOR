
package com.mira.operator.accessibility

import android.content.Context

/**
 * Central safety gate for Mira Universal App Operator.
 *
 * Fail-closed policy:
 * A fresh installation starts in STOPPED state.
 */
object OperatorSafetyGate {

    private const val PREFS_NAME = "operator"
    private const val KEY_EMERGENCY_STOP = "emergency_stop"

    /**
     * Returns true when the operator must remain stopped.
     * Missing preference defaults to true (fail-closed).
     */
    fun isStopped(context: Context): Boolean {
        return preferences(context).getBoolean(
            KEY_EMERGENCY_STOP,
            true
        )
    }

    /**
     * Immediately stores the stopped state.
     * Returns false if the preference could not be committed.
     */
    fun stop(context: Context): Boolean {
        return preferences(context).edit()
            .putBoolean(KEY_EMERGENCY_STOP, true)
            .commit()
    }

    /**
     * Resumes the operator after an explicit user action.
     * Returns false if the preference could not be committed.
     */
    fun resume(context: Context): Boolean {
        return preferences(context).edit()
            .putBoolean(KEY_EMERGENCY_STOP, false)
            .commit()
    }

    private fun preferences(context: Context) =
        context.applicationContext.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )
}
