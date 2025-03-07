package fr.isen.clement.isensmartcompanion.database

import androidx.room.Database
import androidx.room.RoomDatabase
import fr.isen.clement.isensmartcompanion.dao.MessageDao
import fr.isen.clement.isensmartcompanion.models.Message

@Database(entities = [Message::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
}