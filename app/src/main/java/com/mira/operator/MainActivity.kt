package com.mira.operator

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.mira.operator.apps.AppRegistry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Evolution 20 native app shell. Local interactions are real; external execution is not connected. */
class MainActivity : Activity() {
    private val pearl = Color.rgb(248, 250, 252)
    private val graphite = Color.rgb(35, 43, 54)
    private val electricBlue = Color.rgb(37, 99, 235)
    private val muted = Color.rgb(105, 116, 130)
    private val border = Color.rgb(220, 226, 234)
    private lateinit var page: LinearLayout
    private lateinit var missionInput: EditText
    private lateinit var missionStatus: TextView
    private lateinit var governanceStatus: TextView
    private var selectedDestination = "Civilization Core"
    private var selectedLayer = "Agent Civilization"
    private var authorityMode = "GOVERNED"

    private val destinations = listOf(
        "Civilization Core", "Command Nexus", "Digital Twin", "Agent Civilization",
        "Creation Engine", "Intelligence Network", "Knowledge Universe",
        "Research & Discovery", "Enterprise OS", "Sovereign Infrastructure",
        "Trust & Security", "Evolution Engine", "Universal Registry", "Settings"
    )
    private val architectureLayers = listOf(
        "Universal Intelligence OS" to "Central intelligence and operating architecture",
        "Civilization Digital Twin" to "System, project and business relationship model",
        "Agent Civilization" to "Identity, delegation and coordination",
        "Creation Engine" to "Software, products and capability development",
        "Intelligence Network" to "Federated systems and model coordination",
        "Knowledge Universe" to "Memory, retrieval and evidence",
        "Enterprise OS" to "Business and operational intelligence",
        "Sovereign Infrastructure" to "Compute, storage and deployment",
        "Trust Architecture" to "Policy, verification and audit",
        "Evolution Engine" to "Controlled improvement and recovery"
    )
    private val governanceItems = listOf(
        "Human approval", "Project isolation", "Evidence provenance", "Rollback readiness"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = pearl
        window.navigationBarColor = pearl
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        val scroll = ScrollView(this).apply { setBackgroundColor(pearl); isFillViewport = true }
        page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(18), dp(18), dp(30))
        }
        scroll.addView(page)
        setContentView(scroll)
        render()
    }

    private fun render() {
        page.removeAllViews()
        addBrandHeader()
        addHero()
        addMission()
        addMetrics()
        addAuthority()
        addNavigation()
        addDestinationContent()
        addGovernance()
        addEventStream()
        addVision()
        addFooter()
    }

    private fun addBrandHeader() {
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        row.addView(label("MIRA NEXUS X", 15f, graphite, true), LinearLayout.LayoutParams(0, -2, 1f))
        row.addView(label("E20  ·  ID 96318", 11f, electricBlue, true))
        page.addView(row)
        page.addView(label("UNIVERSAL INTELLIGENCE CIVILIZATION OS", 10f, muted, true).apply {
            setPadding(0, dp(5), 0, dp(14))
        })
    }

    private fun addHero() = panel {
        addView(label("MIRA NEXUS X", 27f, graphite, true))
        addView(label("UNIVERSAL INTELLIGENCE CIVILIZATION OS", 11f, electricBlue, true).apply { setPadding(0, dp(7), 0, dp(8)) })
        addView(label("E20  ·  OMNIPOTENCE EDITION", 10f, muted, true))
        addView(label("One Intelligence Civilization.", 19f, graphite, true).apply { setPadding(0, dp(18), 0, dp(5)) })
        addView(label("A unified operating architecture for intelligence, creation, enterprise and governed autonomy.", 13f, muted, false))
    }

    private fun addMission() {
        heading("UNIVERSAL MISSION INTERFACE")
        panel {
            addView(label("Mission: Build a globally scalable, secure and economically sustainable intelligence ecosystem", 14f, graphite, true))
            addView(label("Mission objective", 12f, muted, true).apply { setPadding(0, dp(15), 0, dp(6)) })
            missionInput = EditText(this@MainActivity).apply {
                hint = "Describe a mission objective"
                textSize = 14f
                setTextColor(graphite)
                setHintTextColor(muted)
                setPadding(dp(12), dp(10), dp(12), dp(10))
                minHeight = dp(48)
                background = rounded(Color.WHITE, border, 10)
            }
            addView(missionInput)
            addView(primaryButton("Register mission") {
                val objective = missionInput.text.toString().trim()
                if (objective.isEmpty()) {
                    missionStatus.text = "Enter a mission objective first."
                } else {
                    val prefs = getSharedPreferences("mira_nexus_local", MODE_PRIVATE)
                    val old = prefs.getString("missions", "") ?: ""
                    val item = "${System.currentTimeMillis()}|${objective.replace('\n', ' ')}"
                    prefs.edit().putString("missions", listOf(old, item).filter { it.isNotBlank() }.joinToString("\n")).apply()
                    missionStatus.text = "Saved locally on this device. No agent execution was started."
                    addEvent("Mission objective registered locally")
                    missionInput.text.clear()
                }
            })
            missionStatus = label("Local mission register. Registration saves a record on this device; it does not execute a mission.", 11f, muted, false)
            addView(missionStatus)
        }
    }

    private fun addMetrics() {
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        listOf("ARCHITECTURE" to "10", "AGENTS" to "4", "CAPABILITIES" to "5").forEach { (name, number) ->
            val tile = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                background = rounded(Color.WHITE, border, 12)
                setPadding(dp(10), dp(13), dp(8), dp(13))
                addView(label(number, 22f, electricBlue, true))
                addView(label(name, 9f, muted, true))
            }
            val lp = LinearLayout.LayoutParams(0, -2, 1f)
            if (row.childCount > 0) lp.marginStart = dp(8)
            row.addView(tile, lp)
        }
        page.addView(row)
        page.addView(label("Illustrative architecture/configuration counts — not live agent or capability telemetry.", 10f, muted, false).apply {
            setPadding(2, dp(6), 2, dp(10))
        })
    }

    private fun addAuthority() {
        heading("AUTHORITY CONFIGURATION")
        panel {
            addView(label("Current authority mode", 12f, muted, true))
            val row = LinearLayout(this@MainActivity).apply { orientation = LinearLayout.HORIZONTAL }
            listOf("GOVERNED", "REVIEW", "RESTRICTED").forEach { mode ->
                val button = Button(this@MainActivity).apply {
                    text = mode; textSize = 9f; isAllCaps = false
                    setTextColor(if (authorityMode == mode) Color.WHITE else graphite)
                    background = rounded(if (authorityMode == mode) electricBlue else pearl, border, 9)
                    setOnClickListener { authorityMode = mode; addEvent("Local authority preference changed to $mode"); render() }
                }
                val lp = LinearLayout.LayoutParams(0, dp(44), 1f)
                if (row.childCount > 0) lp.marginStart = dp(6)
                row.addView(button, lp)
            }
            addView(row)
            addView(label("Selection is a local interface preference. It does not grant or enforce agent permissions.", 10f, muted, false).apply { setPadding(0, dp(8), 0, 0) })
        }
    }

    private fun addNavigation() {
        heading("UNIFIED CIVILIZATION NAVIGATION")
        val horizontal = HorizontalScrollView(this).apply { isHorizontalScrollBarEnabled = false }
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        destinations.forEach { destination ->
            val button = Button(this).apply {
                text = destination; textSize = 11f; isAllCaps = false
                setTextColor(if (selectedDestination == destination) Color.WHITE else graphite)
                background = rounded(if (selectedDestination == destination) electricBlue else Color.WHITE, border, 18)
                setPadding(dp(12), 0, dp(12), 0)
                setOnClickListener { selectedDestination = destination; render() }
            }
            row.addView(button, LinearLayout.LayoutParams(-2, dp(42)).apply { marginEnd = dp(7) })
        }
        horizontal.addView(row)
        page.addView(horizontal)
        page.addView(label("Swipe horizontally to access all 14 destinations.", 10f, muted, false).apply { setPadding(0, dp(5), 0, dp(8)) })
    }

    private fun addDestinationContent() {
        heading(selectedDestination.uppercase(Locale.ROOT))
        if (selectedDestination == "Civilization Core") addCore() else if (selectedDestination == "Settings") addSettings() else panel {
            addView(label(selectedDestination, 17f, graphite, true))
            addView(label("This destination is part of the Evolution 20 architecture map. Its operational module is not yet implemented.", 12f, muted, false).apply { setPadding(0, dp(8), 0, 0) })
        }
    }

    private fun addCore() {
        panel {
            addView(label("UNIVERSAL CIVILIZATION CORE", 17f, graphite, true))
            addView(label("সমগ্র প্ল্যাটফর্মের কেন্দ্রীয় Intelligence ও Governance architecture।", 12f, muted, false).apply { setPadding(0, dp(7), 0, dp(14)) })
            addView(label("MIRA NEXUS CORE", 13f, electricBlue, true))
            addView(label("UNIVERSAL INTELLIGENCE FABRIC", 11f, graphite, true).apply { setPadding(0, dp(5), 0, dp(3)) })
            addView(label("IDENTITY · MISSION · REASONING · AUTHORITY", 9f, muted, true))
        }
        heading("TEN ARCHITECTURE LAYERS")
        architectureLayers.forEachIndexed { index, (name, description) ->
            panel {
                val row = LinearLayout(this@MainActivity).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
                row.addView(label("${index + 1}.", 13f, electricBlue, true).apply { setPadding(0, 0, dp(10), 0) })
                val column = LinearLayout(this@MainActivity).apply { orientation = LinearLayout.VERTICAL }
                column.addView(label(name, 13f, graphite, true))
                column.addView(label(description, 11f, muted, false).apply { setPadding(0, dp(3), 0, 0) })
                row.addView(column, LinearLayout.LayoutParams(0, -2, 1f))
                row.addView(label(if (selectedLayer == name) "SELECTED" else "SELECT", 9f, if (selectedLayer == name) electricBlue else muted, true))
                addView(row)
                setOnClickListener { selectedLayer = name; render() }
            }
        }
        panel {
            addView(label("SELECTED SYSTEM LAYER", 10f, muted, true))
            addView(label(selectedLayer, 16f, electricBlue, true).apply { setPadding(0, dp(5), 0, 0) })
        }
    }

    private fun addGovernance() {
        heading("UNIVERSAL GOVERNANCE & EVIDENCE CENTER")
        panel {
            addView(label("Local governance preferences", 14f, graphite, true))
            val checkboxes = mutableListOf<CheckBox>()
            governanceItems.forEach { item ->
                val check = CheckBox(this@MainActivity).apply {
                    text = item; textSize = 13f; setTextColor(graphite)
                    isChecked = getSharedPreferences("mira_nexus_local", MODE_PRIVATE).getBoolean("gov_$item", true)
                    buttonTintList = ColorStateList.valueOf(electricBlue)
                    setOnCheckedChangeListener { _, checked ->
                        getSharedPreferences("mira_nexus_local", MODE_PRIVATE).edit().putBoolean("gov_$item", checked).apply()
                        governanceStatus.text = "${checkboxes.count { it.isChecked }} controls selected"
                    }
                }
                checkboxes.add(check)
                addView(check)
            }
            governanceStatus = label("${checkboxes.count { it.isChecked }} controls selected", 11f, electricBlue, true).apply { setPadding(0, dp(8), 0, 0) }
            addView(governanceStatus)
            addView(label("These are saved UI preferences only; they do not yet enforce policy, approvals, isolation or rollback in the operator service.", 10f, muted, false).apply { setPadding(0, dp(7), 0, 0) })
        }
    }

    private fun addEventStream() {
        heading("ILLUSTRATIVE EVENT STREAM")
        panel {
            loadEvents().forEach { event -> addView(label("• $event", 11f, graphite, false).apply { setPadding(0, dp(4), 0, dp(4)) }) }
            val actions = LinearLayout(this@MainActivity).apply { orientation = LinearLayout.HORIZONTAL }
            actions.addView(primaryButton("Add event") { addEvent("User added a local event"); render() }, LinearLayout.LayoutParams(0, dp(46), 1f))
            actions.addView(primaryButton("Clear events") {
                getSharedPreferences("mira_nexus_local", MODE_PRIVATE).edit().remove("events").apply(); render()
            }, LinearLayout.LayoutParams(0, dp(46), 1f).apply { marginStart = dp(8) })
            addView(actions)
            addView(label("Events are stored locally and are not server audit records.", 10f, muted, false).apply { setPadding(0, dp(7), 0, 0) })
        }
    }

    private fun addVision() {
        heading("LONG-TERM ARCHITECTURE VISION")
        panel {
            listOf(
                "Universal Intelligence" to "Reasoning, research, learning and intelligence coordination",
                "Universal Creation" to "AI, software, models, tools and products",
                "Universal Collaboration" to "Agents, systems, knowledge and enterprises",
                "Universal Trust" to "Security, authority, verification and recovery",
                "Continuous Evolution" to "Controlled improvement and future technology adaptation"
            ).forEach { (name, description) ->
                addView(label(name, 13f, graphite, true))
                addView(label(description, 11f, muted, false).apply { setPadding(0, dp(3), 0, dp(10)) })
            }
            addView(label("Vision and roadmap domains — not claims of completed capability.", 10f, electricBlue, true))
        }
    }

    private fun addSettings() {
        panel {
            addView(label("LOCAL APP SETTINGS", 15f, graphite, true))
            addView(label("Open Android Accessibility Settings to review or disable the existing user-enabled service.", 12f, muted, false).apply { setPadding(0, dp(7), 0, dp(10)) })
            addView(primaryButton("Open Accessibility Settings") { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) })
            addView(primaryButton("Open ChatGPT") { openAllowedApp("com.openai.chatgpt") })
            addView(primaryButton("Open Gemini") { openAllowedApp("com.google.android.googlequicksearchbox") })
            val stopPrefs = getSharedPreferences("operator", MODE_PRIVATE)
            val isEmergencyStopped = stopPrefs.getBoolean("emergency_stop", false)
            addView(label(
                "Emergency Stop pauses Mira's accessibility-event processing. Android's Accessibility Service remains enabled until you disable it in system settings.",
                10f, muted, false
            ).apply { setPadding(0, dp(8), 0, dp(8)) })
            addView(primaryButton(if (isEmergencyStopped) "Resume Observation" else "Emergency Stop") {
                val stopNow = !stopPrefs.getBoolean("emergency_stop", false)
                stopPrefs.edit().putBoolean("emergency_stop", stopNow).apply()
                addEvent(if (stopNow) "Emergency Stop enabled" else "Observation resumed by user")
                Toast.makeText(
                    this@MainActivity,
                    if (stopNow) "Observation stopped." else "Observation resumed.",
                    Toast.LENGTH_LONG
                ).show()
                render()
            })
        }
    }

    private fun addFooter() {
        page.addView(label("MIRA NEXUS X", 13f, graphite, true).apply { setPadding(0, dp(20), 0, dp(4)) })
        page.addView(label("96318 · E20", 11f, electricBlue, true))
        page.addView(label("UNIVERSAL INTELLIGENCE CIVILIZATION OS", 10f, muted, true).apply { setPadding(0, dp(4), 0, dp(4)) })
        page.addView(label("OMNIPOTENCE EDITION · EVOLUTION 20", 10f, muted, true))
        page.addView(label("NATIVE ANDROID APP · LOCAL MODE", 9f, electricBlue, true).apply { setPadding(0, dp(9), 0, dp(3)) })
        page.addView(label("External agents, models, APIs and services are not connected. Unimplemented modules are identified in the interface.", 10f, muted, false))
    }

    private fun addEvent(message: String) {
        val prefs = getSharedPreferences("mira_nexus_local", MODE_PRIVATE)
        val current = prefs.getString("events", null)?.split("\n") ?: listOf(
            "Civilization workspace initialized — Design event 1",
            "Universal identity architecture defined — Design event 2",
            "Trust boundaries established — Design event 3"
        )
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        prefs.edit().putString("events", (current + "$message — $timestamp").takeLast(100).joinToString("\n")).apply()
    }

    private fun loadEvents(): List<String> = getSharedPreferences("mira_nexus_local", MODE_PRIVATE)
        .getString("events", null)?.split("\n") ?: listOf(
            "Civilization workspace initialized — Design event 1",
            "Universal identity architecture defined — Design event 2",
            "Trust boundaries established — Design event 3"
        )

    private fun openAllowedApp(packageName: String) {
        if (!AppRegistry.isAllowed(packageName)) { toast("App is not allowlisted"); return }
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        if (intent == null) toast("App is not installed or unavailable") else try { startActivity(intent) } catch (_: Exception) { toast("Unable to open app") }
    }

    private fun heading(value: String) {
        page.addView(label(value, 11f, electricBlue, true).apply { setPadding(dp(2), dp(17), dp(2), dp(8)) })
    }

    private fun panel(block: LinearLayout.() -> Unit): LinearLayout {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = rounded(Color.WHITE, border, 14)
            setPadding(dp(14), dp(14), dp(14), dp(14))
            block()
        }
        page.addView(box, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(10) })
        return box
    }

    private fun label(value: String, size: Float, color: Int, bold: Boolean) = TextView(this).apply {
        text = value; textSize = size; setTextColor(color); includeFontPadding = true
        if (bold) setTypeface(typeface, Typeface.BOLD)
    }

    private fun primaryButton(caption: String, action: () -> Unit) = Button(this).apply {
        text = caption; textSize = 11f; isAllCaps = false; minHeight = dp(44)
        setTextColor(Color.WHITE); background = rounded(electricBlue, electricBlue, 10)
        setOnClickListener { action() }
    }

    private fun rounded(fill: Int, stroke: Int, radius: Int): GradientDrawable = GradientDrawable().apply {
        setColor(fill); cornerRadius = dp(radius).toFloat(); setStroke(dp(1), stroke)
    }

    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
