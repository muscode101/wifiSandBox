package com.digitres.wifiSandbox.util

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences


class Preferences(context: Context) {
    private var sharedPreferences: SharedPreferences

    init {
        sharedPreferences = context.getSharedPreferences(context.packageName, MODE_PRIVATE)
    }
    fun editor(): SharedPreferences.Editor = this.sharedPreferences.edit()

    fun instance() = sharedPreferences

    fun clear() {
        sharedPreferences.edit().clear().commit()
    }

}
