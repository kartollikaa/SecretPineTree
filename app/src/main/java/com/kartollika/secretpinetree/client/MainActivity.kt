package com.kartollika.secretpinetree.client

import android.Manifest.permission
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.kartollika.secretpinetree.client.messenger.MessengerScreen
import com.kartollika.secretpinetree.client.messenger.MessengerViewModel
import com.kartollika.secretpinetree.client.theme.SecretPineTreeClientTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

  @OptIn(ExperimentalPermissionsApi::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    WindowCompat.setDecorFitsSystemWindows(window, false)

    setContent {
      SecretPineTreeClientTheme {

        Surface(
          modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
          color = MaterialTheme.colors.background
        ) {
          val permissionsLauncher =
            rememberMultiplePermissionsState(permissions = getRequiredPermissions())

          if (permissionsLauncher.allPermissionsGranted) {
            val viewModel: MessengerViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsState()
            val lookingForPineState by viewModel.lookingForPineState.collectAsState()

            MessengerScreen(
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
              permissionLauncher = permissionsLauncher
            )
          }
        }
      }
    }
  }

  companion object {
    private fun getRequiredPermissions(): List<String> {
      val newAndroidRequired = if (VERSION.SDK_INT >= VERSION_CODES.S) {
        listOf(
          permission.BLUETOOTH_ADVERTISE,
          permission.BLUETOOTH_CONNECT,
          permission.BLUETOOTH_SCAN
        )
      } else {
        emptyList()
      }.toTypedArray()

      return listOf(
        permission.BLUETOOTH,
        permission.BLUETOOTH_ADMIN,
        permission.ACCESS_WIFI_STATE,
        permission.CHANGE_WIFI_STATE,
        permission.ACCESS_COARSE_LOCATION,
        permission.ACCESS_FINE_LOCATION,
        permission.READ_EXTERNAL_STORAGE,
        *newAndroidRequired
      )
    }
  }
}