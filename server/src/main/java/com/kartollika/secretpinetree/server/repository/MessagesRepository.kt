package com.kartollika.secretpinetree.server.repository

import com.kartollika.secretpinetree.domain.model.Message
import com.kartollika.secretpinetree.server.Dispatcher
import com.kartollika.secretpinetree.server.data.dao.MessagesDao
import com.kartollika.secretpinetree.server.data.entity.MessageEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface MessagesRepository {
  suspend fun sendMessage(message: Message)
  fun getMessages(count: Int = 50, offset: Int = 0): Flow<List<Message>>
  fun getLastMessage(): Flow<Message?>
}

class MessagesRepositoryImpl @Inject constructor(
  private val dao: MessagesDao,
  private val dispatcher: Dispatcher
) : MessagesRepository {

  override suspend fun sendMessage(message: Message) = withContext(dispatcher.io) {
    dao.insertMessage(MessageEntity(author = message.author, message = message.message))
  }

  override fun getMessages(count: Int, offset: Int) = dao.getMessages(count, offset).map {
    it.map { Message(it.id, message = it.message, author = it.author) }
  }

  @Suppress("SENSELESS_COMPARISON")
  override fun getLastMessage(): Flow<Message?> =
    dao.getLastMessage().map {
      if (it == null) return@map null
      Message(it.id, message = it.message, author = it.author)
    }
}