package com.mira.operator.security

import android.content.Context
import com.mira.operator.apps.AppRegistry

/**
 * User consent is separate from the developer-maintained app allowlist.
 * Unknown packages and missing consent are always denied.
 */
object AppConsentManager {

    private const val PREFS_NAME = "operator_app_consent"
    private const val KEY_PREFIX = "consent:"

    fun isAuthorized(context: Context, packageName: String): Boolean {
        if (!AppRegistry.isAllowed(packageName)) return false

        return try {
            context.applicationContext
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_PREFIX + packageName, false)
        } catch (_: Exception) {
            false
        }
    }

    @Synchronized
    fun grant(context: Context, packageName: String): Boolean {
        if (!AppRegistry.isAllowed(packageName)) return false

        return try {
            context.applicationContext
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_PREFIX + packageName, true)
                .commit()
        } catch (_: Exception) {
            false
        }
    }

    @Synchronized
    fun revoke(context: Context, packageName: String): Boolean {
        if (!AppRegistry.isAllowed(packageName)) return false

        return try {
            context.applicationContext
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_PREFIX + packageName, false)
                .commit()
        } catch (_: Exception) {
            false
        }
    }

    @Synchronized
    fun revokeAll(context: Context): Boolean {
        return try {
            val editor = context.applicationContext
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()

            AppRegistry.getAllowedApps().forEach { app ->
                editor.putBoolean(KEY_PREFIX + app.packageName, false)
            }

            editor.commit()
        } catch (_: Exception) {
            false
        }
    }
}
