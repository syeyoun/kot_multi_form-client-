package org.example.testproject.repository

import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.decodeRecord
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.example.testproject.models.ChatRoomEntity
import org.example.testproject.models.MessageEntity
import org.example.testproject.supabase.SupabaseClient
//import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.filter
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.flow.mapNotNull
import org.example.testproject.supabase.SupabaseClient.client

class ChatRepository {
    private val client = SupabaseClient.client

    // 채팅방 생성
    suspend fun createChatRoom(
        postId: Long,
        creatorId: String,
        applicantId: String
    ): ChatRoomEntity? {
        return try {
            client.from("chat_rooms")
                .insert(
                    ChatRoomEntity(
                        postId = postId,
                        creatorId = creatorId,
                        applicantId = applicantId
                    )
                ) {
                    select()
                }
                .decodeSingle<ChatRoomEntity>()
        } catch (e: Exception) {
            println("Error creating chat room: ${e.message}")
            null
        }
    }

    // 내 채팅방 목록 조회
    suspend fun getMyChatRooms(userId: String): List<ChatRoomEntity> {
        return try {
            val asCreator = client.from("chat_rooms")
                .select {
                    filter {
                        eq("creator_id", userId)
                    }
                }
                .decodeList<ChatRoomEntity>()

            val asApplicant = client.from("chat_rooms")
                .select {
                    filter {
                        eq("applicant_id", userId)
                    }
                }
                .decodeList<ChatRoomEntity>()

            (asCreator + asApplicant).distinctBy { it.id }
        } catch (e: Exception) {
            println("Error fetching chat rooms: ${e.message}")
            emptyList()
        }
    }

    // 메시지 전송
    suspend fun sendMessage(
        chatRoomId: Long,
        senderId: String,
        message: String
    ): MessageEntity? {
        return try {
            client.from("messages")
                .insert(
                    MessageEntity(
                        chatRoomId = chatRoomId,
                        senderId = senderId,
                        message = message
                    )
                ) {
                    select()
                }
                .decodeSingle<MessageEntity>()
        } catch (e: Exception) {
            println("Error sending message: ${e.message}")
            null
        }
    }

    // 채팅방 메시지 조회
    suspend fun getMessages(chatRoomId: Long): List<MessageEntity> {
        return try {
            client.from("messages")
                .select {
                    filter {
                        eq("chat_room_id", chatRoomId)
                    }
                }
                .decodeList<MessageEntity>()
        } catch (e: Exception) {
            println("Error fetching messages: ${e.message}")
            emptyList()
        }
    }

    // 실시간 메시지 구독 (수정!)
// 실시간 메시지 구독
    suspend fun subscribeToMessages(chatRoomId: Long): Flow<MessageEntity> {  // suspend 추가!
        val channelId = "messages-room-$chatRoomId"
        val channel = client.realtime.channel(channelId)

        val flow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "messages"
            filter = "chat_room_id=eq.$chatRoomId"
        }.mapNotNull { action ->
            when (action) {
                is PostgresAction.Insert -> {
                    println("========== REALTIME INSERT DETECTED ==========")
                    action.decodeRecord<MessageEntity>()
                }
                else -> null
            }
        }

        // 채널 구독 시작 (suspend)
        channel.subscribe()

        return flow
    }
    // 채팅방 이름 수정 (유저별로)
    suspend fun updateChatRoomName(
        chatRoomId: Long,
        customName: String,
        currentUserId: String,
        creatorId: String
    ): Boolean {
        return try {
            val isCreator = currentUserId == creatorId
            val columnName = if (isCreator) "creator_custom_name" else "applicant_custom_name"

            client.from("chat_rooms")
                .update({
                    set(columnName, customName)
                }) {
                    filter {
                        eq("id", chatRoomId)
                    }
                }
            true
        } catch (e: Exception) {
            println("Error updating chat room name: ${e.message}")
            false
        }
    }

    suspend fun getOrCreateChatRoom(
        postId: Long,
        userId: String,
        teacherId: String
    ): ChatRoomEntity? {
        return try {
            // 1. 기존 채팅방 확인
            val existing = client.from("chat_rooms")
                .select {
                    filter {
                        eq("post_id", postId)
                        or {
                            eq("applicant_id", userId)
                            eq("creator_id", userId)
                        }
                    }
                }
                .decodeSingle<ChatRoomEntity>()

            existing

        } catch (e: Exception) {
            // 2. 없으면 새로 생성
            try {
                client.from("chat_rooms")
                    .insert(mapOf(
                        "post_id" to postId,
                        "teacher_id" to teacherId,
                        "student_id" to userId,
                        "created_at" to kotlinx.datetime.Clock.System.now().toString()
                    ))
                    .decodeSingle<ChatRoomEntity>()
            } catch (e2: Exception) {
                println("Error creating chat room: ${e2.message}")
                null
            }
        }
    }
}
