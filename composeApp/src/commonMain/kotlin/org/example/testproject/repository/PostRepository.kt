package org.example.testproject.repository

import io.github.jan.supabase.postgrest.from
import org.example.testproject.models.PostEntity
import org.example.testproject.supabase.SupabaseClient

class PostRepository {
    private val client = SupabaseClient.client

    // 게시글 작성
    suspend fun createPost(post: PostEntity): PostEntity? {
        return try {
            client.from("posts")
                .insert(post) {
                    select()
                }
                .decodeSingle<PostEntity>()
        } catch (e: Exception) {
            println("Error creating post: ${e.message}")
            null
        }
    }

    // 특정 구의 게시글 조회
    suspend fun getPostsByDistrict(districtId: Int): List<PostEntity> {
        return try {
            client.from("posts")
                .select {
                    filter {
                        eq("district_id", districtId)
                    }
//                    order("created_at", ascending = false)
                }
                .decodeList<PostEntity>()
        } catch (e: Exception) {
            println("Error fetching posts: ${e.message}")
            emptyList()
        }
    }

    // 모든 게시글 조회
    suspend fun getAllPosts(): List<PostEntity> {
        return try {
            client.from("posts")
                .select {
//                    order("created_at", ascending = false)
                }
                .decodeList<PostEntity>()
        } catch (e: Exception) {
            println("Error fetching all posts: ${e.message}")
            emptyList()
        }
    }

    // 특정 유저의 게시글 조회
    suspend fun getPostsByUserId(userId: String): List<PostEntity> {
        return try {
            client.from("posts")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<PostEntity>()
        } catch (e: Exception) {
            println("Error fetching user posts: ${e.message}")
            emptyList()
        }
    }

    // 포스트 ID로 조회
    suspend fun getPostById(postId: Long): PostEntity? {
        return try {
            client.from("posts")
                .select {
                    filter {
                        eq("id", postId)
                    }
                }
                .decodeSingle<PostEntity>()
        } catch (e: Exception) {
            println("Error fetching post by id: ${e.message}")
            null
        }
    }

    // 게시글 수정
    suspend fun updatePost(
        postId: Long,
        title: String,
        content: String,
        subjectId: Int,
        startDate: String,
        endDate: String
    ): Boolean {
        return try {
            client.from("posts")
                .update({
                    set("title", title)
                    set("content", content)
                    set("subject_id", subjectId)
                    set("start_date", startDate)
                    set("end_date", endDate)
                }) {
                    filter {
                        eq("id", postId)
                    }
                }
            true
        } catch (e: Exception) {
            println("Error updating post: ${e.message}")
            false
        }
    }

    // 게시글 삭제 (신청자 없을 때만)
    suspend fun deletePost(postId: Long): Boolean {
        return try {
            // 1. 신청자 확인
            val appRepo = ApplicationRepository()
            val applications = appRepo.getApplicationsByPost(postId)

            if (applications.isNotEmpty()) {
                println("Cannot delete: Post has applicants")
                return false
            }

            // 2. 삭제
            client.from("posts")
                .delete {
                    filter {
                        eq("id", postId)
                    }
                }
            true
        } catch (e: Exception) {
            println("Error deleting post: ${e.message}")
            false
        }
    }

    // 과외 완전 종료 (Post + 채팅방 + Applications 전부 삭제)
    suspend fun deletePostCompletely(postId: Long): Boolean {
        return try {
            // 1. 관련 채팅방 삭제
            client.from("chat_rooms")
                .delete {
                    filter {
                        eq("post_id", postId)
                    }
                }

            // 2. 관련 applications 삭제
            client.from("applications")
                .delete {
                    filter {
                        eq("post_id", postId)
                    }
                }

            // 3. Post 삭제
            client.from("posts")
                .delete {
                    filter {
                        eq("id", postId)
                    }
                }

            true
        } catch (e: Exception) {
            println("Error deleting post completely: ${e.message}")
            false
        }
    }

}