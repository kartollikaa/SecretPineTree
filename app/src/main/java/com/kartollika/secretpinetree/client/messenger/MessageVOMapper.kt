package com.kartollika.secretpinetree.client.messenger

import com.kartollika.secretpinetree.client.domain.model.Message
import javax.inject.Inject

class MessageVOMapper @Inject constructor() {
  fun mapMessageToVO(message: Message) = MessageVO(
    author = message.author,
    message = message.message
  )
}