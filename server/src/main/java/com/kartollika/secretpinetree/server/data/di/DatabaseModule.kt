package com.kartollika.secretpinetree.server.data.di

import android.content.Context
import androidx.room.Room
import com.kartollika.secretpinetree.server.data.PineTreeDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

  @Provides
  @Singleton
  fun providesImperialAssaultDatabase(
    @ApplicationContext context: Context,
  ): PineTreeDatabase = Room.databaseBuilder(
    context, PineTreeDatabase::class.java, "ia-database"
  )
    .build()

  @Provides
  fun provideMessagesDao(database: PineTreeDatabase) = database.messagesDao()
}
