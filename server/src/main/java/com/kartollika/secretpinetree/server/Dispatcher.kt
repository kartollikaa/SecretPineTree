package com.kartollika.secretpinetree.server

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope

data class Dispatcher(
  val scope: CoroutineScope,
  val io: CoroutineDispatcher,
  val main: CoroutineDispatcher
)