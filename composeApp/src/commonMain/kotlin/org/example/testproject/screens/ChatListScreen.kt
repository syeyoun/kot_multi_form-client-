package org.example.testproject.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.example.testproject.models.ChatRoomEntity
import org.example.testproject.models.PostEntity
import org.example.testproject.repository.ChatRepository
import org.example.testproject.repository.PostRepository
import androidx.compose.foundation.combinedClickable

@Composable
fun ChatListScreen(
    currentUserId: String,
    onChatRoomClick: (ChatRoomEntity, PostEntity) -> Unit
) {
    val chatRepository = remember { ChatRepository() }
    val postRepository = remember { PostRepository() }
    val scope = rememberCoroutineScope()

    var chatRooms by remember { mutableStateOf<List<ChatRoomEntity>>(emptyList()) }
    var posts by remember { mutableStateOf<Map<Long, PostEntity>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(false) }
    var editingChatRoom by remember { mutableStateOf<ChatRoomEntity?>(null) }  // 추가! (Added!)

    // 채팅방 로드 (Load chat rooms)
    LaunchedEffect(Unit) {
        isLoading = true
        chatRooms = chatRepository.getMyChatRooms(currentUserId)

        // 각 채팅방의 게시글 정보 로드 (Load post info for each chat room)
        val allPosts = postRepository.getAllPosts()
        posts = allPosts.associateBy { it.id }

        isLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // 헤더 (Header)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF6200EE))
                .padding(vertical = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "채팅방(Chats)",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (chatRooms.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "아직 아무 채팅도 없습니다(No chats yet)",
                        fontSize = 18.sp,
                        color = Color(0xFF999999)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "신청 또는 게시글 작성해주세요!(Apply to posts to start chatting)",
                        fontSize = 14.sp,
                        color = Color(0xFFBBBBBB)
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(chatRooms) { chatRoom ->
                    val post = posts[chatRoom.postId]
                    if (post != null) {
                        ChatRoomItem(
                            chatRoom = chatRoom,
                            post = post,
                            currentUserId = currentUserId,
                            onClick = { onChatRoomClick(chatRoom, post) },
                            onLongClick = {
                                editingChatRoom = chatRoom  // 길게 누르면 수정.... (Long press to edit....)
                            }
                        )
                    }
                }
            }
        }
    }

    // 이름 수정 다이얼로그 (Edit name dialog)
    editingChatRoom?.let { chatRoom ->
        val post = posts[chatRoom.postId]
        if (post != null) {
            val isCreator = currentUserId == chatRoom.creatorId
            val currentName = if (isCreator) chatRoom.creatorCustomName ?: post.title
            else chatRoom.applicantCustomName ?: post.title

            EditChatNameDialog(
                currentName = currentName,
                onConfirm = { newName ->
                    scope.launch {
                        val success = chatRepository.updateChatRoomName(
                            chatRoomId = chatRoom.id,
                            customName = newName,
                            currentUserId = currentUserId,
                            creatorId = chatRoom.creatorId
                        )
                        if (success) {
                            // 리스트 새로고침 (Refresh list)
                            chatRooms = chatRepository.getMyChatRooms(currentUserId)
                        }
                    }
                    editingChatRoom = null
                },
                onDismiss = { editingChatRoom = null }
            )
        }
    }
}

@Composable
fun ChatRoomItem(
    chatRoom: ChatRoomEntity,
    post: PostEntity,
    currentUserId: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit  // 이거 추가! (Added this!)
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(Color.White)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 프로필 아이콘 (Profile icon)
        Box(
            modifier = Modifier
                .size(50.dp)
                .background(Color(0xFF6200EE).copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = post.title.first().toString(),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6200EE)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = post.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "터치시 열립니다!(Tap to open chat)",
                fontSize = 14.sp,
                color = Color(0xFF999999),
                maxLines = 1
            )
        }
    }
    HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
}

// EditChatNameDialog (동일)
@Composable
fun EditChatNameDialog(
    currentName: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var newName by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "채팅방 이름 수정(Edit Chat Name)",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                label = { Text("채팅방 이름(Chat Name)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF6200EE),
                    focusedLabelColor = Color(0xFF6200EE)
                )
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (newName.isNotBlank()) {
                        onConfirm(newName)
                    }
                },
                enabled = newName.isNotBlank()
            ) {
                Text("저장(Save)", color = Color(0xFF6200EE))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소(Cancel)", color = Color(0xFF999999))
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}