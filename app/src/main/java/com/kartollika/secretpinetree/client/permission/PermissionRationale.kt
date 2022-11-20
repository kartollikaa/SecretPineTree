package com.kartollika.secretpinetree.client.permission

import android.content.Intent
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
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
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.kartollika.secretpinetree.client.R.string

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionRationale(
  permissionLauncher: MultiplePermissionsState,
  locationEnabled: () -> Boolean,
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
      Text(text = stringResource(string.permissions_rationale))
      Spacer(modifier = Modifier.height(16.dp))
      Button(onClick = { permissionLauncher.launchMultiplePermissionRequest() }) {
        Text(text = stringResource(string.permissions_rationale_give_permissions))
      }
    }
  }
}

@OptIn(ExperimentalPermissionsApi::class)
private fun actionIfEnabledLocation(
  state: MutableState<(() -> Unit)?>,
  permissionsState: MultiplePermissionsState,
  locationEnabled: () -> Boolean,
  locationEnableLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>,
  action: () -> Unit
) {
  when {
    !permissionsState.allPermissionsGranted -> {
      permissionsState.launchMultiplePermissionRequest()
      state.value = {
        actionIfEnabledLocation(
          state, permissionsState, locationEnabled, locationEnableLauncher, action
        )
      }
    }

    !locationEnabled() -> {
      locationEnableLauncher.launch(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
      state.value = {
        actionIfEnabledLocation(
          state, permissionsState, locationEnabled, locationEnableLauncher, action
        )
      }
    }

    else -> {
      action()
      state.value = null
    }
  }
}