package com.kartollika.secretpinetreeclient.messenger

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.preferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import com.kartollika.secretpinetreeclient.DataStoreModule
import com.kartollika.secretpinetreeclient.Message
import com.kartollika.secretpinetreeclient.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Named

interface MessengerRepository {
  fun getName(): Flow<String>
  suspend fun saveName(name: String)
  fun getMessages(): Flow<List<Message>>
  fun putMessages(messages: List<Message>)
}

class MessengerRepositoryImpl @Inject constructor(
  @Named(DataStoreModule.USER_PREFERENCES_DATASTORE)
  private val dataStore: DataStore<Preferences>
) : MessengerRepository {

  private val _messages = MutableSharedFlow<List<Message>>(replay = 1)

  override suspend fun saveName(name: String) {
    dataStore.edit { preferences ->
      preferences[USER_NAME_KEY] = name
    }
  }

  override fun getName(): Flow<String> {
    return dataStore.data.map { preferences ->
      preferences[USER_NAME_KEY] ?: ""
    }
  }

  override fun getMessages(): Flow<List<Message>> = _messages.asSharedFlow()

  override fun putMessages(messages: List<Message>) {
    _messages.tryEmit(messages)
  }

  companion object {
    private const val USER_NAME = "USER_NAME"
    private val USER_NAME_KEY = stringPreferencesKey(USER_NAME)
  }

}