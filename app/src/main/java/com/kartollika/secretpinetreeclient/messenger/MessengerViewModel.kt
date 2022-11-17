package com.kartollika.secretpinetreeclient.messenger

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy
import com.kartollika.secretpinetreeclient.Message
import com.kartollika.secretpinetreeclient.messenger.LookingForPine.Connected
import com.kartollika.secretpinetreeclient.messenger.MessagingUiState.Loading
import com.kartollika.secretpinetreeclient.messenger.MessagingUiState.Messenger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

@HiltViewModel
class MessengerViewModel @Inject constructor(
  private val messengerRepository: MessengerRepository,
  private val connectionsClient: ConnectionsClient
) : ViewModel() {

  private var endpointId: String = ""

  private val _lookingForPineState = MutableStateFlow<LookingForPine>(LookingForPine.Loading)
  val lookingForPineState = _lookingForPineState.asStateFlow()

  val uiState = combine(
    messengerRepository.getName(),
    messengerRepository.getMessages(),
  ) { name, messages ->
    MessagingUiState.Messenger(name, messages)
  }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(),
      initialValue = Loading
    )

  fun saveName(name: String) {
    viewModelScope.launch {
      messengerRepository.saveName(name)
    }
  }

  fun startDiscovery() {
    val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
      override fun onEndpointFound(endpointId: String, p1: DiscoveredEndpointInfo) {
        onEndpointFound(endpointId, p1.endpointName)
      }

      override fun onEndpointLost(p0: String) {
        _lookingForPineState.tryEmit(LookingForPine.Loading)
      }
    }

    val discoveryOptions = DiscoveryOptions.Builder().setStrategy(Strategy.P2P_STAR).build()
    connectionsClient
      .startDiscovery("NearbyService", endpointDiscoveryCallback, discoveryOptions)
      .addOnSuccessListener { unused: Void? -> }
      .addOnFailureListener { e: java.lang.Exception? -> }
  }

  fun connect(endpointId: String) {
    connectionsClient
      .requestConnection("Android Phone", endpointId, connectionLifecycleCallback)
      .addOnSuccessListener { unused: Void? -> }
      .addOnFailureListener { e: Exception? -> }
  }

  fun sendMessage(messageText: String) {
    val name = (uiState.value as Messenger).name
    val message = Message(0, name, messageText)
    val payload = Payload.fromBytes(Json.encodeToString(message).toByteArray())
    connectionsClient.sendPayload(endpointId, payload)
  }

  fun stopDiscovery() {
    connectionsClient.stopDiscovery()
  }

  private fun onEndpointFound(endpointId: String, discoveredEndpointName: String) {
    _lookingForPineState.tryEmit(LookingForPine.Found(endpointId, discoveredEndpointName))
  }

  fun dismissConnectionDialog() {
    _lookingForPineState.tryEmit(LookingForPine.Loading)
  }

  private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
    override fun onConnectionInitiated(endpointId: String, p1: ConnectionInfo) {
      connectionsClient
        .acceptConnection(endpointId, object : PayloadCallback() {
          override fun onPayloadReceived(p0: String, payload: Payload) {
            val messages = String(payload.asBytes()!!)
            Log.d("NEARBY", "connection result $messages")
            putMessagesLocal(messages)
          }

          override fun onPayloadTransferUpdate(p0: String, p1: PayloadTransferUpdate) {
          }
        });
    }

    override fun onConnectionResult(endpoint: String, result: ConnectionResolution) {
      Log.d("NEARBY", "connection result $result")

      if (result.status.statusCode == ConnectionsStatusCodes.STATUS_OK) {
        _lookingForPineState.tryEmit(Connected)
        endpointId = endpoint
      }
    }

    override fun onDisconnected(p0: String) {
    }
  }

  @OptIn(ExperimentalSerializationApi::class)
  private fun putMessagesLocal(messages: String) {
    val messages = Json.decodeFromString<List<Message>>(messages)
    messengerRepository.putMessages(messages)
  }
}

sealed class MessagingUiState {
  object Loading : MessagingUiState()

  data class Messenger(
    val name: String,
    val messages: List<Message>
  ) : MessagingUiState()
}

sealed class LookingForPine {

  object Connected: LookingForPine()

  data class Found(
    val endpointId: String = "",
    val discoveredEndpointName: String = ""
  ): LookingForPine()

  object Loading: LookingForPine()

  /*var shouldShowPineConnectDialog by mutableStateOf(false)
    private set

  fun setShowPineConnectDialog(shouldShow: Boolean) {
    shouldShowPineConnectDialog = shouldShow
  }*/
}