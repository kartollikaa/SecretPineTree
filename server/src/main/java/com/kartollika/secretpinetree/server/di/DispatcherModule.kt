package com.kartollika.secretpinetree.server.di

import com.kartollika.secretpinetree.server.Dispatcher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DispatcherModule {

  @Provides
  fun provideCoroutineScope() = CoroutineScope(SupervisorJob() + Dispatchers.IO)

  @Provides
  @Singleton
  fun provideDispatcher(
    coroutineScope: CoroutineScope
  ): Dispatcher {
    return Dispatcher(
      scope = coroutineScope,
      io = Dispatchers.IO,
      main = Dispatchers.Main
    )
  }
}