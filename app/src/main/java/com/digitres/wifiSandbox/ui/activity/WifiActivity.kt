package com.digitres.wifiSandbox.ui.activity


import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.digitres.wifiSandbox.ui.dialog.WifiDialog
import com.digitres.wifiSandbox.util.WifiUtil


class WifiActivity : ComponentActivity() {

    lateinit var wifiUtil: WifiUtil
    private var showWifiDialog = mutableStateOf(false)

    private val locationPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isPermissionGranted ->
        if (isPermissionGranted) {
            // init wifiUtil
            wifiUtil = WifiUtil(application)
        } else {
            // ask user to allow permission from settings just in case user selected dany for ever option
            openSettingsScreen()
        }
    }


    private val settingPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    )
    {
        if (hasLocationPermission()) {
            println("settingPermissionLauncher")
            initWifi()
        } else {
            println("::location permission not granted")
        }
    }

    private fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            this@WifiActivity,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED


    private fun openSettingsScreen() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null)
        )
        settingPermissionLauncher.launch(intent)
    }

    private fun requestLocationPermissions() {
        locationPermissionsLauncher.launch(
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // location permission is required for connecting to wifi network
        requestLocationPermissions()

        initWifi()


        // init Ui
        setViewContent()
    }

    private fun initWifi() {
        if (hasLocationPermission()) {
            wifiUtil = WifiUtil(application)
            println("wifiConnected::${wifiUtil.isWifiConnected.value}")
        }
    }

    override fun onDestroy() {
        if (hasLocationPermission()) {
            wifiUtil.unregisterWifiConnectionListener()
        }
        super.onDestroy()
    }

    private fun setViewContent() {
        val availableNetworks = mutableStateListOf<String>()
        if (::wifiUtil.isInitialized) {
            wifiUtil.availableNetworks.observeForever {
                availableNetworks.clear()
                availableNetworks.addAll(it)
            }
        }

        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colors.background
            ) {

                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Button(
                        modifier = Modifier
                            .height(50.dp)
                            .width(200.dp),
                        onClick = { showWifiDialog.value = true }) {
                        Text(text = "open wifi dialog")
                    }
                }

                if (showWifiDialog.value) {
                    WifiDialog(
                        setShowDialog = { show, connection ->
                            showWifiDialog.value = show
                        },
                        onAllowSystemDialog = { },
                        backgroundColor = Color.Blue,
                        wifiUtil = wifiUtil
                    )
                }

            }
        }
    }
}

