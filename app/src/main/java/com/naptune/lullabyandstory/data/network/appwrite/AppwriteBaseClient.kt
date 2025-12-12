package com.naptune.lullabyandstory.data.appwrite

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import io.appwrite.Client
import io.appwrite.services.Databases
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppwriteBaseClient @Inject constructor(@ApplicationContext private val context: Context) {

    private val projectId = "671e0ca70034a1e99b3d"

    val client = Client(context)
        .setEndpoint("https://appwrite.taagidtech.com/v1")
        .setProject(projectId)

    val databases = Databases(client)

    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
}