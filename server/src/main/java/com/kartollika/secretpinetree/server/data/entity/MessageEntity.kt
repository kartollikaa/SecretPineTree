package com.kartollika.secretpinetree.server.data.entity

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Keep
@Entity(tableName = "Messages")
data class MessageEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Int = 0,
  @ColumnInfo(name = "author")
  val author: String,
  @ColumnInfo(name = "message")
  val message: String
)