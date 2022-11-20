package com.kartollika.secretpinetree.server.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kartollika.secretpinetree.server.data.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessagesDao {
  @Query("SELECT * from Messages ORDER BY id DESC LIMIT :count OFFSET :offset")
  fun getMessages(count: Int, offset: Int): Flow<List<MessageEntity>>

  @Query("SELECT * from MESSAGES ORDER BY id DESC LIMIT 1")
  fun getLastMessage(): Flow<MessageEntity>

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  fun insertMessage(message: MessageEntity)
}