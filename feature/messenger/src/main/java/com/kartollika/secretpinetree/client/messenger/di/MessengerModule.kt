package com.kartollika.secretpinetree.client.messenger.di

import com.kartollika.secretpinetree.client.messenger.vo.MessageVOMapper
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