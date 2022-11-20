package com.kartollika.secretpinetree.domain.repository

import com.kartollika.secretpinetree.domain.model.Message
import kotlinx.coroutines.flow.Flow

interface MessengerRepository {
  fun getName(): Flow<String>
  suspend fun saveName(name: String)
  fun getMessages(): Flow<List<Message>>
  fun putMessages(messages: List<Message>)
}