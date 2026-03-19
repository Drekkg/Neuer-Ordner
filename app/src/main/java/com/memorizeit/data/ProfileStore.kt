package com.memorizeit.data

import android.content.Context
import com.memorizeit.model.UserProfile
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ProfileStore(context: Context) {
    private val prefs = context.getSharedPreferences("mem_profile", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    fun load(): UserProfile {
        val saved = prefs.getString(KEY_PROFILE, null) ?: return UserProfile()
        return runCatching { json.decodeFromString<UserProfile>(saved) }.getOrElse { UserProfile() }
    }

    fun save(profile: UserProfile) {
        prefs.edit().putString(KEY_PROFILE, json.encodeToString(profile)).apply()
    }

    companion object {
        private const val KEY_PROFILE = "profile_json"
    }
}
