package com.digitres.wifiSandbox.ui.dialog

import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.constraintlayout.compose.ConstraintLayout
import com.digitres.wifiSandbox.util.VersionUtils
import com.digitres.wifiSandbox.util.WifiUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@Composable
fun WifiDialog(
    setShowDialog: (Boolean, String?) -> Unit,
    onAllowSystemDialog: () -> Unit,
    backgroundColor: Color,
    wifiUtil: WifiUtil,
) {

    wifiUtil.setCurrentSSID()
    val password = remember { mutableStateOf("") }
    val ssid = remember { mutableStateOf(wifiUtil.currentSSID.value) }
    val infoTextColor = remember { mutableStateOf(Color.Blue) }
    val checkingAuth = remember { mutableStateOf(false) }
    val passwordVisible = rememberSaveable { mutableStateOf(false) }

    val isWifiEnabled = remember {
        mutableStateOf(wifiUtil.isWifiEnabled())
    }
    val availableNetworks = remember {
        mutableStateListOf<String>()
    }

    println("wifiDialog::wifiUtil.currentSSID.value::${wifiUtil.currentSSID.value}")
    println("wifiDialog::wifiUtil.isWifiConnected()::${wifiUtil.isWifiConnected()}")

    wifiUtil.availableNetworks.observeForever {
        availableNetworks.clear()
        availableNetworks.addAll(it)
    }

    val note =
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) "Please note that WiFi credentials are case-sensitive. If you enter an incorrect password, you will need to disable and re-enable your WiFi from your status bar before attempting to connect to the network again." else "Note that wifi credentials are case sensitive"

    val info =
        remember {
            mutableStateOf(
                if (isWifiEnabled.value) {
                    note
                } else {
                    "Your Wi-Fi is currently disabled. Please enable your Wi-Fi before attempting to connect."
                }
            )
        }


    fun connectWIfi(
        ssid: String,
        password: String,
    ) {
        onAllowSystemDialog()
        checkingAuth.value = true
        wifiUtil.connectWIfi(ssid, password)
        setShowDialog(false, "connect")
    }

    fun disconnectWifi() {
        wifiUtil.disconnectWifi()
        CoroutineScope(IO).launch {
            delay(500)
            withContext(Main) {
                if (VersionUtils.isAndroidQOrLater && wifiUtil.isWifiConnected()) {
                    info.value =
                        "if connection was not established by this app, then disconnect using the system settings"
                }
                if (VersionUtils.isAndroidQOrLater && !wifiUtil.isWifiConnected()) {
                    ssid.value = ""
                }
            }
        }
    }

    Dialog(onDismissRequest = { setShowDialog(false, null) }) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
        ) {
            ConstraintLayout(
                modifier = Modifier
                    .background(Color.White)
                    .wrapContentHeight()
            ) {
                val (headerSection, textFieldContainerRef, connectBtnRef, cancelBtnRef) = createRefs()
                Row(
                    modifier = Modifier
                        .background(color = Color.LightGray)
                        .height(60.dp)
                        .fillMaxWidth()
                        .constrainAs(headerSection) {
                            top.linkTo(parent.top)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Spacer(
                        modifier = Modifier
                            .width(30.dp)
                            .height(30.dp)
                            .weight(2f)
                            .clickable { setShowDialog(false, null) }
                    )
                    Text(
                        text = "WIFI",
                        modifier = Modifier.weight(6f),
                        style = TextStyle(
                            color = backgroundColor,
                            fontSize = 24.sp,
                            fontFamily = FontFamily.Default,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    )
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "close icon",
                        tint = backgroundColor,
                        modifier = Modifier
                            .width(30.dp)
                            .height(30.dp)
                            .weight(2f)
                            .clickable { setShowDialog(false, null) }
                    )
                }

                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .height(220.dp)
                        .constrainAs(textFieldContainerRef)
                        {
                            top.linkTo(headerSection.bottom, 20.dp)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            bottom.linkTo(connectBtnRef.top)
                        }
                ) {
                    if (wifiUtil.isWifiConnected() || ssid.value.isNotEmpty()) {
                        Text(
                            text = info.value,
                            color = infoTextColor.value,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .padding(bottom = 10.dp)
                        )
                        TextField(
                            enabled = false,
                            textStyle = TextStyle(textAlign = TextAlign.Center, fontSize = 18.sp),
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .border(
                                    BorderStroke(
                                        width = 2.dp,
                                        color = infoTextColor.value
                                    ),
                                    shape = RoundedCornerShape(50)
                                ),
                            colors = TextFieldDefaults.textFieldColors(
                                backgroundColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            placeholder = { Text(text = "Enter SSID") },
                            value = ssid.value,
                            onValueChange = {
                                ssid.value = it
                            }
                        )
                        if(!wifiUtil.isWifiConnected()){
                            TextField(

                                modifier = Modifier
                                    .fillMaxWidth(0.8f)
                                    .padding(top = 10.dp)
                                    .border(
                                        BorderStroke(
                                            width = 2.dp,
                                            color = infoTextColor.value
                                        ),
                                        shape = RoundedCornerShape(50)
                                    ),
                                colors = TextFieldDefaults.textFieldColors(
                                    backgroundColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                placeholder = { Text(text = "Enter Password ") },
                                value = password.value,
                                visualTransformation = if (passwordVisible.value) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                onValueChange = {
                                    password.value = it
                                },
                                trailingIcon = {
                                    val description =
                                        if (passwordVisible.value) "Hide password" else "Show password"
                                    IconButton(onClick = {
                                        passwordVisible.value = !passwordVisible.value
                                    }) {
//                                    Icon(imageVector = image, description)
                                    }
                                }
                            )
                        }

                    } else {
                        NetworkLister(
                            availableNetworks = availableNetworks,
                            onSelectNetwork = {
                                println("selected ssid::$it")
                                ssid.value = it
                            }
                        )
                    }
                }

                Button(
                    enabled = !wifiUtil.isWifiConnected() && ssid.value.isNotEmpty(),
                    onClick = {
                        connectWIfi(ssid.value, password.value)
                    },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = backgroundColor,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(50.dp),
                    modifier = Modifier
                        .constrainAs(connectBtnRef) {
                            top.linkTo(textFieldContainerRef.bottom, 10.dp)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            bottom.linkTo(cancelBtnRef.top)
                        }
                        .fillMaxWidth(0.8f)
                        .height(50.dp)
                ) {
                    Text(text = "Connect")
                }
                Button(
                    enabled = wifiUtil.isWifiConnected(),
                    onClick = {
                        disconnectWifi()
                    },
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = backgroundColor,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .constrainAs(cancelBtnRef) {
                            top.linkTo(connectBtnRef.bottom, margin = 5.dp)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            bottom.linkTo(parent.bottom, margin = 20.dp)
                        }
                        .fillMaxWidth(0.8f)
                        .height(50.dp)
                ) {
                    Text(text = "Disconnect")
                }
            }
        }
    }
}

@Preview
@Composable
fun WifiDialogPreview() {
//    WifiDialog(
//        null,
//        value = WifiCredentials("", ""),
//        setShowDialog = {},
//        onAllowSystemDialog = {},
//        backgroundColor = Color.Red,
//    )
}

