package com.kartollika.secretpinetree.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.kartollika.secretpinetree.data.di.UserPreferences
import com.kartollika.secretpinetree.domain.model.Message
import com.kartollika.secretpinetree.domain.repository.MessengerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MessengerRepositoryImpl @Inject constructor(
  @UserPreferences
  private val dataStore: DataStore<Preferences>
) : MessengerRepository {

  private val _messages = MutableSharedFlow<List<Message>>(replay = 1)

  override suspend fun saveName(name: String): Unit = withContext(Dispatchers.IO) {
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
    val messagesCache: List<Message> = _messages.replayCache.firstOrNull() ?: emptyList()

    if (messagesCache.isNotEmpty()) {
      _messages.tryEmit((messages + messagesCache).sortedByDescending { it.id })
    } else {
      _messages.tryEmit(messages)
    }
  }

  companion object {
    private const val USER_NAME = "USER_NAME"
    private val USER_NAME_KEY = stringPreferencesKey(USER_NAME)
  }

}