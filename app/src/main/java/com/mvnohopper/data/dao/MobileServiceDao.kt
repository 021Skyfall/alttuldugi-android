package com.mvnohopper.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mvnohopper.data.entity.MobileService

@Dao
interface MobileServiceDao {

    @Query("SELECT * FROM mobile_services ORDER BY activationDate DESC")
    fun getAll(): LiveData<List<MobileService>>

    @Query("SELECT * FROM mobile_services WHERE id = :id")
    suspend fun getById(id: Long): MobileService?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(mobileService: MobileService): Long

    @Update
    suspend fun update(mobileService: MobileService)

    @Delete
    suspend fun delete(mobileService: MobileService)
}
