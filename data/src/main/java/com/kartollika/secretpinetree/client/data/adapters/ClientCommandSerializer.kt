package com.kartollika.secretpinetree.client.data.adapters

import com.google.gson.JsonElement
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.kartollika.secretpinetree.client.domain.model.ClientRequest
import com.kartollika.secretpinetree.client.domain.model.ClientRequest.LoadMore
import com.kartollika.secretpinetree.client.domain.model.ClientRequest.SendMessage
import java.lang.reflect.Type

class ClientCommandSerializer: JsonSerializer<ClientRequest> {
  override fun serialize(
    src: ClientRequest,
    typeOfSrc: Type,
    context: JsonSerializationContext
  ): JsonElement {
    return when (src) {
      is LoadMore -> context.serialize(src, LoadMore::class.java)
      is SendMessage -> context.serialize(src, SendMessage::class.java)
    }
  }
}
