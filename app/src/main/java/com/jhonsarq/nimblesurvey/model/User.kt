package com.jhonsarq.nimblesurvey.model

data class User(
    val success: Boolean?,
    val accessToken: String?,
    val expiresIn: Long?,
    val refreshToken: String?,
    val createdAt: Long?,
    val name: String?,
    val avatarUrl: String?,
    val message: String?
)
