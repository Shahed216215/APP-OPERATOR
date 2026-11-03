package com.mira.operator.apps

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppRegistryTest {

    @Test
    fun exposesOnlyTheThreeConfiguredPackages() {
        assertEquals(
            setOf(
                "com.openai.chatgpt",
                "com.google.android.googlequicksearchbox",
                "com.android.chrome"
            ),
            AppRegistry.getAllowedApps().map { it.packageName }.toSet()
        )
    }

    @Test
    fun recognizesEachConfiguredPackage() {
        assertTrue(AppRegistry.isAllowed("com.openai.chatgpt"))
        assertTrue(AppRegistry.isAllowed("com.google.android.googlequicksearchbox"))
        assertTrue(AppRegistry.isAllowed("com.android.chrome"))
    }

    @Test
    fun rejectsUnknownAndMalformedPackageNames() {
        assertFalse(AppRegistry.isAllowed("com.example.unknown"))
        assertFalse(AppRegistry.isAllowed(""))
        assertFalse(AppRegistry.isAllowed(" COM.OPENAI.CHATGPT "))
        assertFalse(AppRegistry.isAllowed("COM.OPENAI.CHATGPT"))
    }

    @Test
    fun returnsExpectedDisplayNames() {
        assertEquals("ChatGPT", AppRegistry.getDisplayName("com.openai.chatgpt"))
        assertEquals("Gemini", AppRegistry.getDisplayName("com.google.android.googlequicksearchbox"))
        assertEquals("Google Chrome", AppRegistry.getDisplayName("com.android.chrome"))
        assertNull(AppRegistry.getDisplayName("com.example.unknown"))
    }

    @Test
    fun returnedAppListCannotMutateRegistry() {
        val returned = AppRegistry.getAllowedApps() as MutableList
        returned.clear()

        assertEquals(3, AppRegistry.getAllowedApps().size)
        assertTrue(AppRegistry.isAllowed("com.openai.chatgpt"))
    }
}
