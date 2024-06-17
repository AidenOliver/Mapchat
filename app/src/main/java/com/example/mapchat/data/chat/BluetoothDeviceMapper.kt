package com.example.mapchat.data.chat

import android.bluetooth.BluetoothDevice
import com.example.mapchat.domain.chat.BluetoothDeviceDomain

fun BluetoothDevice.toBluetoothDeviceDomain(): BluetoothDeviceDomain {
    return BluetoothDeviceDomain(
        name = name,
        address = address
    )
}