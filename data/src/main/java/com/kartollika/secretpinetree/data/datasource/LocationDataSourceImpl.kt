package com.kartollika.secretpinetree.data.datasource

import android.location.LocationManager
import com.kartollika.secretpinetree.domain.datasource.LocationDataSource
import javax.inject.Inject

class LocationDataSourceImpl @Inject constructor(
  private val locationManager: LocationManager
): LocationDataSource {

  override fun isLocationEnabled(): Boolean {
    return locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
  }
}