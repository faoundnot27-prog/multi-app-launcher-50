package com.example.multiapplauncher50

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private val selectedApps = mutableListOf<String>()
    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
        }

        val title = TextView(this).apply {
            text = "MULTI APP LAUNCHER 50"
            textSize = 22f
        }

        status = TextView(this).apply {
            text = "Pilih aplikasi yang ingin dibuka"
            textSize = 16f
        }

        val scroll = ScrollView(this)
        val appList = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val launchButton = Button(this).apply {
            text = "BUKA APLIKASI TERPILIH"
            setOnClickListener { launchApps() }
        }

        layout.addView(title)
        layout.addView(status)
        layout.addView(scroll)
        layout.addView(launchButton)
        scroll.addView(appList)

        setContentView(layout)

        loadApps(appList)
    }

    private fun loadApps(container: LinearLayout) {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val apps = packageManager.queryIntentActivities(intent, 0)

        apps.sortedBy { it.loadLabel(packageManager).toString() }
            .forEach { info ->
                val name = info.loadLabel(packageManager).toString()
                val packageName = info.activityInfo.packageName

                val button = Button(this).apply {
                    text = if (selectedApps.contains(packageName)) "✓ $name" else name
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

                        status.text = "Terpilih: ${selectedApps.size}/50"
                    }
                }

                container.addView(button)
            }
    }

    private fun launchApps() {
        if (selectedApps.isEmpty()) {
            Toast.makeText(this, "Pilih aplikasi dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        val firstApp = selectedApps.first()
        val launchIntent = packageManager.getLaunchIntentForPackage(firstApp)

        if (launchIntent != null) {
            startActivity(launchIntent)
        } else {
            Toast.makeText(this, "Aplikasi tidak dapat dibuka", Toast.LENGTH_SHORT).show()
        }
    }
}
