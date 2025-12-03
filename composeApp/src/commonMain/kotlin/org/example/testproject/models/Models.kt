package org.example.testproject.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class District(
    val id: Int,
    val name: String
)

data class Subject(
    val id: Int,
    val name: String
)

val seoulDistricts = listOf(
    District(1, "강남구(Gangnam-gu)"),
    District(2, "강동구(Gangdong-gu)"),
    District(3, "강북구(Gangbuk-gu)"),
    District(4, "강서구(Gangseo-gu)"),
    District(5, "관악구(Gwanak-gu)"),
    District(6, "광진구(Gwangjin-gu)"),
    District(7, "구로구(Guro-gu)"),
    District(8, "금천구(Geumcheon-gu)"),
    District(9, "노원구(Nowon-gu)"),
    District(10, "도봉구(Dobong-gu)"),
    District(11, "동대문구(Dongdaemun-gu)"),
    District(12, "동작구(Dongjak-gu)"),
    District(13, "마포구(Mapo-gu)"),
    District(14, "서대문구(Seodaemun-gu)"),
    District(15, "서초구(Seocho-gu)"),
    District(16, "성동구(Seongdong-gu)"),
    District(17, "성북구(Seongbuk-gu)"),
    District(18, "송파구(Songpa-gu)"),
    District(19, "양천구(Yangcheon-gu)"),
    District(20, "영등포구(Yeongdeungpo-gu)"),
    District(21, "용산구(Yongsan-gu)"),
    District(22, "은평구(Eunpyeong-gu)"),
    District(23, "종로구(Jongno-gu)"),
    District(24, "중구(Jung-gu)"),
    District(25, "중랑구(Jungnang-gu)")
)

val subjects = listOf(
    Subject(1, "국어(Korean)"),
    Subject(2, "영어(English)"),
    Subject(3, "수학(Mathematics)"),
    Subject(4, "사회(Social Studies)"),
    Subject(5, "과학(Science)")
)

data class Post(
    val id: Int,
    val district: District,
    val subject: Subject,
    val startDate: String,
    val endDate: String,
    val title: String,
    val content: String
)

data class ChatRoom(
    val id: Int,
    val post: Post,
    val lastMessage: String = "",
    val timestamp: String = ""
)

data class ChatMessage(
    val id: Int,
    val message: String,
    val isMine: Boolean,
    val timestamp: String
)

//@Serializable
//data class User(
//    val id: String = "",
//    val email: String,
//    val password: String = "",
//    val nickname: String,
//    val role: String = "STUDENT",
//    val approved: Boolean = false,  // 추가!
//    @SerialName("is_admin")
//    val isAdmin: Boolean = false,   // 추가!
//    @SerialName("created_at")
//    val createdAt: String = ""
//)

@Serializable
data class PostEntity(
    val id: Long = 0,
    @SerialName("user_id")
    val userId: String,
    @SerialName("district_id")
    val districtId: Int,
    @SerialName("subject_id")
    val subjectId: Int,
    val title: String,
    val content: String,
    @SerialName("start_date")
    val startDate: String,
    @SerialName("end_date")
    val endDate: String,
    @SerialName("created_at")
    val createdAt: String = ""
)

@Serializable
data class ChatRoomEntity(
    val id: Long = 0,
    @SerialName("post_id")
    val postId: Long,
    @SerialName("creator_id")
    val creatorId: String,
    @SerialName("applicant_id")
    val applicantId: String,
    @SerialName("created_at")
    val createdAt: String = "",
    @SerialName("custom_name")
    val customName: String? = null,
    @SerialName("creator_custom_name")
    val creatorCustomName: String? = null,
    @SerialName("applicant_custom_name")
    val applicantCustomName: String? = null
)

@Serializable
data class MessageEntity(
    val id: Long = 0,
    @SerialName("chat_room_id")
    val chatRoomId: Long,
    @SerialName("sender_id")
    val senderId: String,
    val message: String,
    @SerialName("created_at")
    val createdAt: String = ""
)

@Serializable
data class ApplicationEntity(
    val id: Long = 0,
    @SerialName("post_id")
    val postId: Long,
    @SerialName("user_id")
    val userId: String,
    val status: String = "WAITING",  // CHATTING, WAITING, COMPLETED
    @SerialName("queue_position")
    val queuePosition: Int,
    @SerialName("created_at")
    val createdAt: String = ""
)

@Serializable
data class User(
    val id: String = "",
    val email: String,
    val password: String = "",
    val nickname: String,
    val role: String,
    @SerialName("is_approved")
    val approved: Boolean = false,
    @SerialName("fcm_token")
    val fcmToken: String? = null,  // 추가!
    @SerialName("created_at")
    val createdAt: String = ""
)

//@Serializable
//data class User(
//    val id: String = "",
//    val email: String,
//    val password: String = "",
//    val nickname: String,
//    val role: String = "STUDENT",
//    val approved: Boolean = false,  // 추가!
//    @SerialName("is_admin")
//    val isAdmin: Boolean = false,   // 추가!
//    @SerialName("created_at")
//    val createdAt: String = ""
//)