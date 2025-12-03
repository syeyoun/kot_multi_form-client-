package org.example.testproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
//import android.os.Bundle
//import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.tooling.preview.Preview
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.example.testproject.repository.AuthRepository

//class MainActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        enableEdgeToEdge()
//        super.onCreate(savedInstanceState)
//
//        setContent {
//            App()
//        }
//    }
//}
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Context 초기화
        initPlatformContext(applicationContext)

        // FCM 토큰 가져오기
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                println("=== FCM TOKEN: $token ===")

                // 로그인 후 토큰 저장하도록 전달
                saveFcmTokenToPrefs(token)
            }
        }

        setContent {
            App()
        }
    }

    private fun saveFcmTokenToPrefs(token: String) {
        val prefs = getSharedPreferences("fcm_prefs", MODE_PRIVATE)
        prefs.edit().putString("fcm_token", token).apply()
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}