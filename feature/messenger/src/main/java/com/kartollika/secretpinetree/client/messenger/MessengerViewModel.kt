package com.kartollika.secretpinetree.client.messenger

import android.location.LocationManager
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes.STATUS_ALREADY_CONNECTED_TO_ENDPOINT
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kartollika.secretpinetree.client.domain.datasource.LocationDataSource
import com.kartollika.secretpinetree.client.domain.model.ClientRequest
import com.kartollika.secretpinetree.client.domain.model.ClientRequest.SendMessage
import com.kartollika.secretpinetree.client.domain.model.Message
import com.kartollika.secretpinetree.client.domain.repository.MessengerRepository
import com.kartollika.secretpinetree.client.messenger.LookingForPine.Connected
import com.kartollika.secretpinetree.client.messenger.LookingForPine.Connecting
import com.kartollika.secretpinetree.client.messenger.LookingForPine.Found
import com.kartollika.secretpinetree.client.messenger.MessagingUiState.Loading
import com.kartollika.secretpinetree.client.messenger.MessagingUiState.Messenger
import com.kartollika.secretpinetree.client.messenger.vo.MessageVO
import com.kartollika.secretpinetree.client.messenger.vo.MessageVOMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MessengerViewModel @Inject constructor(
  private val messengerRepository: MessengerRepository,
  private val connectionsClient: ConnectionsClient,
  private val messageVOMapper: MessageVOMapper,
  private val gson: Gson,
  private val locationDataSource: LocationDataSource
) : ViewModel() {

  private var endpointId: String = ""
  private var endpointName: String = ""

  private val _lookingForPineState = MutableStateFlow<LookingForPine>(LookingForPine.Loading)
  val lookingForPineState = _lookingForPineState.asStateFlow()

  val uiState = combine(
    messengerRepository.getName(),
    messengerRepository.getMessages().map { it.map(messageVOMapper::mapMessageToVO) },
    lookingForPineState,
  ) { name, messages, lookingForPine ->
    val endpointName = if (lookingForPine is Connected) {
      lookingForPine.endpointName
    } else {
      endpointName
    }
    Messenger(name, messages, endpointName)
  }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(),
      initialValue = Loading
    )

  fun locationEnabled() = locationDataSource.isLocationEnabled()

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
    val endpointName = endpointName
    _lookingForPineState.tryEmit(Connecting)
    viewModelScope.launch(Dispatchers.Default) {
      connectionsClient
        .requestConnection("Android Phone", endpointId, connectionLifecycleCallback)
        .addOnSuccessListener { unused: Void? -> }
        .addOnFailureListener { e: Exception? ->
          if ((e as ApiException).statusCode == STATUS_ALREADY_CONNECTED_TO_ENDPOINT) {
            _lookingForPineState.tryEmit(Connected(endpointName))
            return@addOnFailureListener
          }
          _lookingForPineState.tryEmit(LookingForPine.Loading)
        }
    }
  }

  fun sendMessage(messageText: String) {
    val name = (uiState.value as Messenger).name
    val message = Message(0, name, messageText)

    val request = SendMessage(message)
    val payload = Payload.fromBytes(gson.toJson(request).toByteArray())
    connectionsClient.sendPayload(endpointId, payload)
  }

  fun stopDiscovery() {
    connectionsClient.stopDiscovery()
  }

  private fun onEndpointFound(endpointId: String, discoveredEndpointName: String) {
    _lookingForPineState.tryEmit(Found(endpointId, discoveredEndpointName))
    endpointName = discoveredEndpointName
  }

  fun dismissConnectionDialog() {
    _lookingForPineState.tryEmit(LookingForPine.Loading)
  }

  private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
    override fun onConnectionInitiated(endpointId: String, p1: ConnectionInfo) {
      connectionsClient
        .acceptConnection(endpointId, object : PayloadCallback() {
          override fun onPayloadReceived(p0: String, payload: Payload) {
            putMessagesLocal(payload)
          }

          override fun onPayloadTransferUpdate(p0: String, p1: PayloadTransferUpdate) {
          }
        })
    }

    override fun onConnectionResult(endpoint: String, result: ConnectionResolution) {
      Log.d("NEARBY", "connection result $result")

      if (result.status.statusCode == ConnectionsStatusCodes.STATUS_OK) {
        _lookingForPineState.tryEmit(Connected(endpointName))
        endpointId = endpoint
      }
    }

    override fun onDisconnected(p0: String) {
      _lookingForPineState.tryEmit(LookingForPine.Loading)
      endpointId = ""
    }
  }

  private fun putMessagesLocal(payload: Payload) {
    viewModelScope.launch(Dispatchers.Default) {
      val bytes = payload.asBytes() ?: return@launch
      val payloadString = String(bytes)

      val listMessages = object : TypeToken<List<Message>>() {}.type
      val messages = gson.fromJson<List<Message>>(payloadString, listMessages)
      messengerRepository.putMessages(messages)
    }
  }

  fun loadMore() {
    val state = (uiState.value as? Messenger)?.messages ?: return

    viewModelScope.launch(Dispatchers.Default) {
      val request = ClientRequest.LoadMore(offset = state.size)
      val payload = Payload.fromBytes(gson.toJson(request).toByteArray())
      connectionsClient.sendPayload(endpointId, payload)
    }
  }
}

sealed class MessagingUiState {
  object Loading : MessagingUiState()

  data class Messenger(
    val name: String,
    val messages: List<MessageVO>,
    val serverName: String
  ) : MessagingUiState()
}

sealed class LookingForPine {

  data class Connected(val endpointName: String): LookingForPine()

  data class Found(
    val endpointId: String = "",
    val discoveredEndpointName: String = ""
  ): LookingForPine()

  object Loading: LookingForPine()
  object Connecting: LookingForPine()

  /*var shouldShowPineConnectDialog by mutableStateOf(false)
    private set

  fun setShowPineConnectDialog(shouldShow: Boolean) {
    shouldShowPineConnectDialog = shouldShow
  }*/
}