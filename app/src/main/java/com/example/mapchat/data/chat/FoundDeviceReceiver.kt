package com.example.mapchat.data.chat

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class FoundDeviceReceiver (
    private val onDeviceFound: (BluetoothDevice) -> Unit
): BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        when(intent?.action) {
            BluetoothDevice.ACTION_FOUND -> {
                val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(
                        BluetoothDevice.EXTRA_DEVICE,
                        BluetoothDevice::class.java
                    )
                } else {
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                }
                device?.let(onDeviceFound)
            }
        }
    }
}

// This code defines a BroadcastReceiver called FoundDeviceReceiver that is
// designed to handle Bluetooth device discovery events in Android.
// Let's break down its functionality:

//Constructor:
// It takes a function onDeviceFound as a parameter.
// This function will be called when a new Bluetooth device is found.
// This function takes a BluetoothDevice object as its parameter and
// doesn't return anything (indicated by Unit).

//onReceive Method:
// This method is called by the Android system when a broadcast is received.
// It checks if the received broadcast's action is BluetoothDevice.ACTION_FOUND, which
// indicates that a new Bluetooth device has been discovered.

//Retrieving the Device:
// It extracts the BluetoothDevice object from the intent using getParcelableExtra.
// The code handles API level differences for retrieving the device object:
//    For Android versions Tiramisu (API 33) and above, it
//    explicitly provides the type (BluetoothDevice::class.java) for type safety.
// For older Android versions, it relies on the system to infer the type.

//Invoking the Callback:
// If a BluetoothDevice is successfully extracted (not null), it calls the
// onDeviceFound function, passing the discovered device as an argument.
// The ?.let block ensures that onDeviceFound is only called if device is not null.

//In essence, this code listens for Bluetooth device discovery events and,
// when a device is found, it executes the provided onDeviceFound function,
// giving you a way to react to the discovery of new Bluetooth devices in your application.

//Typical Usage:
// You would register this BroadcastReceiver in your Android application to
// be notified when new Bluetooth devices are discovered during a device scan.
// The onDeviceFound function would then contain the logic for how your
// app should handle the newly discovered device (e.g., displaying it in a list,
// attempting to connect, etc.).