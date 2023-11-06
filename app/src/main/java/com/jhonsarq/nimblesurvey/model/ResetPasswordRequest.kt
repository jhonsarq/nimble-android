package com.jhonsarq.nimblesurvey.model

data class ResetPasswordRequest(
    val user: ResetPasswordEmailRequest,
    val client_id: String,
    val client_secret: String
)
