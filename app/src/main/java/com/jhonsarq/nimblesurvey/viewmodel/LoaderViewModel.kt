package com.jhonsarq.nimblesurvey.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LoaderViewModel: ViewModel() {
    private val _loader = MutableLiveData<Boolean?>()
    val loader: LiveData<Boolean?> = _loader

    fun setLoader(data: Boolean?) {
        _loader.value = data
    }
}