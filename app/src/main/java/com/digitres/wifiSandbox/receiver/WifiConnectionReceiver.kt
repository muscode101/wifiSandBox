package com.digitres.wifiSandbox.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.wifi.SupplicantState
import android.net.wifi.WifiManager
import android.widget.Toast

class WifiConnectionReceiver :
    BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION == action) {
            when (intent.getParcelableExtra<SupplicantState>(WifiManager.EXTRA_NEW_STATE)) {
                SupplicantState.COMPLETED -> {
                    wifiLog(
                        "Wifi connected successfully".toUpperCase(), context
                    )
                }
                SupplicantState.DISCONNECTED -> {
                    val error = intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, -1)
                    if (error == WifiManager.ERROR_AUTHENTICATING) {
                        wifiLog("Wifi connection failed".toUpperCase(), context)
                    }
                }
                else -> wifiLog("Authenticating...".toUpperCase(), context)
            }
        }
    }

    private fun wifiLog(text: String, context: Context) {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show()
    }
}