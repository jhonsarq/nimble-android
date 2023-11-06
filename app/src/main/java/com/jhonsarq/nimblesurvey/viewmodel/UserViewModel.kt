package com.jhonsarq.nimblesurvey.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jhonsarq.nimblesurvey.model.LoginRequest
import com.jhonsarq.nimblesurvey.model.LogoutRequest
import com.jhonsarq.nimblesurvey.model.RefreshTokenRequest
import com.jhonsarq.nimblesurvey.model.ResetPasswordRequest
import com.jhonsarq.nimblesurvey.model.User
import com.jhonsarq.nimblesurvey.utilities.ApiClient
import com.jhonsarq.nimblesurvey.utilities.DbClient
import kotlinx.coroutines.launch

class UserViewModel: ViewModel() {
    private val apiClient = ApiClient()
    private val dbClient = DbClient()
    private val _user = MutableLiveData<User?>()
    private val _message = MutableLiveData<String?>()
    val user: LiveData<User?> = _user
    val message: LiveData<String?> = _message

    fun login(parameters: LoginRequest) {
        viewModelScope.launch {
            val response: User = apiClient.login(parameters)

            _user.value = response
        }
    }

    fun resetPassword(parameters: ResetPasswordRequest) {
        viewModelScope.launch {
            val response: String = apiClient.resetPassword(parameters)

            _message.value = response
        }
    }

    fun getLocalProfile(context: Context) {
        _user.value = dbClient.getProfile(context)
    }

    fun getProfile(user: User, context: Context) {
        viewModelScope.launch {
            val response: User = apiClient.getProfile(user, context)

            _user.value = response
        }
    }

    fun refreshToken(user: User, context: Context, parameters: RefreshTokenRequest) {
        viewModelScope.launch {
            val response: User = apiClient.refreshToken(user, context, parameters)

            _user.value = response
        }
    }

    fun logout(context: Context, parameters: LogoutRequest) {
        viewModelScope.launch {
            val response: User = apiClient.logout(context, parameters)

            _user.value = response
        }
    }
}