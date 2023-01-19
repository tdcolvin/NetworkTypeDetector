package com.hardcoreandroid.detector5g

import android.os.Build
import android.os.Bundle
import android.telephony.TelephonyDisplayInfo
import android.telephony.TelephonyManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.hardcoreandroid.detector5g.ui.theme.Detector5GTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Detector5GTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Detector5GScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun Detector5GScreen(viewModel: Detector5GViewModel = viewModel()) {
    val permissionState = rememberPermissionState(
        android.Manifest.permission.READ_PHONE_STATE
    )

    //No permission is needed for API >= 31
    if (Build.VERSION.SDK_INT >= 31 || permissionState.status == PermissionStatus.Granted) {
        val telephony by viewModel.telephonyType.collectAsStateWithLifecycle()
        NetworkType(telephony)
    }
    else {
        //API < 31 - READ_PHONE_STATE permission is needed
        Column {
            Text("Permission is required")
            Button(onClick = { permissionState.launchPermissionRequest() }) {
                Text(text = "Request Permission")
            }
        }
    }
}

@Composable
fun NetworkType(
    telephony: TelephonyDisplayInfo?
) {

    val baseTypeString = when(telephony?.networkType) {
        TelephonyManager.NETWORK_TYPE_CDMA -> "CDMA"
        TelephonyManager.NETWORK_TYPE_1xRTT -> "1xRTT"
        TelephonyManager.NETWORK_TYPE_EDGE -> "EDGE"
        TelephonyManager.NETWORK_TYPE_EHRPD -> "eHRPD"
        TelephonyManager.NETWORK_TYPE_EVDO_0 -> "EVDO rev 0"
        TelephonyManager.NETWORK_TYPE_EVDO_A -> "EVDO rev A"
        TelephonyManager.NETWORK_TYPE_EVDO_B -> "EVDO rev B"
        TelephonyManager.NETWORK_TYPE_GPRS -> "GPRS"
        TelephonyManager.NETWORK_TYPE_GSM -> "GSM"
        TelephonyManager.NETWORK_TYPE_HSDPA -> "HSDPA"
        TelephonyManager.NETWORK_TYPE_HSPA -> "HSPA"
        TelephonyManager.NETWORK_TYPE_HSPAP -> "HSPA+"
        TelephonyManager.NETWORK_TYPE_HSUPA -> "HSUPA"
        TelephonyManager.NETWORK_TYPE_IDEN -> "iDen"
        TelephonyManager.NETWORK_TYPE_IWLAN -> "IWLAN"
        TelephonyManager.NETWORK_TYPE_LTE -> "LTE"
        TelephonyManager.NETWORK_TYPE_NR -> "NR (new radio) 5G"
        TelephonyManager.NETWORK_TYPE_TD_SCDMA -> "TD_SCDMA"
        TelephonyManager.NETWORK_TYPE_UMTS -> "UMTS"
        else -> "[Unknown]"
    }

    val overrideString = when(telephony?.overrideNetworkType) {
        TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_NSA -> "5G non-standalone"
        TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_ADVANCED -> "5G standalone (advanced)"
        TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_LTE_ADVANCED_PRO -> "LTE Advanced Pro"
        TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_LTE_CA -> "LTE (carrier aggregation)"
        else -> null
    }

    val netTypeString = overrideString ?: baseTypeString
    Text("Network type: $netTypeString")
}