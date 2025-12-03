package org.example.testproject.utils

expect class AuthPreferences(){
    fun saveLoginState(userId: String, userRole: String)
    fun getLoginState(): Pair<String?, String?>
    fun clearLoginState()
    fun isLoggedIn(): Boolean
}