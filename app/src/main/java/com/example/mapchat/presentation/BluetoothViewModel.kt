package com.example.mapchat.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mapchat.domain.chat.BluetoothController
import com.example.mapchat.domain.chat.BluetoothDevice
import com.example.mapchat.domain.chat.BluetoothDeviceDomain
import com.example.mapchat.domain.chat.ConnectionResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BluetoothViewModel @Inject constructor(
    private val bluetoothController: BluetoothController
) : ViewModel() {

    private val _state = MutableStateFlow(BluetoothUiState()) // Read-write UI state
    val state = combine( // Combine mutable UI stateflow with BluetoothController stateflow
        bluetoothController.scannedDevices,
        bluetoothController.pairedDevices,
        _state
    ) {scannedDevices, pairedDevices, state ->
        state.copy(
            scannedDevices = scannedDevices,
            pairedDevices = pairedDevices,
            messages = if(state.isConnected) state.messages else emptyList()
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _state.value) // Read-only UI state

    private var deviceConnectionJob: Job? = null

    init { // Listen to bluetoothController events to update the UI state
        bluetoothController.isConnected.onEach { isConnected ->
            _state.update { it.copy(isConnected = isConnected) }
        }.launchIn(viewModelScope)

        bluetoothController.errors.onEach { error ->
            _state.update { it.copy(
                errorMessage = error
            ) }
        }.launchIn(viewModelScope)
    }

    fun connectToDevice(device: BluetoothDeviceDomain) { // Update the state, and start a job to connect to the device
        _state.update { it.copy(isConnecting = true) }
        deviceConnectionJob = bluetoothController
            .connectToDevice(device)
            .listen()
    }

    fun disconnectFromDevice() { // Cancel the device connection job, close the connection, and update the UI state
        deviceConnectionJob?.cancel()
        bluetoothController.closeConnection()
        _state.update { it.copy(
            isConnecting = false,
            isConnected = false
        ) }
    }

    fun waitForIncomingConnections() { // Update the state, and start a job to listen for incoming connections
        _state.update { it.copy(isConnecting = true) }
        deviceConnectionJob = bluetoothController
            .startBluetoothServer()
            .listen()
    }

    fun sendMessage(message: String) { // Launch a coroutine to send the message to the device, and update the UI state
        viewModelScope.launch {
            val bluetoothMessage = bluetoothController.trySendMessage(message)
            if (bluetoothMessage != null) {
                _state.update { it.copy(
                    messages = it.messages + bluetoothMessage
                ) }
            }
        }
    }

    fun startScan() {
        bluetoothController.startDiscovery()
    }

    fun stopScan() {
        bluetoothController.stopDiscovery()
    }

    private fun Flow<ConnectionResult>.listen(): Job { // Listen for connection events and update the UI state
        return onEach { result ->
            when(result) {
                ConnectionResult.ConnectionEstablished -> {
                    _state.update {it.copy(
                        isConnected = true,
                        isConnecting = false,
                        errorMessage = null
                    )}
                }
                is ConnectionResult.TransferSucceeded -> {
                    _state.update { it.copy(
                        messages = it.messages + result.message
                    ) }
                }
                is ConnectionResult.Error -> {
                    _state.update {it.copy(
                        isConnected = false,
                        isConnecting = false,
                        errorMessage = result.message
                    )}
                }
            }
        }
            .catch { throwable -> // Close the connection on exception
                bluetoothController.closeConnection()
                _state.update {it.copy(
                    isConnected = false,
                    isConnecting = false,
                )}
            }
            .launchIn(viewModelScope) // Launch the job in the ViewModel scope
    }

    override fun onCleared() { // Release the BluetoothController when the ViewModel is cleared
        super.onCleared()
        bluetoothController.release()
    }
}