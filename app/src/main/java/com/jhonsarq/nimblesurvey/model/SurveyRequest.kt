package com.jhonsarq.nimblesurvey.model

data class SurveyRequest(
    val survey_id: String,
    val questions: List<SurveyQuestionRequest>
)
