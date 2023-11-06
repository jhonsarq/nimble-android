package com.jhonsarq.nimblesurvey.model

data class Answer(
    val id: String,
    val text: String,
    val order: Int,
    val type: String,
    val placeholder: String?
)
