package com.digitres.wifiSandbox.util

import android.text.TextUtils

object SSIDUtils {

    fun convertToQuotedString(ssid: String): String? {
        if (TextUtils.isEmpty(ssid)) {
            return ""
        }
        val lastPos = ssid.length - 1
        return if ((lastPos < 0 || ssid[0] == '"') && ssid[lastPos] == '"') {
            ssid
        } else "\"" + ssid + "\""
    }
}