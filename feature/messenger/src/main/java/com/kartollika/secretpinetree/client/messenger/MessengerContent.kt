package com.kartollika.secretpinetree.client.messenger

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Colors
import androidx.compose.material.LocalContentColor
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.primarySurface
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.kartollika.secretpinetree.client.messenger.LookingForPine.Connected
import com.kartollika.secretpinetree.client.messenger.LookingForPine.Connecting
import com.kartollika.secretpinetree.client.messenger.LookingForPine.Found
import com.kartollika.secretpinetree.client.messenger.MessagingUiState.Loading
import com.kartollika.secretpinetree.client.messenger.MessagingUiState.Messenger
import com.kartollika.secretpinetree.client.messenger.vo.MessageVO
import com.kartollika.secretpinetree.client.ui_kit.theme.BlueGrey30
import com.kartollika.secretpinetree.client.ui_kit.theme.BlueGrey90
import com.kartollika.secretpinetree.client.ui_kit.theme.SecretPineTreeClientTheme
import kotlinx.coroutines.launch

@Composable fun MessengerScreen(
  modifier: Modifier = Modifier,
  state: MessagingUiState,
  lookingForPineState: LookingForPine,
  startDiscovery: () -> Unit,
  stopDiscovery: () -> Unit,
  onConnectToEndpoint: (String) -> Unit,
  onDismissConnectionDialog: () -> Unit,
  onSendClick: (String) -> Unit,
  onNameSaved: (String) -> Unit,
  onLoadMore: () -> Unit
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
        MessengerContent(
          state, modifier, onSendClick, onNameSaved, onLoadMore
        )
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
  DisposableEffect(state) {
    if (state is LookingForPine.Loading) {
      startDiscovery()
    }

    onDispose {
      stopDiscovery()
    }
  }

  Column(
    modifier = modifier.padding(horizontal = 64.dp),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {

    ProvideTextStyle(value = LocalTextStyle.current.copy(textAlign = TextAlign.Center)) {
      when (state) {
        is Connecting -> {
          CircularProgressIndicator()
          Spacer(modifier = Modifier.height(8.dp))
          Text(text = "Подключаемся...")
        }

        is LookingForPine.Loading, is Found -> {
          CircularProgressIndicator()
          Spacer(modifier = Modifier.height(16.dp))
          Text(text = "Ищем ближайшую сосну. Подойдите к ней")
        }
        else -> {}
      }
    }


    if (state is Found) {
      PineConnectDialog(state,
        onDismiss = onDismissConnectionDialog,
        onConfirm = { onConnectToEndpoint(state.endpointId) })
    }
  }
}

@Composable private fun PineConnectDialog(
  state: Found, onDismiss: () -> Unit, onConfirm: () -> Unit
) {
  AlertDialog(onDismissRequest = onDismiss, title = {
    Text(text = "Нашли сосну \"${state.discoveredEndpointName}\"!")
  }, text = {
    Text(text = "Чтобы присоединиться к чату и начать общение около сосны, нажмите \"Подключиться\"")
  }, dismissButton = {
    TextButton(onClick = onDismiss) {
      Text(text = "Отмена")
    }
  }, confirmButton = {
    TextButton(onClick = {
      onConfirm()
    }) {
      Text(text = "Подключиться")
    }
  })
}

@Composable fun MessengerContent(
  state: Messenger,
  modifier: Modifier,
  onSendClick: (String) -> Unit,
  onNameSaved: (String) -> Unit,
  onLoadMore: () -> Unit
) {
  val scrollState = rememberLazyListState()

  // Remember a SystemUiController
  val systemUiController = rememberSystemUiController()
  val colors = MaterialTheme.colors
  DisposableEffect(systemUiController) {
    // Update all of the system bar colors to be transparent, and use
    // dark icons if we're in light theme
    systemUiController.setSystemBarsColor(color = colors.primarySurface)
    systemUiController.setNavigationBarColor(color = colors.surface)

    onDispose {
      systemUiController.setNavigationBarColor(color = Color.Transparent)
      systemUiController.setSystemBarsColor(color = Color.Transparent)
    }
  }

  Scaffold(
    modifier = modifier,
    topBar = {
      TopAppBar(
        title = {
          Text(text = state.serverName)
        },
      )
    },
  ) {
    Box(modifier = Modifier.fillMaxSize()) {
      Column(modifier = Modifier.fillMaxSize()) {
        Messages(
          state, modifier = Modifier.weight(1f), scrollState = scrollState, onLoadMore = onLoadMore
        )
        UserInput(
          modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding(),
          onSendClick = onSendClick
        )
      }

      IntroduceYourselfDialog(
        state = state, onNameSaved = onNameSaved
      )
    }
  }
}

@Composable fun IntroduceYourselfDialog(
  state: Messenger,
  onNameSaved: (String) -> Unit = {},
) {
  if (state.name.isEmpty()) {
    var nameInput by remember {
      mutableStateOf(TextFieldValue(state.name))
    }

    AlertDialog(
      shape = RoundedCornerShape(16.dp),
      properties = DialogProperties(
        dismissOnBackPress = false, dismissOnClickOutside = false
      ),
      title = {
        Text(text = "Как вас звать?")
      },
      onDismissRequest = {},
      text = {
        OutlinedTextField(value = nameInput, onValueChange = {
          nameInput = it
        })
      },
      confirmButton = {
        TextButton(enabled = nameInput.text.isNotEmpty(), onClick = {
          onNameSaved(nameInput.text)
        }) {
          Text(text = "Сохранить")
        }
      },
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

  Surface(
    elevation = 8.dp
  ) {
    Row(
      modifier = modifier
        .height(56.dp)
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {

      UserInputText(
        textState = textState,
        onTextChanged = { textState = it },
        modifier = Modifier.weight(1f)
      )

      SendButton(enabled = textState.text.isNotEmpty(), textState = textState, onSendClick = {
        onSendClick(it)
        textState = TextFieldValue()
      })
    }
  }
}

@Composable private fun UserInputText(
  onTextChanged: (TextFieldValue) -> Unit,
  textState: TextFieldValue,
  modifier: Modifier = Modifier,
) {
  var lastFocusState by remember { mutableStateOf(false) }

  Box(modifier = modifier, contentAlignment = Alignment.CenterStart) {
    BasicTextField(
      modifier = Modifier
        .fillMaxWidth()
        .align(Alignment.CenterStart)
        .matchParentSize()
        .padding(end = 16.dp)
        .onFocusChanged { state ->
          lastFocusState = state.isFocused
        },
      value = textState,
      onValueChange = { onTextChanged(it) },
      maxLines = 1,
      singleLine = true,
      textStyle = LocalTextStyle.current.copy(color = LocalContentColor.current),
      cursorBrush = SolidColor(LocalContentColor.current)
    )

    if (textState.text.isEmpty() && !lastFocusState) {
      Text(
        text = "Write message here",
        style = MaterialTheme.typography.body1.copy(
          color = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
        )
      )
    }
  }
}

@Composable private fun SendButton(
  onSendClick: (String) -> Unit, textState: TextFieldValue, enabled: Boolean
) {
  val disabledContentColor = MaterialTheme.colors.onSurface.copy(alpha = 0.3f)

  val buttonColors = ButtonDefaults.buttonColors(
    disabledBackgroundColor = Color.Transparent, disabledContentColor = disabledContentColor
  )

  val border = if (!enabled) {
    BorderStroke(
      width = 1.dp, color = MaterialTheme.colors.onSurface.copy(alpha = 0.3f)
    )
  } else {
    null
  }

  Button(enabled = enabled,
    colors = buttonColors,
    border = border,
    contentPadding = PaddingValues(0.dp),
    shape = CircleShape,
    onClick = {
      onSendClick(textState.text)
    }) {
    Text(
      "Send", modifier = Modifier.padding(horizontal = 16.dp)
    )
  }
}

@Composable fun Messages(
  state: Messenger,
  scrollState: LazyListState,
  modifier: Modifier = Modifier,
  onLoadMore: () -> Unit
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
      for (index in state.messages.indices) {
        val content = state.messages[index]

        item {
          MessageView(
            message = content, isUserMe = content.author == state.name
          )

          Spacer(Modifier.height(16.dp))
        }
      }

      item {
        LoadMore(onLoadMore)
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
      }, modifier = Modifier
        .align(Alignment.BottomEnd)
        .offset(x = (-32).dp)
    )
  }
}

@Composable fun LoadMore(
  onLoadMore: () -> Unit
) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .padding(top = 16.dp),
    contentAlignment = Alignment.Center
  ) {
    OutlinedButton(
      onClick = onLoadMore,
      shape = RoundedCornerShape(16.dp),
    ) {
      Text(text = "Загрузить еще")
    }
  }
}

@Composable fun MessageView(
  message: MessageVO,
  isUserMe: Boolean,
) {

  val backgroundBubbleColor = if (isUserMe) {
    MaterialTheme.colors.primary
  } else {
    MaterialTheme.colors.messageBackgroundColor(isUserMe = isUserMe)
  }

  Column {
    ProvideTextStyle(value = MaterialTheme.typography.subtitle1.copy(color = MaterialTheme.colors.primary)) {
      Text(text = message.author)
    }
    Spacer(modifier = Modifier.height(4.dp))
    MessageBubble(backgroundBubbleColor, message)
  }
}

@Composable
private fun MessageBubble(
  backgroundBubbleColor: Color,
  message: MessageVO
) {
  Surface(
    color = backgroundBubbleColor, shape = RoundedCornerShape(
      topStart = 4.dp, topEnd = 20.dp, bottomEnd = 20.dp, bottomStart = 20.dp
    )
  ) {

    ProvideTextStyle(
      value = MaterialTheme.typography.body1.copy(
        color = LocalContentColor.current,
      )
    ) {
      Text(
        modifier = Modifier.padding(16.dp),
        text = message.message
      )
    }
  }
}

@Composable fun Colors.messageBackgroundColor(isUserMe: Boolean): Color {
  return if (isUserMe) {
    primary
  } else {
    if (isLight) {
      BlueGrey90
    } else {
      BlueGrey30
    }
  }
}

@Preview @Composable fun MessagePreview() {
  MessageView(
    message = MessageVO(author = "Dmitrii", message = "Hi!"), isUserMe = false
  )
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun UserInputPreview() {
  SecretPineTreeClientTheme {
    UserInput()
  }
}

private val JumpToBottomThreshold = 56.dp
