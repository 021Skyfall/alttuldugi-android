package com.mvnohopper.data.repository

import androidx.lifecycle.LiveData
import com.mvnohopper.data.dao.MobileServiceDao
import com.mvnohopper.data.entity.MobileService

class MobileServiceRepository(
    private val mobileServiceDao: MobileServiceDao
) {
    fun getAll(): LiveData<List<MobileService>> = mobileServiceDao.getAll()

    suspend fun deleteByIds(ids: Collection<Long>) {
        if (ids.isEmpty()) return
        mobileServiceDao.deleteByIds(ids.toList())
    }

    suspend fun insert(mobileService: MobileService): Long = mobileServiceDao.insert(mobileService)

    suspend fun update(mobileService: MobileService) = mobileServiceDao.update(mobileService)

    suspend fun delete(mobileService: MobileService) = mobileServiceDao.delete(mobileService)
}
