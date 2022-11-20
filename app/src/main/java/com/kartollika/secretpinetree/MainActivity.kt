package com.kartollika.secretpinetree

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.ACCESS_WIFI_STATE
import android.Manifest.permission.BLUETOOTH
import android.Manifest.permission.BLUETOOTH_ADMIN
import android.Manifest.permission.BLUETOOTH_ADVERTISE
import android.Manifest.permission.BLUETOOTH_CONNECT
import android.Manifest.permission.BLUETOOTH_SCAN
import android.Manifest.permission.CHANGE_WIFI_STATE
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.AdvertisingOptions.Builder
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy
import com.kartollika.secretpinetree.client.ClientActivity
import com.kartollika.secretpinetree.databinding.ActivityMainBinding
import com.kartollika.secretpinetree.server.MainActivityViewModel
import com.kartollika.secretpinetree.server.repository.MessagesRepository
import com.permissionx.guolindev.PermissionX
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

  private lateinit var viewBinding: ActivityMainBinding

  private val viewModel: MainActivityViewModel by viewModels()

  private var pendingAction: (() -> Unit)? = null

  private val bluetoothEnableLauncher = registerForActivityResult(StartActivityForResult()) {
    if (it.resultCode == Activity.RESULT_OK) {
      pendingAction?.invoke()
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    viewBinding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(viewBinding.root)

    viewBinding.startServer.setOnClickListener {
      startServer()
    }

    viewBinding.stopServer.setOnClickListener {
      stopServer()
    }

    viewBinding.startClient.setOnClickListener {
      startActivity(Intent(this, ClientActivity::class.java))
    }
  }

  private fun stopServer() {
    val connectionsClient = Nearby.getConnectionsClient(this)
    connectionsClient.stopAdvertising()
    connectionsClient.stopAllEndpoints()
    onServerStopped()
  }

  private fun startServer() {
    askPermissions {
      enableBluetooth {
        startAdvertising()
      }
    }
  }

  private fun askPermissions(action: () -> Unit) {
    PermissionX.init(this)
      .permissions(getRequiredPermissions())
      .request { allGranted, _, _ ->
        if (allGranted) {
          action()
        }
      }
  }

  private fun enableBluetooth(action: () -> Unit) {
    val bluetoothAdapter: BluetoothAdapter = (getSystemService(BLUETOOTH_SERVICE) as BluetoothManager).adapter
    if (!bluetoothAdapter.isEnabled) {
      pendingAction = action
      bluetoothEnableLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
      return
    }
    action()
  }

  private fun startAdvertising() {
    viewBinding.startServer.isEnabled = false
    val advertisingOptions = Builder().setStrategy(Strategy.P2P_STAR).build()
    Nearby.getConnectionsClient(this)
      .startAdvertising(
        "Общество любителей сосн", "NearbyService", object : ConnectionLifecycleCallback() {
          override fun onConnectionInitiated(endpoint: String, p1: ConnectionInfo) {
            log("onConnectionInitiated")

            Thread {
              Nearby.getConnectionsClient(this@MainActivity)
                .acceptConnection(endpoint, object : PayloadCallback() {
                  override fun onPayloadReceived(endpointId: String, payload: Payload) {
                    val receivedPayload = payload.asBytes()?.let { String(it) }
                    log("Payload received $receivedPayload")

                    viewModel.onPayloadReceived(endpointId, payload)
                  }

                  override fun onPayloadTransferUpdate(p0: String, p1: PayloadTransferUpdate) {
                  }
                })
            }.start()
          }

          override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            when (result.status.statusCode) {
              ConnectionsStatusCodes.STATUS_OK -> {
                log("Connected")
                viewModel.addConnection(endpointId)
              }
              ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                log("Rejected")
              }
              ConnectionsStatusCodes.STATUS_ERROR -> {
                log("Error")
              }
            }
          }

          override fun onDisconnected(endpoint: String) {
            log("disconnected")
            viewModel.closeConnection(endpoint)
          }
        }, advertisingOptions
      )
      .addOnSuccessListener { unused: Void? ->
        onServerStarted()
      }
      .addOnFailureListener { e: Exception? -> onServerStopped() }
  }

  private fun onServerStarted() {
    viewBinding.startServer.isEnabled = true
    viewBinding.startClient.isEnabled = false
    switchServerStartVisibility(true)
  }

  private fun onServerStopped() {
    viewBinding.startServer.isEnabled = true
    viewBinding.startClient.isEnabled = true
    switchServerStartVisibility(false)
  }

  private fun switchServerStartVisibility(serverStarted: Boolean) {
    viewBinding.stopServer.isVisible = serverStarted
    viewBinding.startServer.isVisible = !serverStarted
  }

  override fun onDestroy() {
    super.onDestroy()
    stopAdvertising()
  }

  private fun stopAdvertising() {
    Nearby.getConnectionsClient(this).stopAdvertising()
  }

  companion object {

    private fun getRequiredPermissions(): List<String> {
      val newAndroidRequired = if (VERSION.SDK_INT >= VERSION_CODES.S) {
        listOf(
          BLUETOOTH_ADVERTISE,
          BLUETOOTH_CONNECT,
          BLUETOOTH_SCAN
        )
      } else {
        emptyList()
      }.toTypedArray()

      return listOf(
        BLUETOOTH,
        BLUETOOTH_ADMIN,
        ACCESS_WIFI_STATE,
        CHANGE_WIFI_STATE,
        ACCESS_COARSE_LOCATION,
        ACCESS_FINE_LOCATION,
        READ_EXTERNAL_STORAGE,
        *newAndroidRequired
      )
    }
  }

  private fun log(message: String) {
    Log.d("NEARBY", message)
  }
}