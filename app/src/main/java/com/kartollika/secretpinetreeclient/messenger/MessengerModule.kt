package com.kartollika.secretpinetreeclient.messenger

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MessengerModule {

  @Binds
  @Singleton
  abstract fun bindMessengerRepository(messengerRepositoryImpl: MessengerRepositoryImpl): MessengerRepository


}