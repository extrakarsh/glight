package com.example.glight.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.glight.data.local.dao.ComplaintDao
import com.example.glight.data.local.dao.PoleDao
import com.example.glight.data.local.dao.SyncQueueDao
import com.example.glight.data.local.entity.ComplaintEntity
import com.example.glight.data.local.entity.PoleEntity
import com.example.glight.data.local.entity.SyncQueueEntity

@Database(
    entities = [PoleEntity::class, ComplaintEntity::class, SyncQueueEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun poleDao(): PoleDao
    abstract fun complaintDao(): ComplaintDao
    abstract fun syncQueueDao(): SyncQueueDao
}
