package com.jhonsarq.nimblesurvey.utilities

import android.content.Context
import com.jhonsarq.nimblesurvey.model.User

class DbClient {
    fun getProfile(context: Context): User {
        val db = Database(context)
        val data = db.getData("user", null, null, null, null, null, null)
        var user = User(null, null, null, null, null, null, null, null)

        if(data.isNotEmpty()) {
            val accessToken = data[0]["accessToken"].toString()
            val expiresIn = data[0]["expiresIn"].toString().toLong()
            val refreshToken = data[0]["refreshToken"].toString()
            val createdAt = data[0]["createdAt"].toString().toLong()
            val name = data[0]["name"].toString()
            val avatarUrl = data[0]["avatarUrl"].toString()

            user = User(true, accessToken, expiresIn, refreshToken, createdAt, name, avatarUrl, null)
        }

        return user
    }
}