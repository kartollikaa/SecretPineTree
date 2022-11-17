package com.kartollika.secretpinetree.client.messenger

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class MessengerModule {

  @Provides
  fun provideMessageVOMapper() = MessageVOMapper()
}