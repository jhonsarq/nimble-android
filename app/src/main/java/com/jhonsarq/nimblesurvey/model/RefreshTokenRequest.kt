package com.jhonsarq.nimblesurvey.model

data class RefreshTokenRequest(
    val grant_type: String,
    val refresh_token: String,
    val client_id: String,
    val client_secret: String
)
