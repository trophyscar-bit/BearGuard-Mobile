package com.bearguard.mobile

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * matt/2026-08-15: no real UI yet -- this is just the on-ramp to the Accessibility Settings
 * screen, since enabling BearGuardAccessibilityService requires one manual tap there (Android
 * doesn't let an app self-enable its own accessibility service). Everything the app actually
 * does happens in the service, not here.
 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 96, 48, 48)
        }

        root.addView(TextView(this).apply {
            text = "BearGuard Mobile\n\nTap below, then enable \"BearGuard Mobile\" under " +
                    "Downloaded apps in Accessibility settings."
            textSize = 16f
        })

        root.addView(Button(this).apply {
            text = "Open Accessibility Settings"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
        })

        setContentView(root)
    }
}
