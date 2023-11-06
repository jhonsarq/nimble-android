package com.jhonsarq.nimblesurvey.model

data class SurveyQuestionRequest(
    val id: String,
    val answers: List<SurveyAnswerRequest>
)
