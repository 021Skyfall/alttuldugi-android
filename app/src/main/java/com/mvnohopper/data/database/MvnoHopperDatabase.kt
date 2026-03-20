package com.mvnohopper.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.mvnohopper.data.dao.MobileServiceDao
import com.mvnohopper.data.entity.MobileService

@Database(
    entities = [MobileService::class],
    version = 3,
    exportSchema = false
)
abstract class MvnoHopperDatabase : RoomDatabase() {

    abstract fun mobileServiceDao(): MobileServiceDao

    companion object {
        @Volatile
        private var INSTANCE: MvnoHopperDatabase? = null

        fun getInstance(context: Context): MvnoHopperDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    MvnoHopperDatabase::class.java,
                    "mvno_hopper.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
