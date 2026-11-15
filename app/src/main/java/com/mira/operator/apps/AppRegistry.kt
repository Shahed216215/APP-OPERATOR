package com.mira.operator.apps

import android.content.Context
import android.content.Intent

data class AllowedApp(
    val packageName: String,
    val displayName: String
)

object AppRegistry {
    private const val PREFS = "operator_apps"
    private const val KEY_ALLOWED = "allowed_packages"
    private const val KEY_INITIALIZED = "allowlist_initialized"

    private val defaults = setOf(
        "com.openai.chatgpt",
        "com.google.android.googlequicksearchbox",
        "com.android.chrome"
    )

    @Volatile
    private var appContext: Context? = null

    fun initialize(context: Context) {
        val safeContext = context.applicationContext
        appContext = safeContext
        val prefs = safeContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (!prefs.getBoolean(KEY_INITIALIZED, false)) {
            prefs.edit()
                .putStringSet(KEY_ALLOWED, defaults)
                .putBoolean(KEY_INITIALIZED, true)
                .apply()
        }
    }

    fun isAllowed(packageName: String): Boolean {
        val context = appContext ?: return false
        val allowed = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getStringSet(KEY_ALLOWED, emptySet()) ?: emptySet()
        return packageName in allowed
    }

    fun setAllowed(context: Context, packageName: String, allowed: Boolean) {
        val safeContext = context.applicationContext
        val launchIntent = safeContext.packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent == null) return

        val prefs = safeContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val updated = (prefs.getStringSet(KEY_ALLOWED, defaults) ?: defaults).toMutableSet()
        if (allowed) updated.add(packageName) else updated.remove(packageName)
        prefs.edit()
            .putStringSet(KEY_ALLOWED, updated)
            .putBoolean(KEY_INITIALIZED, true)
            .apply()
    }

    fun getLaunchableApps(context: Context): List<AllowedApp> {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return context.packageManager.queryIntentActivities(intent, 0)
            .mapNotNull { resolve ->
                val packageName = resolve.activityInfo?.packageName ?: return@mapNotNull null
                val label = resolve.loadLabel(context.packageManager)?.toString() ?: packageName
                AllowedApp(packageName, label)
            }
            .distinctBy { it.packageName }
            .sortedBy { it.displayName.lowercase() }
    }

    fun getAllowedApps(context: Context): List<AllowedApp> {
        val allowed = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getStringSet(KEY_ALLOWED, defaults) ?: defaults
        return getLaunchableApps(context).filter { it.packageName in allowed }
    }

    fun getDisplayName(context: Context, packageName: String): String? {
        return getLaunchableApps(context).firstOrNull { it.packageName == packageName }?.displayName
    }
}
