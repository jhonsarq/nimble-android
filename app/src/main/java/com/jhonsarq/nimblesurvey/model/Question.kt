package com.jhonsarq.nimblesurvey.model

data class Question(
    val id: String,
    val text: String,
    val helpText: String?,
    val order: Int,
    val pick: String,
    val type: String,
    val coverImageUrl: String,
    val answers: List<Answer>?
)
