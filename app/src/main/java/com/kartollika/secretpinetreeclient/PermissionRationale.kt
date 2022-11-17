package com.kartollika.secretpinetreeclient

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionRationale(
  permissionLauncher: MultiplePermissionsState,
  modifier: Modifier = Modifier
) {
  ProvideTextStyle(value = LocalTextStyle.current.copy(textAlign = TextAlign.Center)) {
    Column(
      modifier = modifier
        .fillMaxWidth()
        .padding(horizontal = 32.dp),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(text = "Для общения около сосны необходимо предоставить несколько разрешений")
      Spacer(modifier = Modifier.height(16.dp))
      Button(onClick = { permissionLauncher.launchMultiplePermissionRequest() }) {
        Text(text = "Дать разрешения")
      }
    }
  }
}