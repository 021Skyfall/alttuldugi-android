package com.mvnohopper.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.mvnohopper.data.database.MvnoHopperDatabase
import com.mvnohopper.data.entity.MobileService
import com.mvnohopper.data.repository.MobileServiceRepository

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MobileServiceRepository = MobileServiceRepository(
        MvnoHopperDatabase.getInstance(application).mobileServiceDao()
    )

    val mobileServices: LiveData<List<MobileService>> = repository.getAll()
}
