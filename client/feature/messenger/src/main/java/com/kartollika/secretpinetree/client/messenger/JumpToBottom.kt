/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kartollika.secretpinetree.client.messenger

import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kartollika.secretpinetree.client.messenger.Visibility.GONE
import com.kartollika.secretpinetree.client.messenger.Visibility.VISIBLE

private enum class Visibility {
  VISIBLE,
  GONE
}

/**
 * Shows a button that lets the user scroll to the bottom.
 */
@Composable
fun JumpToBottom(
  enabled: Boolean,
  onClicked: () -> Unit,
  modifier: Modifier = Modifier
) {
  val transition = updateTransition(
    if (enabled) VISIBLE else GONE,
    label = "JumpToBottom visibility animation"
  )
  val bottomOffset by transition.animateDp(label = "JumpToBottom offset animation") {
    if (it == GONE) {
      (-32).dp
    } else {
      32.dp
    }
  }
  if (bottomOffset > 0.dp) {
    FloatingActionButton(
      onClick = onClicked,
      contentColor = MaterialTheme.colors.onPrimary,
      modifier = modifier
        .offset(y = -bottomOffset)
        .size(54.dp)
    ) {
      Icon(
        modifier = Modifier.size(56.dp).padding(16.dp),
        imageVector = Icons.Filled.ArrowDownward,
        contentDescription = null
      )
    }
  }
}

@Preview
@Composable
fun JumpToBottomPreview() {
  JumpToBottom(enabled = true, onClicked = {})
}
