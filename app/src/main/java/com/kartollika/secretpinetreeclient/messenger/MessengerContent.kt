package com.kartollika.secretpinetreeclient.messenger

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.AlertDialog
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kartollika.secretpinetreeclient.Message
import com.kartollika.secretpinetreeclient.messenger.LookingForPine.Connected
import com.kartollika.secretpinetreeclient.messenger.LookingForPine.Found
import com.kartollika.secretpinetreeclient.messenger.MessagingUiState.Loading
import com.kartollika.secretpinetreeclient.messenger.MessagingUiState.Messenger
import kotlinx.coroutines.launch

@Composable fun MessengerScreen(
  modifier: Modifier = Modifier,
  state: MessagingUiState,
  lookingForPineState: LookingForPine,
  startDiscovery: () -> Unit,
  stopDiscovery: () -> Unit,
  onConnectToEndpoint: (String) -> Unit,
  onDismissConnectionDialog: () -> Unit,
  onSendClick: (String) -> Unit
) {
  if (lookingForPineState !is Connected) {
    LookingPine(
      modifier = Modifier.fillMaxSize(),
      state = lookingForPineState,
      startDiscovery = startDiscovery,
      stopDiscovery = stopDiscovery,
      onConnectToEndpoint = onConnectToEndpoint,
      onDismissConnectionDialog = onDismissConnectionDialog
    )
  } else {
    when (state) {
      Loading -> {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          CircularProgressIndicator()
        }
      }
      is Messenger -> {
        MessengerContent(state, modifier, onSendClick)
      }
    }
  }

}

@Composable fun LookingPine(
  state: LookingForPine,
  modifier: Modifier = Modifier,
  startDiscovery: () -> Unit = {},
  stopDiscovery: () -> Unit = {},
  onConnectToEndpoint: (String) -> Unit = {},
  onDismissConnectionDialog: () -> Unit = {}
) {
  DisposableEffect(Unit) {
    if (state is LookingForPine.Loading) {
      startDiscovery()
    }

    onDispose { stopDiscovery() }
  }

  Column(
    modifier = modifier,
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(text = "Ищем ближайшую сосну. Подойдите к ней")
    Spacer(modifier = Modifier.height(16.dp))
    CircularProgressIndicator()

    if (state is Found) {
      PineConnectDialog(
        state,
        onDismiss = onDismissConnectionDialog,
        onConfirm = { onConnectToEndpoint(state.endpointId) }
      )
    }
  }
}

@Composable private fun PineConnectDialog(
  state: Found,
  onDismiss: () -> Unit,
  onConfirm: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(text = "Нашли сосну ${state.discoveredEndpointName}!")
    }, text = {
      Text(text = "Чтобы присоединиться к чату и начать общение около сосны, нажмите \"Подключиться\"")
    }, dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(text = "Отмена")
      }
    }, confirmButton = {
      TextButton(onClick = {
        onConfirm()
        onDismiss()
      }) {
        Text(text = "Подключиться")
      }
    })
}

@Composable fun MessengerContent(
  state: Messenger,
  modifier: Modifier,
  onSendClick: (String) -> Unit
) {
  val scrollState = rememberLazyListState()

  Column(modifier = modifier.imePadding()) {
    Messages(state.messages, modifier = Modifier.weight(1f), scrollState = scrollState)
    UserInput(
      modifier = Modifier.fillMaxWidth(),
      onSendClick = onSendClick
    )
  }
}

@Composable fun UserInput(
  modifier: Modifier = Modifier,
  onSendClick: (String) -> Unit = { }
) {
  var textState by rememberSaveable(stateSaver = TextFieldValue.Saver) {
    mutableStateOf(TextFieldValue())
  }

  Row(
    modifier = modifier
      .height(48.dp)
      .background(color = Color.DarkGray)
      .padding(horizontal = 16.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {

    BasicTextField(
      modifier = Modifier.weight(1f),
      value = textState,
      onValueChange = { textState = it },
      maxLines = 1,
      singleLine = true
    )

    SendButton(
      textState = textState,
      onSendClick = {
        onSendClick(it)
        textState = TextFieldValue()
      }
    )
  }
}

@Composable
private fun SendButton(
  onSendClick: (String) -> Unit,
  textState: TextFieldValue
) {
  IconButton(onClick = { onSendClick(textState.text) }) {
    Icon(imageVector = Icons.Default.Send, contentDescription = "Send")
  }
}

@Composable fun Messages(
  messages: List<Message>,
  scrollState: LazyListState,
  modifier: Modifier = Modifier,
) {
  val scope = rememberCoroutineScope()
  Box(
    modifier = modifier
  ) {
    LazyColumn(
      reverseLayout = true,
      state = scrollState,
      contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
      modifier = Modifier.fillMaxSize()
    ) {
      for (index in messages.indices) {
        val content = messages[index]

        item {
          MessageView(
            message = content, isUserMe = content.author == "me"
          )

          Spacer(Modifier.height(16.dp))
        }
      }
    }
    // Jump to bottom button shows up when user scrolls past a threshold.
    // Convert to pixels:
    val jumpThreshold = with(LocalDensity.current) {
      JumpToBottomThreshold.toPx()
    }

    // Show the button if the first visible item is not the first one or if the offset is
    // greater than the threshold.
    val jumpToBottomButtonEnabled by remember {
      derivedStateOf {
        scrollState.firstVisibleItemIndex != 0 || scrollState.firstVisibleItemScrollOffset > jumpThreshold
      }
    }

    JumpToBottom(
      // Only show if the scroller is not at the bottom
      enabled = jumpToBottomButtonEnabled, onClicked = {
        scope.launch {
          scrollState.animateScrollToItem(0)
        }
      }, modifier = Modifier.align(Alignment.BottomCenter)
    )
  }
}

@Composable fun MessageView(
  message: Message,
  isUserMe: Boolean,
) {
  Column(
    modifier = Modifier
      .background(
        color = MaterialTheme.colors.secondary,
        shape = RoundedCornerShape(12.dp)
      )
      .padding(8.dp)
  ) {
    ProvideTextStyle(value = MaterialTheme.typography.overline.copy(color = MaterialTheme.colors.primary)) {
      Text(text = message.author)
    }

    ProvideTextStyle(value = MaterialTheme.typography.body1.copy(color = MaterialTheme.colors.onSecondary)) {
      Text(text = message.message)
    }
  }
}

@Preview @Composable fun MessagePreview() {
  MessageView(
    message = Message(id = 0, author = "Dmitrii", message = "Hi!"), isUserMe = false
  )
}

private val JumpToBottomThreshold = 56.dp
