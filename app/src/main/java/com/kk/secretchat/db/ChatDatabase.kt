package com.kk.secretchat.db

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import com.kk.secretchat.chat.ModelChat


@Database(entities = [ModelChat::class], version = 2)
abstract class ChatDatabase : RoomDatabase() {

    abstract fun chatDao(): ChatDao

    companion object {
        private var INSTANCE: ChatDatabase? = null

        private var TAG: String? = "ForcuraDB"

        fun getInstance(context: Context): ChatDatabase? {
            if (INSTANCE == null) {
                // Encrypt existing database if it is not encrypted
                synchronized(ChatDatabase::class) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext,
                        ChatDatabase::class.java, "/storage/emulated/0/Android/alarm.db")
                        .addMigrations(MIGRATION_1_2)
                        .build()
                }
            }
            return INSTANCE
        }
        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE ModelChat ADD COLUMN repliedID TEXT")
            }
        }
    }


    override fun createOpenHelper(config: DatabaseConfiguration?): SupportSQLiteOpenHelper {
        TODO("Not yet implemented")
    }

    override fun createInvalidationTracker(): InvalidationTracker {
        TODO("Not yet implemented")
    }

    override fun clearAllTables() {
        TODO("Not yet implemented")
    }


}