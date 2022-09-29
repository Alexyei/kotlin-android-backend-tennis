package com.example.data.user

import org.bson.types.ObjectId

interface UserDataSource {
    suspend fun getUserByUsername(username: String): User?
    suspend fun getUserById(userid: ObjectId): User?
    suspend fun insertUser(user: User):Boolean
}