package org.example.testproject.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.testproject.models.District
import org.example.testproject.models.Post
import org.example.testproject.models.Subject
import org.example.testproject.models.subjects
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import org.example.testproject.models.PostEntity
import org.example.testproject.repository.PostRepository
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.rememberDatePickerState
import kotlinx.datetime.toLocalDateTime
import org.example.testproject.models.ApplicationEntity
import org.example.testproject.repository.ApplicationRepository
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import org.example.testproject.supabase.SupabaseClient
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostListScreen(
    selectedDistrict: District,
//    post = post,
    currentUserId: String,
    onApplyClick: (PostEntity) -> Unit,  // Post → PostEntity
//    onApplyClick = { onApplyClick(post) },
    onBackClick: () -> Unit
) {
    val repository = remember { PostRepository() }
    val scope = rememberCoroutineScope()

    var postList by remember { mutableStateOf<List<PostEntity>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var selectedSubject by remember { mutableStateOf<Subject?>(null) }

    // 날짜 필터 변수 (Date filter variables)
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    // 화면 진입시 게시글 로드 + 실시간 구독 (Load posts on screen entry + real-time subscription)
    LaunchedEffect(selectedDistrict.id) {
        isLoading = true
        postList = repository.getPostsByDistrict(selectedDistrict.id)
        isLoading = false

        // 실시간 구독 (applications 변경 감지) (Real-time subscription - detect applications changes)
        launch {
            val channel = SupabaseClient.client.realtime.channel("applications-district-${selectedDistrict.id}")

            channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "applications"
            }.collect { action ->
                println("=== APPLICATIONS CHANGED: $action ===")
                // applications 변경되면 게시글 리스트 새로고침 (Refresh post list when applications change)
                postList = repository.getPostsByDistrict(selectedDistrict.id)
            }

            channel.subscribe()
        }
    }

    // 필터링된 게시글 (Filtered posts)
    val filteredPosts = postList.filter { post ->
        val subjectMatch = selectedSubject == null || post.subjectId == selectedSubject?.id

        val dateMatch = when {
            startDate.isEmpty() && endDate.isEmpty() -> true
            startDate.isEmpty() -> post.startDate <= endDate
            endDate.isEmpty() -> post.endDate >= startDate
            else -> post.startDate <= endDate && post.endDate >= startDate
        }

        subjectMatch && dateMatch
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
            Text(
                text = "${selectedDistrict.name} 게시글(Posts)",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // 필터 영역 (Filter area)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "과목 필터(Subject Filter)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF666666)
            )

            Row(
                modifier = Modifier.fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    subject = null,
                    label = "전체(All)",
                    isSelected = selectedSubject == null,
                    onClick = { selectedSubject = null }
                )

                subjects.forEach { subject ->
                    FilterChip(
                        subject = subject,
                        label = subject.name,
                        isSelected = selectedSubject?.id == subject.id,
                        onClick = { selectedSubject = subject }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 시작 날짜 (Start date)
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .clickable { showStartDatePicker = true },
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "📅", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "시작(Start)",
                                fontSize = 10.sp,
                                color = Color(0xFF999999)
                            )
                            Text(
                                text = startDate.ifEmpty { "선택(Select)" },
                                fontSize = 12.sp,
                                color = if (startDate.isEmpty()) Color(0xFF999999) else Color(0xFF333333),
                                fontWeight = if (startDate.isEmpty()) FontWeight.Normal else FontWeight.Medium
                            )
                        }
                    }
                }

                // 종료 날짜 (End date)
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .clickable { showEndDatePicker = true },
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "📅", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "종료(End)",
                                fontSize = 10.sp,
                                color = Color(0xFF999999)
                            )
                            Text(
                                text = endDate.ifEmpty { "선택(Select)" },
                                fontSize = 12.sp,
                                color = if (endDate.isEmpty()) Color(0xFF999999) else Color(0xFF333333),
                                fontWeight = if (endDate.isEmpty()) FontWeight.Normal else FontWeight.Medium
                            )
                        }
                    }
                }

                // 초기화 버튼 (Reset button)
                if (startDate.isNotEmpty() || endDate.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .size(56.dp)
                            .clickable {
                                startDate = ""
                                endDate = ""
                            },
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFF5252)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "✕",
                                fontSize = 24.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Text(
            text = "총 ${filteredPosts.size}개 게시글(Total ${filteredPosts.size} posts)",
            fontSize = 14.sp,
            color = Color(0xFF666666),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (filteredPosts.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "게시글이 없습니다(No posts available)",
                    fontSize = 16.sp,
                    color = Color(0xFF999999)
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredPosts) { post ->
                    PostEntityCard(
                        post = post,
                        currentUserId = currentUserId,
                        onApplyClick = { onApplyClick(post) }
                    )
                }
            }
        }
    }

    // DatePicker Dialogs
    if (showStartDatePicker) {
        CustomDatePickerDialog(
            onDateSelected = { date ->
                startDate = date
                showStartDatePicker = false
            },
            onDismiss = { showStartDatePicker = false }
        )
    }

    if (showEndDatePicker) {
        CustomDatePickerDialog(
            onDateSelected = { date ->
                endDate = date
                showEndDatePicker = false
            },
            onDismiss = { showEndDatePicker = false }
        )
    }
}

@Composable
fun FilterChip(
    subject: Subject?,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF6200EE) else Color(0xFFF0F0F0)
        )
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = if (isSelected) Color.White else Color(0xFF666666),
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun PostCard(
    post: Post,
    onApplyClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 제목 (Title)
            Text(
                text = post.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333)
            )

            // 정보 (Info)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InfoChip(label = post.subject.name, color = Color(0xFF6200EE))
                InfoChip(label = "${post.startDate} ~ ${post.endDate}", color = Color(0xFF03A9F4))
            }

            // 내용 미리보기 (Content preview)
            if (post.content.isNotEmpty()) {
                Text(
                    text = post.content,
                    fontSize = 14.sp,
                    color = Color(0xFF666666),
                    maxLines = 2
                )
            }

            // 신청 버튼 (Apply button)
            Button(
                onClick = onApplyClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6200EE)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "신청(apply)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun InfoChip(label: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 12.sp,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun PostEntityCard(
    post: PostEntity,
    onApplyClick: () -> Unit,
    currentUserId: String
) {
    val appRepo = remember { ApplicationRepository() }
    val scope = rememberCoroutineScope()

    var applications by remember { mutableStateOf<List<ApplicationEntity>>(emptyList()) }
    var showDetailDialog by remember { mutableStateOf(false) }

    // 신청자 수 조회 + 실시간 구독 (Query applicant count + real-time subscription)
    LaunchedEffect(post.id) {
        applications = appRepo.getApplicationsByPost(post.id)

        // 실시간 구독 (Real-time subscription)
        launch {
            val channel = SupabaseClient.client.realtime.channel("applications-post-${post.id}")

            channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "applications"
                filter = "post_id=eq.${post.id}"
            }.collect { action ->
                println("=== POST ${post.id} APPLICATIONS CHANGED: $action ===")
                // applications 변경되면 다시 조회 (Re-query when applications change)
                applications = appRepo.getApplicationsByPost(post.id)
            }

            channel.subscribe()
        }
    }

    val myApplication = applications.find { it.userId == currentUserId }
    val queueSize = applications.size

    Card(
        modifier = Modifier.fillMaxWidth().clickable { showDetailDialog = true },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = post.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "대기열(Queue): $queueSize/3",
                    fontSize = 13.sp,
                    color = if (queueSize >= 3) Color(0xFFFF5252) else Color(0xFF999999)
                )

                // 내 상태 표시 (My status display)
                if (myApplication != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = when (myApplication.status) {
                            "CHATTING" -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                            "WAITING" -> Color(0xFFFFA726).copy(alpha = 0.1f)
                            else -> Color(0xFF999999).copy(alpha = 0.1f)
                        }
                    ) {
                        Text(
                            text = when (myApplication.status) {
                                "CHATTING" -> "💬 채팅중(Chatting)"
                                "WAITING" -> "⏳ 대기중(Waiting) #${myApplication.queuePosition}"
                                else -> "✓ 완료됨(Completed)"
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 12.sp,
                            color = when (myApplication.status) {
                                "CHATTING" -> Color(0xFF4CAF50)
                                "WAITING" -> Color(0xFFFFA726)
                                else -> Color(0xFF999999)
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // 자세히 보기 버튼 (추가!)
            TextButton(
                onClick = { showDetailDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color(0xFF6200EE)
                )
            ) {
                Text(
                    text = "자세히 보기 (View Details)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "▼", fontSize = 10.sp)
            }

            // Apply 버튼 (Apply button)
            Button(
                onClick = onApplyClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6200EE)
                ),
                shape = RoundedCornerShape(8.dp),
                enabled = myApplication == null && queueSize < 3
            ) {
                Text(
                    text = when {
                        myApplication != null -> "이미 신청함(Already Applied)"
                        queueSize >= 3 -> "대기열 가득참(Queue Full)"
                        else -> "신청(Apply)"
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    // 상세보기 다이얼로그 (Detail view dialog)
    if (showDetailDialog) {
        PostDetailDialog(
            post = post,
            onDismiss = { showDetailDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDatePickerDialog(
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()

    androidx.compose.material3.DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = kotlinx.datetime.Instant
                            .fromEpochMilliseconds(millis)
                            .toLocalDateTime(kotlinx.datetime.TimeZone.UTC)
                        val formatted = "${date.year}-${date.monthNumber.toString().padStart(2, '0')}-${date.dayOfMonth.toString().padStart(2, '0')}"
                        onDateSelected(formatted)
                    }
                }
            ) {
                Text("확인(OK)")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소(Cancel)")
            }
        }
    ) {
        androidx.compose.material3.DatePicker(
            state = datePickerState,
            colors = DatePickerDefaults.colors(
                selectedDayContainerColor = Color(0xFF6200EE)
            )
        )
    }
}

@Composable
fun PostDetailDialog(
    post: PostEntity,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = post.title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 과목 (Subject)
                PostInfoRow(
                    icon = "📚",
                    label = "과목(Subject)",
                    value = subjects.find { it.id == post.subjectId }?.name ?: "알 수 없음(Unknown)"
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
                } else {
                    Text(
                        text = "내용 없음(No description)",
                        fontSize = 14.sp,
                        color = Color(0xFF999999),
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("닫기(Close)", color = Color(0xFF6200EE))
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun PostInfoRow(
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

//package org.example.testproject.screens
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import org.example.testproject.models.District
//import org.example.testproject.models.Post
//import org.example.testproject.models.Subject
//import org.example.testproject.models.subjects
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.rememberCoroutineScope
//import kotlinx.coroutines.launch
//import org.example.testproject.models.PostEntity
//import org.example.testproject.repository.PostRepository
//import androidx.compose.material3.DatePicker
//import androidx.compose.material3.DatePickerDefaults
//import androidx.compose.material3.rememberDatePickerState
//import kotlinx.datetime.toLocalDateTime
//import org.example.testproject.models.ApplicationEntity
//import org.example.testproject.repository.ApplicationRepository
//import io.github.jan.supabase.realtime.PostgresAction
//import io.github.jan.supabase.realtime.channel
//import io.github.jan.supabase.realtime.postgresChangeFlow
//import io.github.jan.supabase.realtime.realtime
//import org.example.testproject.supabase.SupabaseClient
//import androidx.compose.foundation.horizontalScroll
//import androidx.compose.foundation.rememberScrollState
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun PostListScreen(
//    selectedDistrict: District,
////    post = post,
//    currentUserId: String,
//    onApplyClick: (PostEntity) -> Unit,  // Post → PostEntity
////    onApplyClick = { onApplyClick(post) },
//    onBackClick: () -> Unit
//) {
//    val repository = remember { PostRepository() }
//    val scope = rememberCoroutineScope()
//
//    var postList by remember { mutableStateOf<List<PostEntity>>(emptyList()) }
//    var isLoading by remember { mutableStateOf(false) }
//    var selectedSubject by remember { mutableStateOf<Subject?>(null) }
//
//    // 날짜 필터 변수 수정
//    var startDate by remember { mutableStateOf("") }
//    var endDate by remember { mutableStateOf("") }
//    var showStartDatePicker by remember { mutableStateOf(false) }
//    var showEndDatePicker by remember { mutableStateOf(false) }
//
//    // 화면 진입시 게시글 로드 + 실시간 구독
//    LaunchedEffect(selectedDistrict.id) {
//        isLoading = true
//        postList = repository.getPostsByDistrict(selectedDistrict.id)
//        isLoading = false
//
//        // 실시간 구독 (applications 변경 감지)
//        launch {
//            val channel = SupabaseClient.client.realtime.channel("applications-district-${selectedDistrict.id}")
//
//            channel.postgresChangeFlow<PostgresAction>(schema = "public") {
//                table = "applications"
//            }.collect { action ->
//                println("=== APPLICATIONS CHANGED: $action ===")
//                // applications 변경되면 게시글 리스트 새로고침
//                postList = repository.getPostsByDistrict(selectedDistrict.id)
//            }
//
//            channel.subscribe()
//        }
//    }
//
//    // 필터링된 게시글 수정
//    val filteredPosts = postList.filter { post ->
//        val subjectMatch = selectedSubject == null || post.subjectId == selectedSubject?.id
//
//        val dateMatch = when {
//            startDate.isEmpty() && endDate.isEmpty() -> true
//            startDate.isEmpty() -> post.startDate <= endDate
//            endDate.isEmpty() -> post.endDate >= startDate
//            else -> post.startDate <= endDate && post.endDate >= startDate
//        }
//
//        subjectMatch && dateMatch
//    }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color(0xFFF5F5F5))
//    ) {
//        // 헤더
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//                .background(Color(0xFF6200EE))
//                .padding(vertical = 16.dp, horizontal = 16.dp)
//        ) {
//            Text(
//                text = "← Back",
//                fontSize = 16.sp,
//                color = Color.White,
//                modifier = Modifier
//                    .clickable { onBackClick() }
//                    .align(Alignment.CenterStart)
//            )
//            Text(
//                text = "${selectedDistrict.name} Posts",
//                fontSize = 20.sp,
//                fontWeight = FontWeight.Bold,
//                color = Color.White,
//                modifier = Modifier.align(Alignment.Center)
//            )
//        }
//
//        // 필터 영역
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .background(Color.White)
//                .padding(16.dp),
//            verticalArrangement = Arrangement.spacedBy(12.dp)
//        ) {
//            Text(
//                text = "Subject Filter",
//                fontSize = 14.sp,
//                fontWeight = FontWeight.Bold,
//                color = Color(0xFF666666)
//            )
//
//            Row(
//                modifier = Modifier.fillMaxWidth()
//                .horizontalScroll(rememberScrollState()),  // 추가!
//                horizontalArrangement = Arrangement.spacedBy(8.dp)
//            ) {
//                FilterChip(
//                    subject = null,
//                    label = "All",
//                    isSelected = selectedSubject == null,
//                    onClick = { selectedSubject = null }
//                )
//
//                subjects.forEach { subject ->
//                    FilterChip(
//                        subject = subject,
//                        label = subject.name,
//                        isSelected = selectedSubject?.id == subject.id,
//                        onClick = { selectedSubject = subject }
//                    )
//                }
//            }
//
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.spacedBy(8.dp)
//            ) {
//                // 시작 날짜
//                Card(
//                    modifier = Modifier
//                        .weight(1f)
//                        .height(56.dp)
//                        .clickable { showStartDatePicker = true },
//                    shape = RoundedCornerShape(8.dp),
//                    colors = CardDefaults.cardColors(containerColor = Color.White),
//                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
//                ) {
//                    Row(
//                        modifier = Modifier
//                            .fillMaxSize()
//                            .padding(horizontal = 12.dp),
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Text(text = "📅", fontSize = 16.sp)
//                        Spacer(modifier = Modifier.width(4.dp))
//                        Column(modifier = Modifier.weight(1f)) {
//                            Text(
//                                text = "Start",
//                                fontSize = 10.sp,
//                                color = Color(0xFF999999)
//                            )
//                            Text(
//                                text = startDate.ifEmpty { "Select" },
//                                fontSize = 12.sp,
//                                color = if (startDate.isEmpty()) Color(0xFF999999) else Color(0xFF333333),
//                                fontWeight = if (startDate.isEmpty()) FontWeight.Normal else FontWeight.Medium
//                            )
//                        }
//                    }
//                }
//
//                // 종료 날짜
//                Card(
//                    modifier = Modifier
//                        .weight(1f)
//                        .height(56.dp)
//                        .clickable { showEndDatePicker = true },
//                    shape = RoundedCornerShape(8.dp),
//                    colors = CardDefaults.cardColors(containerColor = Color.White),
//                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
//                ) {
//                    Row(
//                        modifier = Modifier
//                            .fillMaxSize()
//                            .padding(horizontal = 12.dp),
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Text(text = "📅", fontSize = 16.sp)
//                        Spacer(modifier = Modifier.width(4.dp))
//                        Column(modifier = Modifier.weight(1f)) {
//                            Text(
//                                text = "End",
//                                fontSize = 10.sp,
//                                color = Color(0xFF999999)
//                            )
//                            Text(
//                                text = endDate.ifEmpty { "Select" },
//                                fontSize = 12.sp,
//                                color = if (endDate.isEmpty()) Color(0xFF999999) else Color(0xFF333333),
//                                fontWeight = if (endDate.isEmpty()) FontWeight.Normal else FontWeight.Medium
//                            )
//                        }
//                    }
//                }
//
//                // 초기화 버튼
//                if (startDate.isNotEmpty() || endDate.isNotEmpty()) {
//                    Card(
//                        modifier = Modifier
//                            .size(56.dp)
//                            .clickable {
//                                startDate = ""
//                                endDate = ""
//                            },
//                        shape = RoundedCornerShape(8.dp),
//                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFF5252)),
//                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
//                    ) {
//                        Box(
//                            modifier = Modifier.fillMaxSize(),
//                            contentAlignment = Alignment.Center
//                        ) {
//                            Text(
//                                text = "✕",
//                                fontSize = 24.sp,
//                                color = Color.White,
//                                fontWeight = FontWeight.Bold
//                            )
//                        }
//                    }
//                }
//            }
//        }
//
//        Text(
//            text = "Total ${filteredPosts.size} posts",
//            fontSize = 14.sp,
//            color = Color(0xFF666666),
//            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
//        )
//
//        if (isLoading) {
//            Box(
//                modifier = Modifier.fillMaxSize(),
//                contentAlignment = Alignment.Center
//            ) {
//                CircularProgressIndicator()
//            }
//        } else if (filteredPosts.isEmpty()) {
//            Box(
//                modifier = Modifier.fillMaxSize(),
//                contentAlignment = Alignment.Center
//            ) {
//                Text(
//                    text = "No posts available",
//                    fontSize = 16.sp,
//                    color = Color(0xFF999999)
//                )
//            }
//        } else {
//            LazyColumn(
//                contentPadding = PaddingValues(16.dp),
//                verticalArrangement = Arrangement.spacedBy(12.dp)
//            ) {
//                items(filteredPosts) { post ->
//                    PostEntityCard(
//                        post = post,
//                        currentUserId = currentUserId,  // 추가!
//                        onApplyClick = { onApplyClick(post) }
//                    )
//                }
//            }
//        }
//    }
//
//    // DatePicker Dialogs
//    if (showStartDatePicker) {
//        CustomDatePickerDialog(
//            onDateSelected = { date ->
//                startDate = date
//                showStartDatePicker = false
//            },
//            onDismiss = { showStartDatePicker = false }
//        )
//    }
//
//    if (showEndDatePicker) {
//        CustomDatePickerDialog(
//            onDateSelected = { date ->
//                endDate = date
//                showEndDatePicker = false
//            },
//            onDismiss = { showEndDatePicker = false }
//        )
//    }
//}
//
//@Composable
//fun FilterChip(
//    subject: Subject?,
//    label: String,
//    isSelected: Boolean,
//    onClick: () -> Unit
//) {
//    Card(
//        modifier = Modifier.clickable(onClick = onClick),
//        shape = RoundedCornerShape(20.dp),
//        colors = CardDefaults.cardColors(
//            containerColor = if (isSelected) Color(0xFF6200EE) else Color(0xFFF0F0F0)
//        )
//    ) {
//        Text(
//            text = label,
//            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
//            color = if (isSelected) Color.White else Color(0xFF666666),
//            fontSize = 13.sp,
//            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
//        )
//    }
//}
//
//@Composable
//fun PostCard(
//    post: Post,
//    onApplyClick: () -> Unit
//) {
//    Card(
//        modifier = Modifier.fillMaxWidth(),
//        shape = RoundedCornerShape(12.dp),
//        colors = CardDefaults.cardColors(containerColor = Color.White),
//        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp),
//            verticalArrangement = Arrangement.spacedBy(12.dp)
//        ) {
//            // 제목
//            Text(
//                text = post.title,
//                fontSize = 18.sp,
//                fontWeight = FontWeight.Bold,
//                color = Color(0xFF333333)
//            )
//
//            // 정보
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.spacedBy(12.dp)
//            ) {
//                InfoChip(label = post.subject.name, color = Color(0xFF6200EE))
//                InfoChip(label = "${post.startDate} ~ ${post.endDate}", color = Color(0xFF03A9F4))
//            }
//
//            // 내용 미리보기
//            if (post.content.isNotEmpty()) {
//                Text(
//                    text = post.content,
//                    fontSize = 14.sp,
//                    color = Color(0xFF666666),
//                    maxLines = 2
//                )
//            }
//
//            // 신청 버튼
//            Button(
//                onClick = onApplyClick,
//                modifier = Modifier.fillMaxWidth(),
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = Color(0xFF6200EE)
//                ),
//                shape = RoundedCornerShape(8.dp)
//            ) {
//                Text(
//                    text = "apply",
//                    fontSize = 16.sp,
//                    fontWeight = FontWeight.Bold
//                )
//            }
//        }
//    }
//}
//
//@Composable
//fun InfoChip(label: String, color: Color) {
//    Surface(
//        shape = RoundedCornerShape(6.dp),
//        color = color.copy(alpha = 0.1f)
//    ) {
//        Text(
//            text = label,
//            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
//            fontSize = 12.sp,
//            color = color,
//            fontWeight = FontWeight.Medium
//        )
//    }
//}
//
//@Composable
//fun PostEntityCard(
//    post: PostEntity,
//    onApplyClick: () -> Unit,
//    currentUserId: String  // 추가!
//) {
//    val appRepo = remember { ApplicationRepository() }
//    val scope = rememberCoroutineScope()
//
//    var applications by remember { mutableStateOf<List<ApplicationEntity>>(emptyList()) }
//    var showDetailDialog by remember { mutableStateOf(false) }  // 추가!
//
//    // 신청자 수 조회 + 실시간 구독
//    LaunchedEffect(post.id) {
//        applications = appRepo.getApplicationsByPost(post.id)
//
//        // 실시간 구독
//        launch {
//            val channel = SupabaseClient.client.realtime.channel("applications-post-${post.id}")
//
//            channel.postgresChangeFlow<PostgresAction>(schema = "public") {
//                table = "applications"
//                filter = "post_id=eq.${post.id}"
//            }.collect { action ->
//                println("=== POST ${post.id} APPLICATIONS CHANGED: $action ===")
//                // applications 변경되면 다시 조회
//                applications = appRepo.getApplicationsByPost(post.id)
//            }
//
//            channel.subscribe()
//        }
//    }
//
//    val myApplication = applications.find { it.userId == currentUserId }
//    val queueSize = applications.size
//
//    Card(
//        modifier = Modifier.fillMaxWidth().clickable { showDetailDialog = true },  // 추가!
//        shape = RoundedCornerShape(12.dp),
//        colors = CardDefaults.cardColors(containerColor = Color.White),
//        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp),
//            verticalArrangement = Arrangement.spacedBy(12.dp)
//        ) {
//            Text(
//                text = post.title,
//                fontSize = 18.sp,
//                fontWeight = FontWeight.Bold,
//                color = Color(0xFF333333)
//            )
//
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Text(
//                    text = "Queue: $queueSize/3",
//                    fontSize = 13.sp,
//                    color = if (queueSize >= 3) Color(0xFFFF5252) else Color(0xFF999999)
//                )
//
//                // 내 상태 표시
//                if (myApplication != null) {
//                    Surface(
//                        shape = RoundedCornerShape(12.dp),
//                        color = when (myApplication.status) {
//                            "CHATTING" -> Color(0xFF4CAF50).copy(alpha = 0.1f)
//                            "WAITING" -> Color(0xFFFFA726).copy(alpha = 0.1f)
//                            else -> Color(0xFF999999).copy(alpha = 0.1f)
//                        }
//                    ) {
//                        Text(
//                            text = when (myApplication.status) {
//                                "CHATTING" -> "💬 Chatting"
//                                "WAITING" -> "⏳ Waiting #${myApplication.queuePosition}"
//                                else -> "✓ Completed"
//                            },
//                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
//                            fontSize = 12.sp,
//                            color = when (myApplication.status) {
//                                "CHATTING" -> Color(0xFF4CAF50)
//                                "WAITING" -> Color(0xFFFFA726)
//                                else -> Color(0xFF999999)
//                            },
//                            fontWeight = FontWeight.Bold
//                        )
//                    }
//                }
//            }
//
//            // Apply 버튼
//            Button(
//                onClick = onApplyClick,
//                modifier = Modifier.fillMaxWidth(),
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = Color(0xFF6200EE)
//                ),
//                shape = RoundedCornerShape(8.dp),
//                enabled = myApplication == null && queueSize < 3  // 조건!
//            ) {
//                Text(
//                    text = when {
//                        myApplication != null -> "Already Applied"
//                        queueSize >= 3 -> "Queue Full"
//                        else -> "Apply"
//                    },
//                    fontSize = 16.sp,
//                    fontWeight = FontWeight.Bold
//                )
//            }
//        }
//    }
//
//    // 상세보기 다이얼로그 (추가!)
//    if (showDetailDialog) {
//        PostDetailDialog(
//            post = post,
//            onDismiss = { showDetailDialog = false }
//        )
//    }
//}
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun CustomDatePickerDialog(
//    onDateSelected: (String) -> Unit,
//    onDismiss: () -> Unit
//) {
//    val datePickerState = rememberDatePickerState()
//
//    androidx.compose.material3.DatePickerDialog(
//        onDismissRequest = onDismiss,
//        confirmButton = {
//            TextButton(
//                onClick = {
//                    datePickerState.selectedDateMillis?.let { millis ->
//                        val date = kotlinx.datetime.Instant
//                            .fromEpochMilliseconds(millis)
//                            .toLocalDateTime(kotlinx.datetime.TimeZone.UTC)
//                        val formatted = "${date.year}-${date.monthNumber.toString().padStart(2, '0')}-${date.dayOfMonth.toString().padStart(2, '0')}"
//                        onDateSelected(formatted)
//                    }
//                }
//            ) {
//                Text("OK")
//            }
//        },
//        dismissButton = {
//            TextButton(onClick = onDismiss) {
//                Text("Cancel")
//            }
//        }
//    ) {
//        androidx.compose.material3.DatePicker(
//            state = datePickerState,
//            colors = DatePickerDefaults.colors(
//                selectedDayContainerColor = Color(0xFF6200EE)
//            )
//        )
//    }
//}
//
//@Composable
//fun PostDetailDialog(
//    post: PostEntity,
//    onDismiss: () -> Unit
//) {
//    AlertDialog(
//        onDismissRequest = onDismiss,
//        title = {
//            Text(
//                text = post.title,
//                fontSize = 20.sp,
//                fontWeight = FontWeight.Bold
//            )
//        },
//        text = {
//            Column(
//                modifier = Modifier.fillMaxWidth(),
//                verticalArrangement = Arrangement.spacedBy(16.dp)
//            ) {
//                // 과목
//                PostInfoRow(
//                    icon = "📚",
//                    label = "Subject",
//                    value = subjects.find { it.id == post.subjectId }?.name ?: "Unknown"
//                )
//
//                Divider(color = Color(0xFFEEEEEE))
//
//                // 날짜
//                InfoRow(
//                    icon = "📅",
//                    label = "Period",
//                    value = "${post.startDate} ~ ${post.endDate}"
//                )
//
//                Divider(color = Color(0xFFEEEEEE))
//
//                // 내용
//                if (post.content.isNotEmpty()) {
//                    Column(
//                        verticalArrangement = Arrangement.spacedBy(4.dp)
//                    ) {
//                        Text(
//                            text = "📄 Description",
//                            fontSize = 14.sp,
//                            fontWeight = FontWeight.Bold,
//                            color = Color(0xFF666666)
//                        )
//                        Text(
//                            text = post.content,
//                            fontSize = 14.sp,
//                            color = Color(0xFF333333),
//                            lineHeight = 20.sp
//                        )
//                    }
//                } else {
//                    Text(
//                        text = "내용 없음 (No description)",
//                        fontSize = 14.sp,
//                        color = Color(0xFF999999),
//                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
//                    )
//                }
//            }
//        },
//        confirmButton = {
//            TextButton(onClick = onDismiss) {
//                Text("닫기 (Close)", color = Color(0xFF6200EE))
//            }
//        },
//        shape = RoundedCornerShape(16.dp)
//    )
//}
//
//@Composable
//fun PostInfoRow(
//    icon: String,
//    label: String,
//    value: String
//) {
//    Row(
//        modifier = Modifier.fillMaxWidth(),
//        horizontalArrangement = Arrangement.spacedBy(12.dp),
//        verticalAlignment = Alignment.Top
//    ) {
//        Text(
//            text = icon,
//            fontSize = 20.sp
//        )
//
//        Column(
//            modifier = Modifier.weight(1f),
//            verticalArrangement = Arrangement.spacedBy(2.dp)
//        ) {
//            Text(
//                text = label,
//                fontSize = 12.sp,
//                color = Color(0xFF999999)
//            )
//            Text(
//                text = value,
//                fontSize = 14.sp,
//                color = Color(0xFF333333),
//                fontWeight = FontWeight.Medium
//            )
//        }
//    }
//}
//
//
