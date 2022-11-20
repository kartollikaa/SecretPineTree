package com.kartollika.secretpinetree.server.di

import com.kartollika.secretpinetree.server.repository.MessagesRepository
import com.kartollika.secretpinetree.server.repository.MessagesRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface DataModule {

  @Binds
  @Singleton
  fun bindsPineTreeRepository(messagesRepository: MessagesRepositoryImpl): MessagesRepository
}