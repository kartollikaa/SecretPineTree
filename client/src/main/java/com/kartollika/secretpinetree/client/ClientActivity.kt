package com.kartollika.secretpinetree.client

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.kartollika.secretpinetree.client.messenger.MessengerScreen
import com.kartollika.secretpinetree.client.messenger.MessengerViewModel
import com.kartollika.secretpinetree.client.permission.PermissionRationale
import com.kartollika.secretpinetree.client.permission.getRequiredPermissions
import com.kartollika.secretpinetree.client.ui_kit.theme.SecretPineTreeClientTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ClientActivity : ComponentActivity() {

  @OptIn(ExperimentalPermissionsApi::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    WindowCompat.setDecorFitsSystemWindows(window, false)

    setContent {
      SecretPineTreeClientTheme {

        val systemUiController = rememberSystemUiController()
        val useDarkIcons = !isSystemInDarkTheme()
        DisposableEffect(systemUiController, useDarkIcons) {
          // Update all of the system bar colors to be transparent, and use
          // dark icons if we're in light theme
          systemUiController.setSystemBarsColor(darkIcons = useDarkIcons, color = Color.Transparent)

          onDispose {
          }
        }

        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colors.background
        ) {
          val permissionsLauncher =
            rememberMultiplePermissionsState(permissions = getRequiredPermissions())

          val viewModel: MessengerViewModel = hiltViewModel()
          val locationEnabled = viewModel.locationEnabled()
          var locationEnabledState = remember { mutableStateOf(locationEnabled && permissionsLauncher.allPermissionsGranted) }

          if (locationEnabledState.value) {
            val state by viewModel.uiState.collectAsState()
            val lookingForPineState by viewModel.lookingForPineState.collectAsState()

            MessengerScreen(
              modifier = Modifier.statusBarsPadding(),
              state = state,
              lookingForPineState = lookingForPineState,
              startDiscovery = {
                viewModel.startDiscovery()
              },
              stopDiscovery = {
                viewModel.stopDiscovery()
              },
              onConnectToEndpoint = {
                viewModel.connect(it)
              },
              onDismissConnectionDialog = {
                viewModel.dismissConnectionDialog()
              },
              onSendClick = { message ->
                viewModel.sendMessage(message)
              },
              onNameSaved = { name ->
                viewModel.saveName(name)
              },
              onLoadMore = {
                viewModel.loadMore()
              }
            )
          } else {
            PermissionRationale(
              modifier = Modifier.fillMaxSize(),
              locationEnabled = viewModel::locationEnabled,
              locationEnabledState = locationEnabledState
            )
          }
        }
      }
    }
  }
}