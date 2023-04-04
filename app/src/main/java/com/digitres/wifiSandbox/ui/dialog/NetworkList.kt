package com.digitres.wifiSandbox.ui.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NetworkLister(availableNetworks: MutableList<String>, onSelectNetwork: (String) -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .fillMaxHeight(0.8f),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        availableNetworks.forEach { networkName ->
            item {
                Spacer(
                    modifier = Modifier
                        .background(Color.Gray)
                        .height(1.dp)
                        .fillMaxWidth()
                )
            }
            item {
                Row(
                    modifier = Modifier
                        .height(50.dp)
                        .fillMaxWidth()
                        .clickable {
                            onSelectNetwork(networkName)
                        },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        text = networkName,
                    )
                }
            }
        }

        item {
            if (availableNetworks.isEmpty()) {
                CircularProgressIndicator(
                    color = Color.Blue,
                    modifier = Modifier
                        .size(50.dp)
                )
                Text(
                    textAlign = TextAlign.Center,
                    text = "Searching for wifi networks...",
                    color = Color.Blue,
                    fontSize = 18.sp,
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                )
            } else {
                Spacer(
                    modifier = Modifier
                        .background(Color.Gray)
                        .height(1.dp)
                        .fillMaxWidth()
                )
            }
        }
    }
}