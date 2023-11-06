package com.jhonsarq.nimblesurvey.model

data class LoginRequest(
    val grant_type: String,
    val email: String,
    val password: String,
    val client_id: String,
    val client_secret: String
)
