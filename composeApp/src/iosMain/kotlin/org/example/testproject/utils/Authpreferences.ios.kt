package org.example.testproject.utils

actual class AuthPreferences {
    actual fun saveLoginState(userId: String, userRole: String) {}
    actual fun getLoginState(): Pair<String?, String?> = Pair(null, null)
    actual fun clearLoginState() {}
    actual fun isLoggedIn(): Boolean = false
}