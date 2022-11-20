package com.kartollika.secretpinetree.server.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kartollika.secretpinetree.server.data.dao.MessagesDao
import com.kartollika.secretpinetree.server.data.entity.MessageEntity

@Database(
  entities = [
    MessageEntity::class
  ],
  version = 1
)
abstract class PineTreeDatabase : RoomDatabase() {
  abstract fun messagesDao(): MessagesDao
}