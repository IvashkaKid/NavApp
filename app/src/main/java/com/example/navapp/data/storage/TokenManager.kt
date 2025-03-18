package com.example.navapp.data.storage

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object TokenManager {
    private const val PREF_NAME = "secure_prefs"
    private const val KEY_TOKEN = "auth_token"
    private const val KEY_LOGIN = "user_login"

    private fun getEncryptedSharedPreferences(context: Context) =
        EncryptedSharedPreferences.create(
            context,
            PREF_NAME,
            MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

    fun saveToken(context: Context, token: String) {
        val prefs = getEncryptedSharedPreferences(context)
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    fun getToken(context: Context): String? {
        val prefs = getEncryptedSharedPreferences(context)
        return prefs.getString(KEY_TOKEN, null)
    }

    fun deleteToken(context: Context) {
        val prefs = getEncryptedSharedPreferences(context)
        prefs.edit().remove(KEY_TOKEN).apply()
    }

    fun saveLogin(context: Context, login: String) {
        getEncryptedSharedPreferences(context).edit()
            .putString(KEY_LOGIN, login)
            .apply()
    }

    fun getLogin(context: Context): String? {
        return getEncryptedSharedPreferences(context)
            .getString(KEY_LOGIN, null)
    }
}