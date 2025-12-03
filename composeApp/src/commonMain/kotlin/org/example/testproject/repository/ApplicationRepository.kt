package org.example.testproject.repository

import io.github.jan.supabase.postgrest.from
import org.example.testproject.models.ApplicationEntity
import org.example.testproject.supabase.SupabaseClient
import org.example.testproject.repository.NotificationRepository


class ApplicationRepository {
    private val client = SupabaseClient.client

    // 신청하기
    suspend fun applyToPost(postId: Long, userId: String): ApplicationEntity? {
        return try {
            // 1. 현재 대기열 확인
            val existingApps = client.from("applications")
                .select {
                    filter {
                        eq("post_id", postId)
                        neq("status", "COMPLETED")  // COMPLETED 제외
                    }
                }
                .decodeList<ApplicationEntity>()

            // 2. 이미 신청했는지 확인
            if (existingApps.any { it.userId == userId }) {
                println("Already applied")
                return null
            }

            // 3. 대기열이 꽉 찼는지 확인 (최대 3명)
            if (existingApps.size >= 3) {
                println("Queue is full")
                return null
            }

            // 4. queue_position 계산
            val nextPosition = existingApps.size + 1

            // 5. status 결정 (1순위면 CHATTING, 아니면 WAITING)
            val status = if (nextPosition == 1) "CHATTING" else "WAITING"

            // 6. 신청 생성
            val application = client.from("applications")
                .insert(
                    ApplicationEntity(
                        postId = postId,
                        userId = userId,
                        status = status,
                        queuePosition = nextPosition
                    )
                ) {
                    select()
                }
                .decodeSingle<ApplicationEntity>()

            // 7. CHATTING이면 채팅방도 생성
            if (status == "CHATTING") {
                val chatRepo = ChatRepository()
                // 포스트 작성자 ID 가져오기
                val postRepo = PostRepository()
                val post = postRepo.getPostById(postId)
                if (post != null) {
                    chatRepo.createChatRoom(
                        postId = postId,
                        creatorId = post.userId,
                        applicantId = userId
                    )

                    // 알림 생성! (Teacher에게)
                    val notificationRepo = NotificationRepository()
                    println("=== CREATING NOTIFICATION FOR: ${post.userId} ===")
                    println("=== POST TITLE: ${post.title} ===")

                    val notification = notificationRepo.createNotification(
                        userId = post.userId,
                        title = "새 채팅이 시작되었습니다!",
                        message = "\"${post.title}\" 게시글에 학생이 신청했습니다.",
                        type = "NEW_CHAT",
                        postId = postId
                    )

                    println("=== NOTIFICATION RESULT: $notification ===")
                }
            }

            application
        } catch (e: Exception) {
            println("Error applying to post: ${e.message}")
            null
        }
    }

    // 포스트의 신청자 목록 조회
    suspend fun getApplicationsByPost(postId: Long): List<ApplicationEntity> {
        return try {
            client.from("applications")
                .select {
                    filter {
                        eq("post_id", postId)
                        neq("status", "COMPLETED")
                    }
                }
                .decodeList<ApplicationEntity>()
                .sortedBy { it.queuePosition }
        } catch (e: Exception) {
            println("Error fetching applications: ${e.message}")
            emptyList()
        }
    }

    // 유저의 신청 목록 조회
    suspend fun getApplicationsByUser(userId: String): List<ApplicationEntity> {
        return try {
            client.from("applications")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<ApplicationEntity>()
        } catch (e: Exception) {
            println("Error fetching user applications: ${e.message}")
            emptyList()
        }
    }

    // 신청 취소
    suspend fun cancelApplication(applicationId: Long): Boolean {
        return try {
            client.from("applications")
                .delete {
                    filter {
                        eq("id", applicationId)
                    }
                }
            true
        } catch (e: Exception) {
            println("Error canceling application: ${e.message}")
            false
        }
    }

    // 채팅 나가기 (대기자 자동 승격)
    suspend fun leaveChat(postId: Long, userId: String): Boolean {
        return try {
            // 1. 내 신청 상태를 COMPLETED로
            client.from("applications")
                .update({
                    set("status", "COMPLETED")
                }) {
                    filter {
                        eq("post_id", postId)
                        eq("user_id", userId)
                    }
                }

            // 2. 대기 중인 사람들 조회
            val waitingApps = client.from("applications")
                .select {
                    filter {
                        eq("post_id", postId)
                        eq("status", "WAITING")
                    }
                }
                .decodeList<ApplicationEntity>()
                .sortedBy { it.queuePosition }

            // 3. 대기자가 있으면 첫 번째 사람을 CHATTING으로
            if (waitingApps.isNotEmpty()) {
                val nextUser = waitingApps.first()

                // status 업데이트
                client.from("applications")
                    .update({
                        set("status", "CHATTING")
                    }) {
                        filter {
                            eq("id", nextUser.id)
                        }
                    }

                // queue_position 재정렬
                waitingApps.forEachIndexed { index, app ->
                    client.from("applications")
                        .update({
                            set("queue_position", index + 1)
                        }) {
                            filter {
                                eq("id", app.id)
                            }
                        }
                }

                // 채팅방 생성
                val chatRepo = ChatRepository()
                val postRepo = PostRepository()
                val post = postRepo.getPostById(postId)
                if (post != null) {
                    chatRepo.createChatRoom(
                        postId = postId,
                        creatorId = post.userId,
                        applicantId = nextUser.userId
                    )
                }
            }

            true
        } catch (e: Exception) {
            println("Error leaving chat: ${e.message}")
            false
        }
    }

    // 다음 학생으로 (현재 Application 삭제 + 다음 사람 매칭)
    suspend fun nextStudent(postId: Long, currentUserId: String): Boolean {
        return try {
            // 1. 현재 채팅방 삭제
            val chatRepo = ChatRepository()
            client.from("chat_rooms")
                .delete {
                    filter {
                        eq("post_id", postId)
                        or {
                            eq("creator_id", currentUserId)
                            eq("applicant_id", currentUserId)
                        }
                    }
                }

            // 2. 현재 Application 삭제
            client.from("applications")
                .delete {
                    filter {
                        eq("post_id", postId)
                        eq("user_id", currentUserId)
                    }
                }

            // 3. 대기 중인 사람들 조회
            val waitingApps = client.from("applications")
                .select {
                    filter {
                        eq("post_id", postId)
                        eq("status", "WAITING")
                    }
                }
                .decodeList<ApplicationEntity>()
                .sortedBy { it.queuePosition }

            // 4. 대기자가 있으면 첫 번째 사람을 CHATTING으로
            if (waitingApps.isNotEmpty()) {
                val nextUser = waitingApps.first()

                // status 업데이트
                client.from("applications")
                    .update({
                        set("status", "CHATTING")
                        set("queue_position", 0)
                    }) {
                        filter {
                            eq("id", nextUser.id)
                        }
                    }

                // 나머지 queue_position 재정렬
                waitingApps.drop(1).forEachIndexed { index, app ->
                    client.from("applications")
                        .update({
                            set("queue_position", index + 1)
                        }) {
                            filter {
                                eq("id", app.id)
                            }
                        }
                }

                // 새 채팅방 생성
                val postRepo = PostRepository()
                val post = postRepo.getPostById(postId)
                if (post != null) {
                    chatRepo.createChatRoom(
                        postId = postId,
                        creatorId = post.userId,
                        applicantId = nextUser.userId
                    )
                }
            }

            true
        } catch (e: Exception) {
            println("Error moving to next student: ${e.message}")
            false
        }
    }
}