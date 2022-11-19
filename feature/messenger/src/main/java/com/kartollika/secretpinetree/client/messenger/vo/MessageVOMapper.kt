package com.kartollika.secretpinetree.client.messenger.vo

import com.kartollika.secretpinetree.client.domain.model.Message

class MessageVOMapper {
  fun mapMessageToVO(message: Message) = MessageVO(
    author = message.author,
    message = message.message
  )
}