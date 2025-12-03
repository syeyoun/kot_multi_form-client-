package org.example.testproject.utils

import android.content.Context
import android.content.SharedPreferences
import org.example.testproject.getPlatformContext

actual class AuthPreferences {
    private val prefs: SharedPreferences? = run {
        val context = getPlatformContext() as? Context
        context?.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    }

    actual fun saveLoginState(userId: String, userRole: String) {
        prefs?.edit()
            ?.putString("user_id", userId)
            ?.putString("user_role", userRole)
            ?.putBoolean("is_logged_in", true)
            ?.apply()
        println("=== LOGIN STATE SAVED: $userId, $userRole ===")
    }

    actual fun getLoginState(): Pair<String?, String?> {
        val userId = prefs?.getString("user_id", null)
        val userRole = prefs?.getString("user_role", null)
        println("=== LOGIN STATE LOADED: $userId, $userRole ===")
        return Pair(userId, userRole)
    }

    actual fun clearLoginState() {
        prefs?.edit()?.clear()?.apply()
        println("=== LOGIN STATE CLEARED ===")
    }

    actual fun isLoggedIn(): Boolean {
        return prefs?.getBoolean("is_logged_in", false) ?: false
    }
}