package org.example.testproject.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
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
import org.example.testproject.models.MessageEntity
import org.example.testproject.models.PostEntity
import org.example.testproject.repository.ChatRepository
import org.example.testproject.models.subjects
import kotlinx.coroutines.launch
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.combinedClickable
import org.example.testproject.repository.ApplicationRepository
import org.example.testproject.repository.PostRepository
import org.example.testproject.utils.BackHandler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    chatRoom: ChatRoomEntity,
    post: PostEntity,
    currentUserId: String,
    onBackClick: () -> Unit,
    onLeaveChat: () -> Unit
) {
    val repository = remember { ChatRepository() }
    val chatRepo = remember { ChatRepository() }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val isCreator = currentUserId == chatRoom.creatorId
    val displayName = if (isCreator) chatRoom.creatorCustomName ?: post.title
    else chatRoom.applicantCustomName ?: post.title

    var messages by remember { mutableStateOf<List<MessageEntity>>(emptyList()) }
    var messageText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showPostInfo by remember { mutableStateOf(false) }
    var showLeaveDialog by remember { mutableStateOf(false) }
    BackHandler(onBack = onBackClick)

    // 메시지 로드 (Load messages)
    LaunchedEffect(chatRoom.id) {
        isLoading = true
        messages = repository.getMessages(chatRoom.id)
        isLoading = false

        // 실시간 구독! (Real-time subscription!)
        launch {
            repository.subscribeToMessages(chatRoom.id).collect { newMessage ->
                println("========== NEW MESSAGE: ${newMessage.message} ==========")
                if (!messages.any { it.id == newMessage.id }) {
                    messages = messages + newMessage
                    println("========== MESSAGE ADDED TO LIST ==========")
                }
            }
        }
    }

    // 새 메시지 오면 스크롤 (Scroll on new message)
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
//            listState.animateScrollToItem(messages.size - 1)
            listState.animateScrollToItem(0)

        }
    }

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF6200EE))
                .padding(vertical = 16.dp, horizontal = 16.dp)
        ) {
            Text(
                text = "← 뒤로(Back)",
                fontSize = 16.sp,
                color = Color.White,
                modifier = Modifier
                    .clickable { onBackClick() }
                    .align(Alignment.CenterStart)
            )

            // 게시글 정보 (클릭 가능) (Post info - clickable)
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .clickable { showPostInfo = !showPostInfo },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = displayName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                // 펼침 표시 (Expand indicator)
                Text(
                    text = if (showPostInfo) "▲" else "▼",
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }

            // 나가기 버튼 (우측) (Leave button - right side)
            Text(
                text = "🚪",
                fontSize = 20.sp,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .clickable { showLeaveDialog = true }
            )
        }

        // 접을 수 있는 정보창 (Collapsible info panel)
        AnimatedVisibility(visible = showPostInfo) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 제목 (Title)
                    InfoRow(
                        icon = "📝",
                        label = "제목(Title)",
                        value = post.title
                    )

                    Divider(color = Color(0xFFEEEEEE))

                    // 과목 (Subject)
                    InfoRow(
                        icon = "📚",
                        label = "과목(Subject)",
                        value = subjects.find { it.id == post.subjectId }?.name ?: "Unknown"
                    )

                    Divider(color = Color(0xFFEEEEEE))

                    // 날짜 (Period)
                    InfoRow(
                        icon = "📅",
                        label = "기간(Period)",
                        value = "${post.startDate} ~ ${post.endDate}"
                    )

                    Divider(color = Color(0xFFEEEEEE))

                    // 내용 (Description)
                    if (post.content.isNotEmpty()) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "📄 내용(Description)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF666666)
                            )
                            Text(
                                text = post.content,
                                fontSize = 14.sp,
                                color = Color(0xFF333333),
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }
        }

        // 메시지 리스트 (Message list)
        if (isLoading) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                reverseLayout = true,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                state = listState
            ) {
                items(messages.reversed()) { message ->
                    MessageBubble(
                        message = message,
                        isMine = message.senderId == currentUserId
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        // 입력창 (Input field)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = messageText,
                onValueChange = { messageText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("메시지 입력...(Type a message...)") },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF5F5F5),
                    unfocusedContainerColor = Color(0xFFF5F5F5),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(24.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (messageText.isNotBlank()) {
                        scope.launch {
                            val result = repository.sendMessage(
                                chatRoomId = chatRoom.id,
                                senderId = currentUserId,
                                message = messageText
                            )
                            if (result != null) {
                                messages = messages + result
                                messageText = ""
                            }
                        }
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFF6200EE), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "전송(Send)",
                    tint = Color.White
                )
            }
        }
    }

    // 나가기 확인 다이얼로그 (Leave confirmation dialog)
    if (showLeaveDialog) {
        AlertDialog(
            onDismissRequest = { showLeaveDialog = false },
            title = {
                Text(
                    text = "나가기 옵션(Leave Options)",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 옵션 1: 완료하고 나가기 (Option 1: Complete & Next)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showLeaveDialog = false
                                scope.launch {
                                    val appRepo = ApplicationRepository()
                                    val success = appRepo.nextStudent(post.id, currentUserId)
                                    if (success) {
                                        onBackClick()
                                    }
                                }
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFA726).copy(alpha = 0.1f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "👋",
                                fontSize = 32.sp
                            )
                            Column {
                                Text(
                                    text = "완료하고 나가기(Complete & Next)",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF333333)
                                )
                                Text(
                                    text = "현재 학생 완료, 다음 대기자 입장(Next student will be matched)",
                                    fontSize = 12.sp,
                                    color = Color(0xFF666666)
                                )
                            }
                        }
                    }

                    // 옵션 2: 과외 종료 (Option 2: Delete Post)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showLeaveDialog = false
                                scope.launch {
                                    val postRepo = PostRepository()
                                    val success = postRepo.deletePostCompletely(post.id)
                                    if (success) {
                                        onBackClick()
                                    }
                                }
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFF5252).copy(alpha = 0.1f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🏁",
                                fontSize = 32.sp
                            )
                            Column {
                                Text(
                                    text = "과외 종료(Delete Post)",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF333333)
                                )
                                Text(
                                    text = "게시글/채팅방/대기열 전부 삭제(Delete everything)",
                                    fontSize = 12.sp,
                                    color = Color(0xFF666666)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showLeaveDialog = false }) {
                    Text("취소(Cancel)", color = Color(0xFF999999))
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun MessageBubble(
    message: MessageEntity,
    isMine: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isMine) 16.dp else 4.dp,
                bottomEnd = if (isMine) 4.dp else 16.dp
            ),
            color = if (isMine) Color(0xFF6200EE) else Color.White,
            shadowElevation = 1.dp,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = message.message,
                    fontSize = 15.sp,
                    color = if (isMine) Color.White else Color(0xFF333333)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatTime(message.createdAt),
                    fontSize = 11.sp,
                    color = if (isMine) Color.White.copy(alpha = 0.7f) else Color(0xFF999999)
                )
            }
        }
    }
}

@Composable
fun InfoRow(
    icon: String,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = icon,
            fontSize = 20.sp
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color(0xFF999999)
            )
            Text(
                text = value,
                fontSize = 14.sp,
                color = Color(0xFF333333),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

fun formatTime(timestamp: String): String {
    // 간단한 시간 포맷 (HH:mm만 추출) (Simple time format - extract HH:mm only)
    return try {
        val parts = timestamp.split("T")
        if (parts.size > 1) {
            val time = parts[1].split(".")[0]
            time.substring(0, 5) // HH:mm
        } else {
            timestamp
        }
    } catch (e: Exception) {
        timestamp
    }
}