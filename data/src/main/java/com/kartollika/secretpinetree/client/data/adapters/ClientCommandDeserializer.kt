package com.kartollika.secretpinetree.server.adapters

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.kartollika.secretpinetree.client.domain.model.ClientRequest
import com.kartollika.secretpinetree.client.domain.model.ClientRequest.LoadMore
import com.kartollika.secretpinetree.client.domain.model.ClientRequest.SendMessage
import java.lang.reflect.Type

class ClientCommandDeserializer: JsonDeserializer<ClientRequest> {
  override fun deserialize(
    json: JsonElement,
    typeOfT: Type,
    context: JsonDeserializationContext
  ): ClientRequest {
    return when (json.asJsonObject["command"].asString) {
      "load_more" -> context.deserialize(json, LoadMore::class.java)
      "send_message" -> context.deserialize(json, SendMessage::class.java)
      else -> error("Unknown command")
    }
  }
}
