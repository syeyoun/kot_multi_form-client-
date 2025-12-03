package org.example.testproject.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NotificationEntity(
    val id: Long = 0,
    @SerialName("user_id")
    val userId: String,
    val title: String,
    val message: String,
    val type: String,  // NEW_CHAT, QUEUE_UPDATE
    @SerialName("post_id")
    val postId: Long? = null,
    @SerialName("is_read")
    val isRead: Boolean = false,
    @SerialName("created_at")
    val createdAt: String = ""
)