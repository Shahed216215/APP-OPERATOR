package com.mira.operator.apps

data class AllowedApp(
    val packageName: String,
    val displayName: String
)

object AppRegistry {

    private val allowedApps = listOf(
        AllowedApp(
            packageName = "com.openai.chatgpt",
            displayName = "ChatGPT"
        ),
        AllowedApp(
            packageName = "com.google.android.googlequicksearchbox",
            displayName = "Gemini"
        ),
        AllowedApp(
            packageName = "com.android.chrome",
            displayName = "Google Chrome"
        )
    )

    fun isAllowed(packageName: String): Boolean {
        return allowedApps.any {
            it.packageName == packageName
        }
    }

    fun getAllowedApps(): List<AllowedApp> {
        return allowedApps.toList()
    }

    fun getDisplayName(packageName: String): String? {
        return allowedApps.firstOrNull {
            it.packageName == packageName
        }?.displayName
    }
}
