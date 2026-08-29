package com.example.phoneinfo

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.widget.Toast

/**
 * Zero-UI launcher. Tapping the icon jumps straight to Android's
 * built-in "Phone information" testing screen (the same screen you
 * reach by dialing *#*#4636#*#*), then this Activity finishes itself
 * immediately so Back from that screen goes straight home.
 *
 * We target the component directly instead of using the dialer's
 * secret-code broadcast, because that broadcast is a protected
 * broadcast as of Android 8 (Oreo) - a third-party app is not allowed
 * to send it. Going straight to the exported Activity that the secret
 * code itself resolves to is the supported, documented way for an app
 * to reach it.
 */
class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = Intent().apply {
            setClassName("com.android.phone", "com.android.phone.settings.RadioInfo")
        }

        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                this,
                "Couldn't find the Phone information screen on this build. " +
                    "Try dialing *#*#4636#*#* instead.",
                Toast.LENGTH_LONG
            ).show()
        }

        finish()
    }
}
