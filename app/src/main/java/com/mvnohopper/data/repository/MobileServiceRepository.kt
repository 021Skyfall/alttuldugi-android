package com.mvnohopper.data.repository

import androidx.lifecycle.LiveData
import com.mvnohopper.data.dao.MobileServiceDao
import com.mvnohopper.data.entity.MobileService

class MobileServiceRepository(
    private val mobileServiceDao: MobileServiceDao
) {
    fun getAll(): LiveData<List<MobileService>> = mobileServiceDao.getAll()

    suspend fun getById(id: Long): MobileService? = mobileServiceDao.getById(id)

    suspend fun insert(mobileService: MobileService): Long = mobileServiceDao.insert(mobileService)

    suspend fun update(mobileService: MobileService) = mobileServiceDao.update(mobileService)

    suspend fun delete(mobileService: MobileService) = mobileServiceDao.delete(mobileService)
}
