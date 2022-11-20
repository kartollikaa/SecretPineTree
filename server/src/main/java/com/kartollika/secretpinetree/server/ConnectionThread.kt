package com.kartollika.secretpinetree.server

import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.Payload
import com.google.gson.Gson
import com.kartollika.secretpinetree.domain.model.Message
import com.kartollika.secretpinetree.server.repository.MessagesRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class ConnectionThread(
  private val repository: MessagesRepository,
  private val dispatcher: Dispatcher,
  private val endpointId: String,
  private val connectionsClient: ConnectionsClient,
  private val gson: Gson
): Thread() {

  private lateinit var job: Job

  override fun run() {
    super.run()

    dispatcher.scope.launch {
      val messages = repository.getMessages().first()
      sendMessages(messages)
    }

    job = repository.getLastMessage()
      .drop(1)
      .onEach { message -> if (message != null) sendMessages(listOf(message)) }
      .launchIn(dispatcher.scope)

    while (true) {
      if (interrupted()) {
        job.cancel()
        break
      }
    }
  }

  private fun sendMessages(messages: List<Message>) {
    val bytePayload = gson.toJson(messages).toByteArray()
    connectionsClient.sendPayload(endpointId, Payload.fromBytes(bytePayload))
  }
}