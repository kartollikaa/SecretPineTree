package com.kartollika.secretpinetree.client.domain.repository

import com.kartollika.secretpinetree.client.domain.model.Message
import kotlinx.coroutines.flow.Flow

interface MessengerRepository {
  fun getName(): Flow<String>
  suspend fun saveName(name: String)
  fun getMessages(): Flow<List<Message>>
  fun putMessages(messages: List<Message>)
}