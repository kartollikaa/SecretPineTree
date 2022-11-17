package com.kartollika.secretpinetree.client.data.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.kartollika.secretpinetree.client.data.adapters.ClientCommandSerializer
import com.kartollika.secretpinetree.client.domain.model.ClientRequest
import com.kartollika.secretpinetree.server.adapters.ClientCommandDeserializer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class GsonModule {

  @Provides
  @Singleton
  fun provideGson(): Gson {
    return GsonBuilder()
      .registerTypeAdapter(ClientRequest::class.java, ClientCommandSerializer())
      .registerTypeAdapter(ClientRequest::class.java, ClientCommandDeserializer())
      .create()
  }
}