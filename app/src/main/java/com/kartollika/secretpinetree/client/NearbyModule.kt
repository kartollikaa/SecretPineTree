package com.kartollika.secretpinetree.client

import android.content.Context
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.ConnectionsClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class NearbyModule {

  @Provides
  fun provideNearbyConnectionClient(@ApplicationContext context: Context): ConnectionsClient {
    return Nearby.getConnectionsClient(context)
  }
}