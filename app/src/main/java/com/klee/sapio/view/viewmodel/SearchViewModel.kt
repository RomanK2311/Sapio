package com.klee.sapio.view.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.klee.sapio.model.Application
import com.klee.sapio.model.ApplicationsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor() : ViewModel() {

    @Inject
    lateinit var applicationRepository: ApplicationsRepository

    val foundApplications = MutableLiveData<List<Application>>()

    fun searchApplication(pattern: String) {
        viewModelScope.launch {
            val result = applicationRepository.searchApplications(pattern)
            foundApplications.postValue(result)
        }
    }
}