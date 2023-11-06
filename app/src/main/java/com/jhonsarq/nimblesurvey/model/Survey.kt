package com.jhonsarq.nimblesurvey.model

data class Survey(
    val id: String,
    val title: String,
    val description: String,
    val coverImageUrl: String,
    val type: String,
    val thanksText: String?,
    val thanksCoverImageUrl: String?,
    val questions: List<Question>?
)
