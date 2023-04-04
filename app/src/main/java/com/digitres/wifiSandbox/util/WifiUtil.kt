package com.digitres.wifiSandbox.util

import android.Manifest
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.*
import android.net.wifi.*
import android.os.Build
import android.os.CountDownTimer
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.ActivityCompat
import androidx.lifecycle.MutableLiveData
import com.digitres.wifiSandbox.util.VersionUtils.isLollipopOrLater
import java.util.*

class WifiUtil(private val application: Application) {

    private lateinit var wifiManager: WifiManager
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var wifiReceiver: BroadcastReceiver
    private lateinit var wifiScanReceiver: BroadcastReceiver
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback

    val currentSSID: MutableState<String> = mutableStateOf("")
    val availableNetworks: MutableLiveData<List<String>> by lazy {
        MutableLiveData<List<String>>()
    }

    val isWifiConnected: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>(false)
    }

    init {
        // to listen for wifi connection result on android 10 plus
        initNetworkCallback()

        // to listen for wifi connection result on android below 10 plus
        initWifiConnectionReceiver()
        initWifiScannerReceiver()
        registerWifiConnectionListener()

        // used to connect to wifi on android 9 and below as well as check if device is connected to wifi
        initWifiManager()

        setCurrentSSID()

        startScan()
    }

    private fun startScan() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        application.registerReceiver(wifiScanReceiver, intentFilter)
        wifiManager.startScan()
    }

    fun setCurrentSSID(): String? {
        val wifiInfo = wifiManager.connectionInfo
        val ssid = wifiInfo.ssid.replace("\"", "")
        if (ssid != "<unknown ssid>") {
            currentSSID.value = ssid
        }
        println("inGetCurrentNetworkSSID::${ssid}")
        return if (isWifiConnected()) {
            ssid
        } else {
            null
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun connectWifiAndroidTenPlus(ssid: String, password: String) {
        val v = View(application)
        v.setOnClickListener { }
        println("connectWifiAndroidTenPlus")
        val wifiNetworkSpecifier = WifiNetworkSpecifier
            .Builder()
            .setSsid(ssid)
            .setWpa2Passphrase(password)
            .build()
        val networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .setNetworkSpecifier(wifiNetworkSpecifier)
            .build()
        connectivityManager.requestNetwork(networkRequest, networkCallback)
    }

    private fun initWifiScannerReceiver() {
        wifiScanReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val success =
                    intent?.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false) ?: false
                if (success) {
                    val results = if (ActivityCompat.checkSelfPermission(
                            application,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        wifiManager.scanResults
                    } else {
                        listOf()
                    }

                    if (results.isEmpty()) {
                        println("no networks")
                    } else {
                        val availableNetworkNames = mutableListOf<String>()
                        results.map { availableNetworkNames.add(it.SSID) }
                        availableNetworks.postValue(availableNetworkNames)
                    }
                }
            }
        }
    }


    private fun initWifiConnectionReceiver() {
        wifiReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val action = intent.action
                if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION == action) {
                    when (intent.getParcelableExtra<SupplicantState>(WifiManager.EXTRA_NEW_STATE)) {
                        SupplicantState.COMPLETED -> {
                            wifiLog(
                                "Wifi connected successfully".uppercase(), context
                            )
                            setCurrentSSID()
                            isWifiConnected.postValue(true)
                        }
                        SupplicantState.DISCONNECTED -> {
                            val error = intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, -1)
                            if (error == WifiManager.ERROR_AUTHENTICATING) {
                                wifiLog("Wifi connection failed".uppercase(), context)
                            }
                        }
                        SupplicantState.AUTHENTICATING -> {
                            wifiLog("AUTHENTICATING...", context)
                        }
                        SupplicantState.INTERFACE_DISABLED -> {
                            wifiLog("INTERFACE_DISABLED...", context)
                        }
                        SupplicantState.INACTIVE -> {
                            wifiLog("INACTIVE...", context)
                        }
                        SupplicantState.SCANNING -> {
                            wifiLog("SCANNING...", context)
                        }
                        SupplicantState.ASSOCIATING -> {
                            wifiLog("ASSOCIATING...", context)
                        }
                        SupplicantState.ASSOCIATED -> {
                            wifiLog("ASSOCIATED...", context)
                        }
                        SupplicantState.FOUR_WAY_HANDSHAKE -> {
                            wifiLog("FOUR_WAY_HANDSHAKE...", context)
                        }
                        SupplicantState.GROUP_HANDSHAKE -> {
                            wifiLog("GROUP_HANDSHAKE...", context)
                        }
                        SupplicantState.DORMANT -> {
                            wifiLog("DORMANT...", context)
                        }
                        SupplicantState.UNINITIALIZED -> {
                            wifiLog("UNINITIALIZED...", context)
                        }
                        SupplicantState.INVALID -> {
                            wifiLog("INVALID...", context)
                        }
                        null -> {}
                    }
                }
            }

            private fun wifiLog(text: String, context: Context) {
                Toast.makeText(context, text, Toast.LENGTH_LONG).show()
            }
        }
    }



    private fun initNetworkCallback() {
        connectivityManager =
            application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                super.onCapabilitiesChanged(network, networkCapabilities)
                println("onCapabilitiesChanged::${network}")
            }

            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                println("onAvailable")
                connectivityManager.bindProcessToNetwork(network)
                isWifiConnected.postValue(true)
                setCurrentSSID()
            }

            override fun onUnavailable() {
                super.onUnavailable()
                println("onUnavailable")

                val note =
                    if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) "Wrong password or wifi name, please restart device and try again" else "Wrong password or wifi name, check your credentials and try again"
                val toast = Toast.makeText(application, note, Toast.LENGTH_LONG)
                object : CountDownTimer(20000, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        toast.show()
                    }

                    override fun onFinish() {
                        toast.cancel()
                    }
                }.start()
                isWifiConnected.postValue(false)
                currentSSID.value = ""
            }

            override fun onBlockedStatusChanged(network: Network, blocked: Boolean) {
                super.onBlockedStatusChanged(network, blocked)
                isWifiConnected.postValue(false)
                currentSSID.value = ""
            }
        }
    }

    private fun initWifiManager() {
        wifiManager = application.getSystemService(ComponentActivity.WIFI_SERVICE) as WifiManager
        isWifiConnected.postValue(isWifiConnected())
    }

    private fun registerWifiConnectionListener() {
        if (!VersionUtils.isAndroidQOrLater) {
            val filter = IntentFilter(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)
            application.registerReceiver(wifiReceiver, filter)
        }
    }

    fun connectWIfi(
        ssid: String,
        password: String,
    ) {
        if (VersionUtils.isAndroidQOrLater) {
            connectWifiAndroidTenPlus(ssid, password)
        } else {
            connectWIfiBelowAndroidTen(ssid, password)
        }
    }



    private fun connectWIfiBelowAndroidTen(
        ssid: String,
        password: String,
    ) {
        try {
            val wifiConfig = WifiConfiguration()
            wifiConfig.SSID = "\"" + ssid + "\""
            wifiConfig.preSharedKey = "\"" + password + "\""
            val netId = wifiManager.addNetwork(wifiConfig)
            wifiManager.disconnect()
            wifiManager.enableNetwork(netId, true)
            wifiManager.reconnect()
            setCurrentSSID()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(application, "connection failed", Toast.LENGTH_LONG).show()
        }
    }

    fun disconnectWifi() {
        if (VersionUtils.isAndroidQOrLater) {
            disconnectWifiAndroidTenPlus()
        } else {
            disconnectWIfiBelowAndroidTen()
        }
    }

    private fun disconnectWifiAndroidTenPlus() {
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
            isWifiConnected.postValue(false)
            currentSSID.value = ""
        } catch (e: Exception) {
            println("disconnectWifi::error::${e.message}")
            println("disconnectWifi::error::if NetworkCallback was not registered then cant disconnect if this app is not the one that established the connection")
        }
    }

    private fun disconnectWIfiBelowAndroidTen(): Boolean {
        return try {
            wifiManager.disableNetwork(wifiManager.connectionInfo.networkId)
            wifiManager.disconnect()
            isWifiConnected.postValue(false)
            currentSSID.value = ""
            true
        } catch (e: java.lang.Exception) {
            println("disconnectWifi::error::${e.message}")
            false
        }
    }

    fun unregisterWifiConnectionListener() {
        if (!VersionUtils.isAndroidQOrLater) {
            application.unregisterReceiver(wifiReceiver)
        }
        application.unregisterReceiver(wifiScanReceiver)
    }

    fun isWifiEnabled(): Boolean = wifiManager.isWifiEnabled



    fun isWifiConnected(): Boolean {
        return if (isLollipopOrLater) {
            isConnectedToNetworkLollipop()
        } else {
            val networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
            return networkInfo?.state == NetworkInfo.State.CONNECTED
        }
    }

    fun isWifiConnectedTo(ssid: String?): Boolean {
        if (ssid != null) {
            if (wifiManager.connectionInfo != null &&
                wifiManager.connectionInfo.ssid != null &&
                wifiManager.connectionInfo.ipAddress != 0 &&
                ssid == wifiManager.connectionInfo.ssid
            ) {
                return true
            }
        }
        return false
    }


    private fun isConnectedToNetworkLollipop(): Boolean {
        var isWifiConn = false
        for (network in connectivityManager.allNetworks) {
            val networkInfo = connectivityManager.getNetworkInfo(network)
            if (networkInfo != null && ConnectivityManager.TYPE_WIFI == networkInfo.type) {
                isWifiConn = isWifiConn or networkInfo.isConnected
            }
        }
        return isWifiConn
    }

}