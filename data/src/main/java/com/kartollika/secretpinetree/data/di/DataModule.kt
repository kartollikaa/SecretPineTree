package com.kartollika.secretpinetree.data.di

import com.kartollika.secretpinetree.data.datasource.LocationDataSourceImpl
import com.kartollika.secretpinetree.data.repository.MessengerRepositoryImpl
import com.kartollika.secretpinetree.domain.datasource.LocationDataSource
import com.kartollika.secretpinetree.domain.repository.MessengerRepository
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
  fun bindMessengerRepository(messengerRepositoryImpl: MessengerRepositoryImpl): MessengerRepository

  @Binds
  @Singleton
  fun bindLocationDataSource(locationDataSourceImpl: LocationDataSourceImpl): LocationDataSource
}