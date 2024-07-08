package com.example.mapchat.di

import android.content.Context
import com.example.mapchat.data.chat.AndroidBluetoothController
import com.example.mapchat.domain.chat.BluetoothController
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideBluetoothController(@ApplicationContext context: Context): BluetoothController {
        return AndroidBluetoothController(context)
    }
}

// Initialize AndroidBluetoothController instance and provide it to the BluetoothController interface
// BluetoothViewModel will use this instance to communicate with the Bluetooth device
