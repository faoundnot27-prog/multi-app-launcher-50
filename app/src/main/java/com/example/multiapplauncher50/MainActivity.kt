package com.example.multiapplauncher50

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private val selectedApps = mutableListOf<String>()
    private lateinit var status: TextView
    private lateinit var queue: TextView
    private lateinit var appList: LinearLayout

    private var currentIndex = 0
    private var running = false
    private var paused = false

    private val prefs by lazy {
        getSharedPreferences("launcher_data", MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        selectedApps.addAll(
            prefs.getString("selected_apps", "")
                ?.split("\n")
                ?.filter { it.isNotBlank() }
                ?: emptyList()
        )

        currentIndex = prefs.getInt("current_index", 0)
        running = prefs.getBoolean("running", false)
        paused = prefs.getBoolean("paused", false)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 20, 20, 20)
        }

        val title = TextView(this).apply {
            text = "MULTI APP LAUNCHER 50"
            textSize = 22f
        }

        status = TextView(this).apply {
            textSize = 16f
        }

        queue = TextView(this).apply {
            textSize = 15f
        }

        appList = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val scroll = ScrollView(this)
        scroll.addView(appList)

        val startButton = Button(this).apply {
            text = "MULAI"
            setOnClickListener { startSequence() }
        }

        val nextButton = Button(this).apply {
            text = "BERIKUTNYA"
            setOnClickListener { nextApp() }
        }

        val pauseButton = Button(this).apply {
            text = "JEDA / LANJUTKAN"
            setOnClickListener { togglePause() }
        }

        val stopButton = Button(this).apply {
            text = "BERHENTI"
            setOnClickListener { stopSequence() }
        }

        layout.addView(title)
        layout.addView(status)
        layout.addView(queue)
        layout.addView(startButton)
        layout.addView(nextButton)
        layout.addView(pauseButton)
        layout.addView(stopButton)

        layout.addView(
            scroll,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        setContentView(layout)

        loadApps()
        updateDisplay()
    }

    private fun saveData() {
        prefs.edit()
            .putString("selected_apps", selectedApps.joinToString("\n"))
            .putInt("current_index", currentIndex)
            .putBoolean("running", running)
            .putBoolean("paused", paused)
            .apply()
    }

    private fun loadApps() {
        appList.removeAllViews()

        val intent = Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_LAUNCHER)

        val apps = packageManager.queryIntentActivities(intent, 0)

        apps.sortedBy {
            it.loadLabel(packageManager).toString()
        }.forEach { info ->

            val name = info.loadLabel(packageManager).toString()
            val packageName = info.activityInfo.packageName

            val button = Button(this).apply {
                text = if (selectedApps.contains(packageName)) {
                    "✓ $name"
                } else {
                    name
                }

                setOnClickListener {
                    if (selectedApps.contains(packageName)) {
                        selectedApps.remove(packageName)
                        text = name
                    } else if (selectedApps.size < 50) {
                        selectedApps.add(packageName)
                        text = "✓ $name"
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            "Maksimal 50 aplikasi",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    currentIndex = 0
                    running = false
                    paused = false
                    saveData()
                    updateDisplay()
                }
            }

            appList.addView(button)
        }
    }

    private fun startSequence() {
        if (selectedApps.isEmpty()) {
            Toast.makeText(
                this,
                "Pilih aplikasi dahulu",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        currentIndex = 0
        running = true
        paused = false
        saveData()
        launchCurrentApp()
    }

    private fun nextApp() {
        if (!running) {
            Toast.makeText(this, "Tekan MULAI dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        if (paused) {
            Toast.makeText(this, "Lanjutkan dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        currentIndex++

        if (currentIndex >= selectedApps.size) {
            stopSequence()
            Toast.makeText(this, "Semua aplikasi selesai dibuka", Toast.LENGTH_LONG).show()
            return
        }

        saveData()
        launchCurrentApp()
    }

    private fun launchCurrentApp() {
        if (currentIndex !in selectedApps.indices) return

        val packageName = selectedApps[currentIndex]
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)

        if (launchIntent != null) {
            updateDisplay()
            startActivity(launchIntent)
        } else {
            Toast.makeText(
                this,
                "Aplikasi tidak ditemukan",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun togglePause() {
        if (!running) return

        paused = !paused
        saveData()
        updateDisplay()
    }

    private fun stopSequence() {
        running = false
        paused = false
        currentIndex = 0
        saveData()
        updateDisplay()
    }

    private fun updateDisplay() {
        status.text = when {
            !running -> "Status: Siap | Terpilih: ${selectedApps.size}/50"
            paused -> "Status: Dijeda"
            else -> "Status: Berjalan | Aplikasi ${currentIndex + 1}/${selectedApps.size}"
        }

        queue.text = "Urutan aplikasi:\n" +
            selectedApps.mapIndexed { index, packageName ->
                "${index + 1}. $packageName"
            }.joinToString("\n")
    }
}
