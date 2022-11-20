package com.kartollika.secretpinetree.client.messenger.vo

import com.kartollika.secretpinetree.domain.model.Message

class MessageVOMapper {
  fun mapMessageToVO(message: Message) = MessageVO(
    author = message.author,
    message = message.message
  )
}