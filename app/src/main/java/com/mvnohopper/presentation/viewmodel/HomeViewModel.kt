package com.mvnohopper.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.mvnohopper.data.database.MvnoHopperDatabase
import com.mvnohopper.data.entity.MobileService
import com.mvnohopper.data.repository.MobileServiceRepository
import com.mvnohopper.domain.model.MobileServiceWithCalculations
import com.mvnohopper.domain.usecase.CalculateDateUseCase
import androidx.lifecycle.map

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MobileServiceRepository = MobileServiceRepository(
        MvnoHopperDatabase.getInstance(application).mobileServiceDao()
    )
    private val calculateDateUseCase = CalculateDateUseCase()

    val mobileServices: LiveData<List<MobileServiceWithCalculations>> =
        repository.getAll().map { items: List<MobileService> ->
            items.map { calculateDateUseCase(it) }
        }
}
