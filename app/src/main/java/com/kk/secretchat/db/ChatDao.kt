package com.kk.secretchat.db

import androidx.room.*
import com.kk.secretchat.chat.ModelChat

@Dao
interface ChatDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(chatModel: ModelChat): Long

    @Update
    fun update(vararg chatModel: ModelChat): Int

    @Update
    fun update(chatModel: ModelChat): Int

//    @Query("UPDATE ModelChat SET repliedID=:chatId WHERE chatID = :chatId")
//    fun updateReplied(chatId: String)

    @Query("UPDATE ModelChat SET isDeliver=1 WHERE chatID = :chatId")
    fun updateDelivery(chatId: String)

    @Query("UPDATE ModelChat SET isRead=1 AND isDeliver=1 WHERE chatID = :chatId")
    fun updateRead(chatId: String)

    @Delete
    fun delete(chatModel: ModelChat)

    @Query("DELETE FROM ModelChat")
    fun deleteAll()

    @Query("SELECT * FROM ModelChat where _id = :id")
    fun selectForId(id: Long): ModelChat

    @Query("SELECT * FROM ModelChat where fromId = :from AND toId = :to")
    fun selectAllFromAndToUser(from: String, to: String): List<ModelChat>

    @Query("SELECT * FROM ModelChat where fromId = :from AND toId = :to AND timestamp > :time")
    fun selectTodaysChatAllFromAndToUser(from: String, to: String, time: Long): List<ModelChat>

    @Query("SELECT * FROM ModelChat where fromId = :from ")
    fun selectAllFromUser(from: String): List<ModelChat>

    @Query("SELECT * FROM ModelChat where timestamp > :timestamp ORDER BY timestamp DESC")
    fun selectAllFromTime(timestamp: Long): List<ModelChat>

    @Query("SELECT chatID FROM ModelChat where isRead = 0 AND isIncomming = 1")
    fun selectAllUnreadIds(): List<String>



    @Query("SELECT * FROM ModelChat ORDER BY timestamp DESC")
    fun selectAll(): List<ModelChat>
}
