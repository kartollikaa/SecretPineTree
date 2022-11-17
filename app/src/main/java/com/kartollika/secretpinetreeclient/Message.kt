package com.kartollika.secretpinetreeclient

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Message(
  @SerialName("id")
  val id: Int,
  @SerialName("author")
  val author: String,
  @SerialName("message")
  val message: String
)