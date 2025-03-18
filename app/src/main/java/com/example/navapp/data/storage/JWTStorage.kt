package com.example.navapp.data.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object SecureStorage {
    private const val PREFS_NAME = "secure_prefs"
    private const val KEY_JWT = "jwt_token"

    private fun encryptedPrefs(context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun saveToken(context: Context, token: String) {
        encryptedPrefs(context).edit().putString(KEY_JWT, token).apply()
    }

    fun getToken(context: Context): String? {
        return encryptedPrefs(context).getString(KEY_JWT, null)
    }

    fun clearToken(context: Context) {
        encryptedPrefs(context).edit().remove(KEY_JWT).apply()
    }
}