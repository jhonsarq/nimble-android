package com.jhonsarq.nimblesurvey.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jhonsarq.nimblesurvey.model.ApiResponse
import com.jhonsarq.nimblesurvey.model.Survey
import com.jhonsarq.nimblesurvey.model.SurveyRequest
import com.jhonsarq.nimblesurvey.utilities.ApiClient
import kotlinx.coroutines.launch

class SurveysViewModel: ViewModel() {
    private val apiClient = ApiClient()
    private val _surveys = MutableLiveData<List<Survey>?>()
    private val _survey = MutableLiveData<Survey?>()
    private val _surveyId = MutableLiveData<String?>()
    private val _apiResponse = MutableLiveData<ApiResponse?>()
    private val _successClosed = MutableLiveData<Boolean?>()
    val surveys: LiveData<List<Survey>?> = _surveys
    val survey: LiveData<Survey?> = _survey
    val surveyId: LiveData<String?> = _surveyId
    val apiResponse: LiveData<ApiResponse?> = _apiResponse
    val successClosed: LiveData<Boolean?> = _successClosed

    fun getSurveys(accessToken: String) {
        viewModelScope.launch {
            val response: List<Survey> = apiClient.getSurveys(accessToken)

            _surveys.value = response
        }
    }

    fun getSurvey(accessToken: String, id: String) {
        viewModelScope.launch {
            val response: Survey? = apiClient.getSurvey(accessToken, id)

            _survey.value = response
        }
    }

    fun submitSurvey(accessToken: String, parameters: SurveyRequest) {
        viewModelScope.launch {
            val response: ApiResponse? = apiClient.submitSurvey(accessToken, parameters)

            _apiResponse.value = response
        }
    }

    fun cleanSurveys() {
        _surveys.value = null
    }

    fun cleanSurvey() {
        _survey.value = null
    }

    fun cleanApiResponse() {
        _apiResponse.value = null
    }

    fun setSurveyId(data: String?) {
        _surveyId.value = data
    }

    fun setSuccessClosed(data: Boolean?) {
        _successClosed.value = data
    }
}