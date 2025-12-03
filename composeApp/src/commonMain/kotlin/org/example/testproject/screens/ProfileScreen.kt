package org.example.testproject.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.example.testproject.models.PostEntity
import org.example.testproject.models.subjects
import org.example.testproject.repository.PostRepository
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import kotlinx.coroutines.launch
import org.example.testproject.models.ApplicationEntity
import org.example.testproject.repository.ApplicationRepository

@Composable
fun ProfileScreen(
    currentUserId: String,
    userRole: String,
    nickname: String,
    email: String,
    onLogout: () -> Unit
) {
    val repository = remember { PostRepository() }
    val scope = rememberCoroutineScope()

    var myPosts by remember { mutableStateOf<List<PostEntity>>(emptyList()) }
    var myApplications by remember { mutableStateOf<List<ApplicationEntity>>(emptyList()) }
    var applicationPosts by remember { mutableStateOf<Map<Long, PostEntity>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(false) }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var postToDelete by remember { mutableStateOf<PostEntity?>(null) }
    var deleteError by remember { mutableStateOf("") }

    var showLogoutDialog by remember { mutableStateOf(false) }  // 추가!

    // 데이터 로드
    LaunchedEffect(currentUserId, userRole) {
        isLoading = true
        println("=== LOADING PROFILE: userId=$currentUserId, role=$userRole ===")

        if (userRole == "TEACHER") {
            myPosts = repository.getPostsByUserId(currentUserId)
            println("=== LOADED ${myPosts.size} POSTS ===")
        } else {  // STUDENT
            val appRepo = ApplicationRepository()
            myApplications = appRepo.getApplicationsByUser(currentUserId)
            println("=== LOADED ${myApplications.size} APPLICATIONS ===")

            val postMap = mutableMapOf<Long, PostEntity>()
            myApplications.forEach { app ->
                val post = repository.getPostById(app.postId)
                if (post != null) {
                    postMap[app.postId] = post
                }
            }
            applicationPosts = postMap
            println("=== LOADED ${applicationPosts.size} POST DETAILS ===")
        }
        isLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // 헤더
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF6200EE))
                .padding(vertical = 24.dp, horizontal = 16.dp)
        ) {
            Text(
                text = "프로필(Profile)",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )

            IconButton(
                onClick = { showLogoutDialog = true },  // 수정!
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = "로그아웃(Logout)",
                    tint = Color.White
                )
            }
        }

        // 로그아웃 확인 다이얼로그 (추가!)
        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                title = {
                    Text(
                        text = "로그아웃 (Logout)",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text("정말 로그아웃 하시겠습니까? (Are you sure you want to logout?)")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showLogoutDialog = false
                            onLogout()
                        }
                    ) {
                        Text("로그아웃 (Logout)", color = Color(0xFFFF5252))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutDialog = false }) {
                        Text("취소 (Cancel)", color = Color(0xFF999999))
                    }
                },
                shape = RoundedCornerShape(16.dp)
            )
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 프로필 정보 카드
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 프로필 아이콘
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(Color(0xFF6200EE).copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = nickname.firstOrNull()?.toString()?.uppercase() ?: "U",
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6200EE)
                            )
                        }

                        Text(
                            text = nickname,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333)
                        )

                        Text(
                            text = email,
                            fontSize = 14.sp,
                            color = Color(0xFF666666)
                        )

                        // 역할 뱃지
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (userRole == "TEACHER") Color(0xFF6200EE) else Color(0xFF03A9F4)
                        ) {
                            Text(
                                text = if (userRole == "TEACHER") "교사(Teacher)" else "학생(Student)",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                fontSize = 14.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Teacher: 내 게시글 목록
            if (userRole == "TEACHER") {
                item {
                    Text(
                        text = "내 게시글(My Posts) (${myPosts.size})",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )
                }

                if (isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                } else if (myPosts.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "아직 게시글이 없습니다(No posts yet)",
                                    fontSize = 16.sp,
                                    color = Color(0xFF999999)
                                )
                            }
                        }
                    }
                } else {
                    items(myPosts) { post ->
                        MyPostCard(
                            post = post,
                            onEditClick = { editPost ->
                                println("Edit post: ${editPost.id}")
                            },
                            onDeleteClick = { deletePost ->
                                postToDelete = deletePost
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }

            // Student: 신청 목록
            if (userRole == "STUDENT") {
                item {
                    Text(
                        text = "내 신청 목록(My Applications)",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )
                }

                if (isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                } else if (myApplications.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "아직 신청 내역이 없습니다(No applications yet)",
                                    fontSize = 16.sp,
                                    color = Color(0xFF999999)
                                )
                            }
                        }
                    }
                } else {
                    items(myApplications) { application ->
                        ApplicationCard(
                            application = application,
                            post = applicationPosts[application.postId],
                            onCancelClick = { app ->
                                scope.launch {
                                    val appRepo = ApplicationRepository()
                                    val success = appRepo.cancelApplication(app.id)
                                    if (success) {
                                        myApplications = myApplications.filter { it.id != app.id }
                                        println("Application canceled successfully")
                                    } else {
                                        println("Failed to cancel application")
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MyPostCard(
    post: PostEntity,
    onEditClick: (PostEntity) -> Unit,
    onDeleteClick: (PostEntity) -> Unit
) {
    val subject = subjects.find { it.id == post.subjectId }
    val appRepo = remember { ApplicationRepository() }
    val scope = rememberCoroutineScope()

    var applicantCount by remember { mutableStateOf(0) }

    LaunchedEffect(post.id) {
        val applications = appRepo.getApplicationsByPost(post.id)
        applicantCount = applications.size
    }

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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = post.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333),
                    modifier = Modifier.weight(1f)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = { onEditClick(post) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "수정(Edit)",
                            tint = Color(0xFF6200EE),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = { onDeleteClick(post) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "삭제(Delete)",
                            tint = Color(0xFFFF5252),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF6200EE).copy(alpha = 0.1f)
                ) {
                    Text(
                        text = subject?.name ?: "Unknown",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        color = Color(0xFF6200EE),
                        fontWeight = FontWeight.Medium
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF03A9F4).copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "${post.startDate} ~ ${post.endDate}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        color = Color(0xFF03A9F4),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (post.content.isNotEmpty()) {
                Text(
                    text = post.content,
                    fontSize = 14.sp,
                    color = Color(0xFF666666),
                    maxLines = 2
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "신청자(Applicants): $applicantCount/3",
                    fontSize = 13.sp,
                    color = if (applicantCount >= 3) Color(0xFFFF5252) else Color(0xFF999999),
                    fontWeight = if (applicantCount > 0) FontWeight.Bold else FontWeight.Normal
                )

                if (applicantCount > 0) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFF5252).copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "⚠️ 신청자 있음(Has applicants)",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 11.sp,
                            color = Color(0xFFFF5252),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ApplicationCard(
    application: ApplicationEntity,
    post: PostEntity?,
    onCancelClick: (ApplicationEntity) -> Unit
) {
    if (post == null) return

    val subject = subjects.find { it.id == post.subjectId }

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = post.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333),
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = when (application.status) {
                        "CHATTING" -> Color(0xFF4CAF50).copy(alpha = 0.15f)
                        "WAITING" -> Color(0xFFFFA726).copy(alpha = 0.15f)
                        else -> Color(0xFF999999).copy(alpha = 0.15f)
                    }
                ) {
                    Text(
                        text = when (application.status) {
                            "CHATTING" -> "💬 채팅중(Chatting)"
                            "WAITING" -> "⏳ 대기중(Queue) #${application.queuePosition}"
                            else -> "✓ 완료(Completed)"
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 13.sp,
                        color = when (application.status) {
                            "CHATTING" -> Color(0xFF4CAF50)
                            "WAITING" -> Color(0xFFFFA726)
                            else -> Color(0xFF999999)
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF6200EE).copy(alpha = 0.1f)
                ) {
                    Text(
                        text = subject?.name ?: "Unknown",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        color = Color(0xFF6200EE),
                        fontWeight = FontWeight.Medium
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF03A9F4).copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "${post.startDate} ~ ${post.endDate}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        color = Color(0xFF03A9F4),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (post.content.isNotEmpty()) {
                Text(
                    text = post.content,
                    fontSize = 14.sp,
                    color = Color(0xFF666666),
                    maxLines = 2
                )
            }

            Text(
                text = "신청일(Applied): ${application.createdAt}",
                fontSize = 12.sp,
                color = Color(0xFF999999)
            )

            Button(
                onClick = { onCancelClick(application) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF5252)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "신청 취소(Cancel Application)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

//@Composable
//fun ProfileScreen(
//    currentUserId: String,
//    userRole: String,
//    nickname: String,
//    email: String,
//    onLogout: () -> Unit
//) {
//    val repository = remember { PostRepository() }
//    val scope = rememberCoroutineScope()
//
//    var myPosts by remember { mutableStateOf<List<PostEntity>>(emptyList()) }
//    var myApplications by remember { mutableStateOf<List<ApplicationEntity>>(emptyList()) }  // 추가!
//    var applicationPosts by remember { mutableStateOf<Map<Long, PostEntity>>(emptyMap()) }  // 추가!
//    var isLoading by remember { mutableStateOf(false) }
//
//    var showDeleteDialog by remember { mutableStateOf(false) }  // 추가!
//    var postToDelete by remember { mutableStateOf<PostEntity?>(null) }  // 추가!
//    var deleteError by remember { mutableStateOf("") }  // 추가!
//
//// 데이터 로드
//    LaunchedEffect(currentUserId, userRole) {
//        isLoading = true
//        println("=== LOADING PROFILE: userId=$currentUserId, role=$userRole ===")
//
//        if (userRole == "TEACHER") {
//            myPosts = repository.getPostsByUserId(currentUserId)
//            println("=== LOADED ${myPosts.size} POSTS ===")
//        } else {  // STUDENT
//            val appRepo = ApplicationRepository()
//            myApplications = appRepo.getApplicationsByUser(currentUserId)
//            println("=== LOADED ${myApplications.size} APPLICATIONS ===")
//
//            // 각 신청에 해당하는 게시글 정보 가져오기
//            val postMap = mutableMapOf<Long, PostEntity>()
//            myApplications.forEach { app ->
//                val post = repository.getPostById(app.postId)
//                if (post != null) {
//                    postMap[app.postId] = post
//                }
//            }
//            applicationPosts = postMap
//            println("=== LOADED ${applicationPosts.size} POST DETAILS ===")
//        }
//        isLoading = false
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
//                .padding(vertical = 24.dp, horizontal = 16.dp)
//        ) {
//            Text(
//                text = "Profile",
//                fontSize = 24.sp,
//                fontWeight = FontWeight.Bold,
//                color = Color.White,
//                modifier = Modifier.align(Alignment.Center)
//            )
//
//            IconButton(
//                onClick = onLogout,
//                modifier = Modifier.align(Alignment.CenterEnd)
//            ) {
//                Icon(
//                    imageVector = Icons.Default.ExitToApp,
//                    contentDescription = "Logout",
//                    tint = Color.White
//                )
//            }
//        }
//
//        LazyColumn(
//            contentPadding = PaddingValues(16.dp),
//            verticalArrangement = Arrangement.spacedBy(16.dp)
//        ) {
//            // 프로필 정보 카드
//            item {
//                Card(
//                    modifier = Modifier.fillMaxWidth(),
//                    shape = RoundedCornerShape(16.dp),
//                    colors = CardDefaults.cardColors(containerColor = Color.White),
//                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
//                ) {
//                    Column(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(24.dp),
//                        horizontalAlignment = Alignment.CenterHorizontally,
//                        verticalArrangement = Arrangement.spacedBy(12.dp)
//                    ) {
//                        // 프로필 아이콘
//                        Box(
//                            modifier = Modifier
//                                .size(80.dp)
//                                .background(Color(0xFF6200EE).copy(alpha = 0.2f), CircleShape),
//                            contentAlignment = Alignment.Center
//                        ) {
//                            Text(
//                                text = nickname.firstOrNull()?.toString()?.uppercase() ?: "U",
//                                fontSize = 36.sp,
//                                fontWeight = FontWeight.Bold,
//                                color = Color(0xFF6200EE)
//                            )
//                        }
//
//                        Text(
//                            text = nickname,
//                            fontSize = 24.sp,
//                            fontWeight = FontWeight.Bold,
//                            color = Color(0xFF333333)
//                        )
//
//                        Text(
//                            text = email,
//                            fontSize = 14.sp,
//                            color = Color(0xFF666666)
//                        )
//
//                        // 역할 뱃지
//                        Surface(
//                            shape = RoundedCornerShape(20.dp),
//                            color = if (userRole == "TEACHER") Color(0xFF6200EE) else Color(0xFF03A9F4)
//                        ) {
//                            Text(
//                                text = if (userRole == "TEACHER") "Teacher" else "Student",
//                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
//                                fontSize = 14.sp,
//                                color = Color.White,
//                                fontWeight = FontWeight.Bold
//                            )
//                        }
//                    }
//                }
//            }
//
//            // Teacher: 내 게시글 목록
//            if (userRole == "TEACHER") {
//                item {
//                    Text(
//                        text = "My Posts (${myPosts.size})",
//                        fontSize = 20.sp,
//                        fontWeight = FontWeight.Bold,
//                        color = Color(0xFF333333)
//                    )
//                }
//
//                if (isLoading) {
//                    item {
//                        Box(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .height(200.dp),
//                            contentAlignment = Alignment.Center
//                        ) {
//                            CircularProgressIndicator()
//                        }
//                    }
//                } else if (myPosts.isEmpty()) {
//                    item {
//                        Card(
//                            modifier = Modifier.fillMaxWidth(),
//                            shape = RoundedCornerShape(12.dp),
//                            colors = CardDefaults.cardColors(containerColor = Color.White)
//                        ) {
//                            Box(
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .padding(32.dp),
//                                contentAlignment = Alignment.Center
//                            ) {
//                                Text(
//                                    text = "No posts yet",
//                                    fontSize = 16.sp,
//                                    color = Color(0xFF999999)
//                                )
//                            }
//                        }
//                    }
//                } else {
//                    items(myPosts) { post ->
//                        MyPostCard(
//                            post = post,
//                            onEditClick = { editPost ->
//                                // TODO: 수정 화면
//                                println("Edit post: ${editPost.id}")
//                            },
//                            onDeleteClick = { deletePost ->
//                                postToDelete = deletePost
//                                showDeleteDialog = true
//                            }
//                        )
//                    }
//                }
//            }
//
//// Student: 신청 목록
//            if (userRole == "STUDENT") {
//                item {
//                    Text(
//                        text = "My Applications",
//                        fontSize = 20.sp,
//                        fontWeight = FontWeight.Bold,
//                        color = Color(0xFF333333)
//                    )
//                }
//
//                if (isLoading) {
//                    item {
//                        Box(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .height(200.dp),
//                            contentAlignment = Alignment.Center
//                        ) {
//                            CircularProgressIndicator()
//                        }
//                    }
//                } else if (myApplications.isEmpty()) {
//                    item {
//                        Card(
//                            modifier = Modifier.fillMaxWidth(),
//                            shape = RoundedCornerShape(12.dp),
//                            colors = CardDefaults.cardColors(containerColor = Color.White)
//                        ) {
//                            Box(
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .padding(32.dp),
//                                contentAlignment = Alignment.Center
//                            ) {
//                                Text(
//                                    text = "No applications yet",
//                                    fontSize = 16.sp,
//                                    color = Color(0xFF999999)
//                                )
//                            }
//                        }
//                    }
//                } else {
//                    items(myApplications) { application ->
//                        ApplicationCard(
//                            application = application,
//                            post = applicationPosts[application.postId],
//                            onCancelClick = { app ->
//                                scope.launch {
//                                    val appRepo = ApplicationRepository()
//                                    val success = appRepo.cancelApplication(app.id)
//                                    if (success) {
//                                        // 리스트에서 제거
//                                        myApplications = myApplications.filter { it.id != app.id }
//                                        println("Application canceled successfully")
//                                    } else {
//                                        println("Failed to cancel application")
//                                    }
//                                }
//                            }
//                        )
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun MyPostCard(
//    post: PostEntity,
//    onEditClick: (PostEntity) -> Unit,
//    onDeleteClick: (PostEntity) -> Unit
//) {
//    val subject = subjects.find { it.id == post.subjectId }
//    val appRepo = remember { ApplicationRepository() }
//    val scope = rememberCoroutineScope()
//
//    var applicantCount by remember { mutableStateOf(0) }  // 추가!
//
//    // 신청자 수 로드
//    LaunchedEffect(post.id) {
//        val applications = appRepo.getApplicationsByPost(post.id)
//        applicantCount = applications.size
//    }
//
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
//            verticalArrangement = Arrangement.spacedBy(8.dp)
//        ) {
//            // 제목과 액션 버튼
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Text(
//                    text = post.title,
//                    fontSize = 18.sp,
//                    fontWeight = FontWeight.Bold,
//                    color = Color(0xFF333333),
//                    modifier = Modifier.weight(1f)
//                )
//
//                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
//                    // 수정 버튼
//                    IconButton(
//                        onClick = { onEditClick(post) },
//                        modifier = Modifier.size(32.dp)
//                    ) {
//                        Icon(
//                            imageVector = Icons.Default.Edit,
//                            contentDescription = "Edit",
//                            tint = Color(0xFF6200EE),
//                            modifier = Modifier.size(20.dp)
//                        )
//                    }
//
//                    // 삭제 버튼
//                    IconButton(
//                        onClick = { onDeleteClick(post) },
//                        modifier = Modifier.size(32.dp)
//                    ) {
//                        Icon(
//                            imageVector = Icons.Default.Delete,
//                            contentDescription = "Delete",
//                            tint = Color(0xFFFF5252),
//                            modifier = Modifier.size(20.dp)
//                        )
//                    }
//                }
//            }
//
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.spacedBy(8.dp)
//            ) {
//                Surface(
//                    shape = RoundedCornerShape(6.dp),
//                    color = Color(0xFF6200EE).copy(alpha = 0.1f)
//                ) {
//                    Text(
//                        text = subject?.name ?: "Unknown",
//                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
//                        fontSize = 12.sp,
//                        color = Color(0xFF6200EE),
//                        fontWeight = FontWeight.Medium
//                    )
//                }
//
//                Surface(
//                    shape = RoundedCornerShape(6.dp),
//                    color = Color(0xFF03A9F4).copy(alpha = 0.1f)
//                ) {
//                    Text(
//                        text = "${post.startDate} ~ ${post.endDate}",
//                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
//                        fontSize = 12.sp,
//                        color = Color(0xFF03A9F4),
//                        fontWeight = FontWeight.Medium
//                    )
//                }
//            }
//
//            if (post.content.isNotEmpty()) {
//                Text(
//                    text = post.content,
//                    fontSize = 14.sp,
//                    color = Color(0xFF666666),
//                    maxLines = 2
//                )
//            }
//
//            // 신청자 수 표시 (실시간)
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Text(
//                    text = "Applicants: $applicantCount/3",
//                    fontSize = 13.sp,
//                    color = if (applicantCount >= 3) Color(0xFFFF5252) else Color(0xFF999999),
//                    fontWeight = if (applicantCount > 0) FontWeight.Bold else FontWeight.Normal
//                )
//
//                // 신청자가 있으면 삭제 불가 표시
//                if (applicantCount > 0) {
//                    Surface(
//                        shape = RoundedCornerShape(8.dp),
//                        color = Color(0xFFFF5252).copy(alpha = 0.1f)
//                    ) {
//                        Text(
//                            text = "⚠️ Has applicants",
//                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
//                            fontSize = 11.sp,
//                            color = Color(0xFFFF5252),
//                            fontWeight = FontWeight.Medium
//                        )
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun ApplicationCard(
//    application: ApplicationEntity,
//    post: PostEntity?,
//    onCancelClick: (ApplicationEntity) -> Unit
//) {
//    if (post == null) return
//
//    val subject = subjects.find { it.id == post.subjectId }
//
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
//            // 제목과 상태
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Text(
//                    text = post.title,
//                    fontSize = 18.sp,
//                    fontWeight = FontWeight.Bold,
//                    color = Color(0xFF333333),
//                    modifier = Modifier.weight(1f)
//                )
//
//                // 상태 뱃지
//                Surface(
//                    shape = RoundedCornerShape(12.dp),
//                    color = when (application.status) {
//                        "CHATTING" -> Color(0xFF4CAF50).copy(alpha = 0.15f)
//                        "WAITING" -> Color(0xFFFFA726).copy(alpha = 0.15f)
//                        else -> Color(0xFF999999).copy(alpha = 0.15f)
//                    }
//                ) {
//                    Text(
//                        text = when (application.status) {
//                            "CHATTING" -> "💬 Chatting"
//                            "WAITING" -> "⏳ Queue #${application.queuePosition}"
//                            else -> "✓ Completed"
//                        },
//                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
//                        fontSize = 13.sp,
//                        color = when (application.status) {
//                            "CHATTING" -> Color(0xFF4CAF50)
//                            "WAITING" -> Color(0xFFFFA726)
//                            else -> Color(0xFF999999)
//                        },
//                        fontWeight = FontWeight.Bold
//                    )
//                }
//            }
//
//            // 과목 및 날짜
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.spacedBy(8.dp)
//            ) {
//                Surface(
//                    shape = RoundedCornerShape(6.dp),
//                    color = Color(0xFF6200EE).copy(alpha = 0.1f)
//                ) {
//                    Text(
//                        text = subject?.name ?: "Unknown",
//                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
//                        fontSize = 12.sp,
//                        color = Color(0xFF6200EE),
//                        fontWeight = FontWeight.Medium
//                    )
//                }
//
//                Surface(
//                    shape = RoundedCornerShape(6.dp),
//                    color = Color(0xFF03A9F4).copy(alpha = 0.1f)
//                ) {
//                    Text(
//                        text = "${post.startDate} ~ ${post.endDate}",
//                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
//                        fontSize = 12.sp,
//                        color = Color(0xFF03A9F4),
//                        fontWeight = FontWeight.Medium
//                    )
//                }
//            }
//
//            // 내용
//            if (post.content.isNotEmpty()) {
//                Text(
//                    text = post.content,
//                    fontSize = 14.sp,
//                    color = Color(0xFF666666),
//                    maxLines = 2
//                )
//            }
//
//            // 신청 날짜
//            Text(
//                text = "Applied: ${application.createdAt}",
//                fontSize = 12.sp,
//                color = Color(0xFF999999)
//            )
//
//            // 취소 버튼 (WAITING 상태만)
////            if (application.status == "WAITING") { 임시
//                Button(
//                    onClick = { onCancelClick(application) },
//                    modifier = Modifier.fillMaxWidth(),
//                    colors = ButtonDefaults.buttonColors(
//                        containerColor = Color(0xFFFF5252)
//                    ),
//                    shape = RoundedCornerShape(8.dp)
//                ) {
//                    Text(
//                        text = "Cancel Application",
//                        fontSize = 14.sp,
//                        fontWeight = FontWeight.Bold
//                    )
//                }
////            }
//        }
//    }
//}