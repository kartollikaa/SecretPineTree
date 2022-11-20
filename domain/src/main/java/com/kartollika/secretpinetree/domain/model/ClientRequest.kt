package com.kartollika.secretpinetree.domain.model

sealed class ClientRequest(val command: String) {
  data class LoadMore(val offset: Int) : ClientRequest("load_more")
  class SendMessage(val message: Message): ClientRequest("send_message")
}