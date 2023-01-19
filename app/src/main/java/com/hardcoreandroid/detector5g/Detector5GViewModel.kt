@file:Suppress("DEPRECATION")

package com.hardcoreandroid.detector5g

import android.app.Application
import android.content.Context
import android.os.Build
import android.telephony.PhoneStateListener
import android.telephony.TelephonyCallback
import android.telephony.TelephonyDisplayInfo
import android.telephony.TelephonyManager
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.stateIn
import java.util.concurrent.Executors

class Detector5GViewModel(application: Application): AndroidViewModel(application) {
    val telephonyType = callbackFlow {
        val telephonyManager = application.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        telephonyManager.dataNetworkType
        // The thread Executor used to run the listener. This governs how threads are created and
        // reused. Here we use a single thread.
        val exec = Executors.newSingleThreadExecutor()

        if (Build.VERSION.SDK_INT >= 31) {
            // SDK >= 31 uses TelephonyManager.registerTelephonyCallback() to listen for
            // TelephonyDisplayInfo changes.
            // It does not require any permissions.

            val callback = object : TelephonyCallback(), TelephonyCallback.DisplayInfoListener {
                override fun onDisplayInfoChanged(telephonyDisplayInfo: TelephonyDisplayInfo) {
                    trySend(telephonyDisplayInfo)
                }
            }
            telephonyManager.registerTelephonyCallback(exec, callback)

            awaitClose {
                telephonyManager.unregisterTelephonyCallback(callback)
                exec.shutdown()
            }
        }
        else {
            // SDK 30 uses TelephonyManager.listen() to listen for TelephonyDisplayInfo changes.
            // It requires READ_PHONE_STATE permission.

            @Suppress("OVERRIDE_DEPRECATION")
            val callback = object : PhoneStateListener(exec) {
                override fun onDisplayInfoChanged(telephonyDisplayInfo: TelephonyDisplayInfo) {
                    trySend(telephonyDisplayInfo)
                }
            }
            telephonyManager.listen(callback, PhoneStateListener.LISTEN_DISPLAY_INFO_CHANGED)

            awaitClose {
                telephonyManager.listen(callback, 0)
                exec.shutdown()
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}
