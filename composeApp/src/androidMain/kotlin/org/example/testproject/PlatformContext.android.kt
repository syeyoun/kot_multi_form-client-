package org.example.testproject

import android.content.Context
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.example.testproject.repository.AuthRepository

private var appContext: Context? = null

fun initPlatformContext(context: Context) {
    appContext = context
}

actual fun getPlatformContext(): Any? = appContext

actual fun saveFcmToken(userId: String) {
    FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
        if (task.isSuccessful) {
            val token = task.result
            println("=== SAVING FCM TOKEN: $token ===")

            GlobalScope.launch {
                val authRepo = AuthRepository()
                authRepo.saveFcmToken(userId, token)
                println("=== FCM TOKEN SAVED ===")
            }
        }
    }
}