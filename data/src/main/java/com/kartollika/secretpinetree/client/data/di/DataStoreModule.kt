package com.kartollika.secretpinetree.client.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
class DataStoreModule {

  @Named(USER_PREFERENCES_DATASTORE)
  @Provides
  fun provideUserDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
    return context.userPreferenceDataStore
  }

  private val Context.userPreferenceDataStore by preferencesDataStore(
    name = USER_PREFERENCES_DATASTORE
  )

  companion object {
    const val USER_PREFERENCES_DATASTORE = "user_preferences"
  }
}