package org.example.testproject.repository

import io.github.jan.supabase.postgrest.from
import org.example.testproject.models.User
import org.example.testproject.supabase.SupabaseClient
import org.example.testproject.supabase.SupabaseClient.client

class AuthRepository {
    private val client = SupabaseClient.client

    // 회원가입
    suspend fun signUp(
        email: String,
        password: String,
        nickname: String,
        role: String  // 추가!
    ): User? {
        return try {
            // 이메일 중복 체크
            val existing = client.from("users")
                .select {
                    filter {
                        eq("email", email)
                    }
                }
                .decodeList<User>()

            if (existing.isNotEmpty()) {
                println("Email already exists")
                return null
            }

            // 유저 생성
            client.from("users")
                .insert(
                    User(
                        email = email,
                        password = password,  // 실제로는 해싱 필요!
                        nickname = nickname,
                        role = role
                    )
                ) {
                    select()
                }
                .decodeSingle<User>()
        } catch (e: Exception) {
            println("Signup error: ${e.message}")
            null
        }
    }

    // 로그인
    suspend fun login(email: String, password: String): User? {
        return try {
            val users = client.from("users")
                .select {
                    filter {
                        eq("email", email)
                        eq("password", password)
                    }
                }
                .decodeList<User>()

            users.firstOrNull()
        } catch (e: Exception) {
            println("Login error: ${e.message}")
            null
        }
    }

    suspend fun getUserById(userId: String): User? {
        return try {
            SupabaseClient.client.from("users")
                .select {
                    filter {
                        eq("id", userId)
                    }
                }
                .decodeSingle<User>()
        } catch (e: Exception) {
            println("Get user error: ${e.message}")
            null
        }
    }

    // FCM 토큰 저장
    suspend fun saveFcmToken(userId: String, token: String): Boolean {
        return try {
            client.from("users")
                .update({
                    set("fcm_token", token)
                }) {
                    filter {
                        eq("id", userId)
                    }
                }
            println("=== FCM TOKEN SAVED ===")
            true
        } catch (e: Exception) {
            println("Error saving FCM token: ${e.message}")
            false
        }
    }
}

