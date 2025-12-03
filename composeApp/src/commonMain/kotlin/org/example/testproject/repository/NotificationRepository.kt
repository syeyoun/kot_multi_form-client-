package org.example.testproject.repository

import io.github.jan.supabase.postgrest.from
import org.example.testproject.models.NotificationEntity
import org.example.testproject.supabase.SupabaseClient
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.realtime.decodeRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull

class NotificationRepository {
    private val client = SupabaseClient.client

    // 알림 생성
    suspend fun createNotification(
        userId: String,
        title: String,
        message: String,
        type: String,
        postId: Long? = null
    ): NotificationEntity? {
        return try {
            println("=== INSERTING NOTIFICATION ===")

            val result = client.from("notifications")
                .insert(
                    buildJsonObject {
                        put("user_id", userId)
                        put("title", title)
                        put("message", message)
                        put("type", type)
                        if (postId != null) {
                            put("post_id", postId)
                        }
                    }
                ) {
                    select()
                }
                .decodeSingle<NotificationEntity>()

            println("=== NOTIFICATION CREATED: $result ===")
            result
        } catch (e: Exception) {
            println("=== NOTIFICATION ERROR: ${e.message} ===")
            e.printStackTrace()
            null
        }
    }

    // 내 알림 목록 조회
    suspend fun getMyNotifications(userId: String): List<NotificationEntity> {
        return try {
            client.from("notifications")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<NotificationEntity>()
                .sortedByDescending { it.createdAt }
        } catch (e: Exception) {
            println("Error fetching notifications: ${e.message}")
            emptyList()
        }
    }

    // 알림 읽음 처리
    suspend fun markAsRead(notificationId: Long): Boolean {
        return try {
            client.from("notifications")
                .update({
                    set("is_read", true)
                }) {
                    filter {
                        eq("id", notificationId)
                    }
                }
            true
        } catch (e: Exception) {
            println("Error marking notification as read: ${e.message}")
            false
        }
    }

    // 실시간 알림 구독
    suspend fun subscribeToNotifications(userId: String): Flow<NotificationEntity> {
        val channelId = "notifications-user-$userId"
        val channel = client.realtime.channel(channelId)

        val flow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "notifications"
            filter = "user_id=eq.$userId"
        }.mapNotNull { action ->
            when (action) {
                is PostgresAction.Insert -> {
                    println("=== NEW NOTIFICATION RECEIVED ===")
                    action.decodeRecord<NotificationEntity>()
                }
                else -> null
            }
        }

        channel.subscribe()
        return flow
    }

    // 읽지 않은 알림 개수
    suspend fun getUnreadCount(userId: String): Int {
        return try {
            val notifications = client.from("notifications")
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("is_read", false)
                    }
                }
                .decodeList<NotificationEntity>()
            notifications.size
        } catch (e: Exception) {
            println("Error fetching unread count: ${e.message}")
            0
        }
    }
}