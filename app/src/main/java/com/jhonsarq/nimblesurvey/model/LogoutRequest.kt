package com.jhonsarq.nimblesurvey.model

data class LogoutRequest(
    val token: String,
    val client_id: String,
    val client_secret: String
)
