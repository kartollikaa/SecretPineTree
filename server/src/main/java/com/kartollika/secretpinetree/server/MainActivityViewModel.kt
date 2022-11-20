package com.kartollika.secretpinetree.server

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.Payload
import com.google.gson.Gson
import com.kartollika.secretpinetree.domain.model.ClientRequest
import com.kartollika.secretpinetree.domain.model.ClientRequest.LoadMore
import com.kartollika.secretpinetree.domain.model.ClientRequest.SendMessage
import com.kartollika.secretpinetree.server.repository.MessagesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
  private val repository: MessagesRepository,
  private val dispatcher: Dispatcher,
  private val connectionsClient: ConnectionsClient,
  private val gson: Gson
) : ViewModel() {

  private val connectedEndpoints = HashMap<String, Endpoint>()
  private val threads = HashMap<String, Thread>()

  fun addConnection(endpointId: String) {
    connectedEndpoints[endpointId] = Endpoint(endpointId)

    val thread = ConnectionThread(
      repository,
      dispatcher,
      endpointId,
      connectionsClient,
      gson
    )

    threads[endpointId] = thread
    thread.start()
  }

  override fun onCleared() {
    super.onCleared()
    stopServer()
  }

  fun stopServer() {
    stopThreads()
    connectedEndpoints.clear()
  }

  private fun stopThreads() {
    threads.values.forEach { it.interrupt() }
    threads.clear()
  }

  fun closeConnection(endpointId: String) {
    connectedEndpoints.remove(endpointId)

    threads[endpointId]?.interrupt()
    threads.remove(endpointId)
  }

  fun onPayloadReceived(endpointId: String, payload: Payload) {
    val receivedPayload = payload.asBytes()?.let { String(it) }

    when (val clientCommand = gson.fromJson(receivedPayload, ClientRequest::class.java)) {
      is LoadMore -> {
        viewModelScope.launch(dispatcher.io) {
          val messages = repository.getMessages(offset = clientCommand.offset)
            .first()
          val bytePayload = gson.toJson(messages).toByteArray()
          connectionsClient.sendPayload(endpointId, Payload.fromBytes(bytePayload))
        }
      }

      is SendMessage -> {
        viewModelScope.launch {
          repository.sendMessage(clientCommand.message)
        }
      }
    }
  }

  data class Endpoint(
    val endpointId: String,
  )
}