package com.mvnohopper.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mvnohopper.data.database.MvnoHopperDatabase
import com.mvnohopper.data.entity.MobileService
import com.mvnohopper.data.repository.MobileServiceRepository
import kotlinx.coroutines.launch

class AddEditViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MobileServiceRepository = MobileServiceRepository(
        MvnoHopperDatabase.getInstance(application).mobileServiceDao()
    )

    private val _saveResult = MutableLiveData<Boolean?>(null)
    val saveResult: LiveData<Boolean?> = _saveResult

    fun saveMobileService(mobileService: MobileService) {
        viewModelScope.launch {
            runCatching {
                repository.insert(mobileService)
            }.onSuccess {
                _saveResult.value = true
            }.onFailure {
                _saveResult.value = false
            }
        }
    }
}
