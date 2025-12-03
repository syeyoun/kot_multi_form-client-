package org.example.testproject

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.example.testproject.models.*
import org.example.testproject.repository.ChatRepository
import org.example.testproject.repository.PostRepository
import org.example.testproject.screens.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import androidx.compose.runtime.rememberCoroutineScope
//import org.example.testproject.repository.ChatRepository
import org.example.testproject.models.PostEntity
import org.example.testproject.models.ChatRoomEntity
import androidx.compose.runtime.rememberCoroutineScope
import org.example.testproject.repository.ApplicationRepository
import org.example.testproject.repository.AuthRepository
import org.example.testproject.models.NotificationEntity
import org.example.testproject.repository.NotificationRepository
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.example.testproject.utils.AuthPreferences
//import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.GlobalScope
//import android.content.Context
//import com.google.firebase.messaging.FirebaseMessaging

enum class MainTab {
    HOME,
    CHAT,
    PROFILE
}

enum class HomeScreen {
    DISTRICT_SELECT,
    SELECT_ACTION,
    CREATE_POST,
    VIEW_POSTS
}

enum class ChatScreen {
    CHAT_LIST,
    CHAT_ROOM
}

enum class AuthScreen {
    LOADING,
    AUTH_SELECT,
    LOGIN,
    SIGNUP
}

@Composable
fun App() {

    var authScreen by remember { mutableStateOf(AuthScreen.LOADING) }
    var isAuthenticated by remember { mutableStateOf(false) }

    var currentTab by remember { mutableStateOf(MainTab.HOME) }
    var currentHomeScreen by remember { mutableStateOf(HomeScreen.DISTRICT_SELECT) }
    var currentChatScreen by remember { mutableStateOf(ChatScreen.CHAT_LIST) }

    var selectedDistrict by remember { mutableStateOf<District?>(null) }
    var selectedChatRoom by remember { mutableStateOf<ChatRoomEntity?>(null) }
    var selectedPost by remember { mutableStateOf<PostEntity?>(null) }

//    val currentUserId = "9d9e8d97-03be-43ab-a404-d4ee4859bfae"  // 임시 유저 ID
    var currentUserId by remember { mutableStateOf("") }
    var userRole by remember { mutableStateOf("STUDENT") }  // 추가!
    val scope = rememberCoroutineScope()

    val authPrefs = remember { AuthPreferences() }

    // 알림 관련 변수 추가 (여기!)
    var unreadNotifications by remember { mutableStateOf(0) }
    var showNotificationPopup by remember { mutableStateOf(false) }
    var latestNotification by remember { mutableStateOf<NotificationEntity?>(null) }

    // 앱 시작 시 자동 로그인 체크 (추가!)
    LaunchedEffect(Unit) {
        if (authPrefs.isLoggedIn()) {
            val (savedUserId, savedUserRole) = authPrefs.getLoginState()
            if (savedUserId != null && savedUserRole != null) {
                currentUserId = savedUserId
                userRole = savedUserRole
                isAuthenticated = true
                println("=== AUTO LOGIN SUCCESS: $savedUserId ===")
            } else {
                authScreen = AuthScreen.AUTH_SELECT
            }
        } else {
            authScreen = AuthScreen.AUTH_SELECT
        }
    }

    // 로그인 후 FCM 토큰 저장 + 알림 구독
    LaunchedEffect(isAuthenticated, currentUserId) {
        if (isAuthenticated && currentUserId.isNotEmpty()) {
            println("=== USER AUTHENTICATED: $currentUserId ===")

            // FCM 토큰 저장
            saveFcmToken(currentUserId)  // import 필요 없음!

            val notificationRepo = NotificationRepository()

            // 읽지 않은 알림 개수
            unreadNotifications = notificationRepo.getUnreadCount(currentUserId)

            // 실시간 구독
            launch {
                notificationRepo.subscribeToNotifications(currentUserId).collect { notification ->
                    println("=== NEW NOTIFICATION: ${notification.title} ===")
                    latestNotification = notification
                    unreadNotifications++
                    showNotificationPopup = true
                }
            }
        }
    }

    if (!isAuthenticated) {
        // 인증 화면
        when (authScreen) {
            AuthScreen.LOADING -> {
                LoadingScreen(
                    onLoadingComplete = {
//                        authScreen = AuthScreen.AUTH_SELECT
                    }
                )
            }

            AuthScreen.AUTH_SELECT -> {
                AuthSelectScreen(
                    onLoginClick = {
                        authScreen = AuthScreen.LOGIN
                    },
                    onSignupClick = {
                        authScreen = AuthScreen.SIGNUP
                    }
                )
            }

            AuthScreen.LOGIN -> {
                LoginScreen(
                    onLoginSuccess = { userId ->
//                        currentUserId = userId
//                        isAuthenticated = true
                        scope.launch {
                            val authRepo = AuthRepository()
                            val user = authRepo.getUserById(userId)
                            if (user != null) {
                                currentUserId = userId
                                userRole = user.role

                                // 로그인 상태 저장! (추가!)
                                authPrefs.saveLoginState(userId, user.role)

                                isAuthenticated = true
                            }
                        }
                    },
                    onSignupClick = {
                        authScreen = AuthScreen.SIGNUP
                    },
                    onBackClick = {
                        authScreen = AuthScreen.AUTH_SELECT
                    }
                )
            }

            AuthScreen.SIGNUP -> {
                SignupScreen(
                    onSignupSuccess = { userId, role ->
                        currentUserId = userId
                        isAuthenticated = true
                        userRole = role  // 저장!

                        // 회원가입 후에도 저장! (추가!)
                        authPrefs.saveLoginState(userId, role)
                    },
                    onBackClick = {
                        authScreen = AuthScreen.AUTH_SELECT
                    }
                )
            }
        }
    } else {

        Scaffold(
            bottomBar = {
                BottomNavigationBar(
                    currentTab = currentTab,
                    onTabSelected = {
                        currentTab = it
                        if (it == MainTab.HOME) {
                            currentHomeScreen = HomeScreen.DISTRICT_SELECT
                        }
                        if (it == MainTab.CHAT) {
                            currentChatScreen = ChatScreen.CHAT_LIST
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                when (currentTab) {
                    MainTab.HOME -> {
                        HomeContent(
                            currentScreen = currentHomeScreen,
                            onScreenChange = { currentHomeScreen = it },
                            selectedDistrict = selectedDistrict,
                            onDistrictSelect = { selectedDistrict = it },
                            currentUserId = currentUserId,
                            userRole = userRole,  // 전달!
                            onNavigateToChat = { post, application ->  // 추가!
                                scope.launch {
                                    // ChatRoom 생성/가져오기
                                    val chatRepo = ChatRepository()
                                    val chatRoom = chatRepo.getOrCreateChatRoom(
                                        postId = post.id,
                                        userId = currentUserId,
                                        teacherId = post.userId
                                    )

                                    if (chatRoom != null) {
                                        selectedChatRoom = chatRoom
                                        selectedPost = post
                                        currentTab = MainTab.CHAT
                                        currentChatScreen = ChatScreen.CHAT_ROOM
                                    }
                                }
                            }
                        )
                    }

                    MainTab.CHAT -> {
                        ChatContent(
                            currentScreen = currentChatScreen,
                            selectedChatRoom = selectedChatRoom,
                            selectedPost = selectedPost,
                            currentUserId = currentUserId,
                            onChatRoomSelect = { chatRoom, post ->
                                selectedChatRoom = chatRoom
                                selectedPost = post
                                currentChatScreen = ChatScreen.CHAT_ROOM
                            },
                            onBackClick = {
                                currentChatScreen = ChatScreen.CHAT_LIST
                            },
                            onScreenChange = { screen ->  // 추가!
                                currentChatScreen = screen
                            }
                        )
                    }

                    MainTab.PROFILE -> {
                        var nickname by remember { mutableStateOf("") }
                        var email by remember { mutableStateOf("") }

                        LaunchedEffect(currentUserId) {
                            if (currentUserId.isNotEmpty()) {
                                val authRepo = AuthRepository()
                                val user = authRepo.getUserById(currentUserId)
                                if (user != null) {
                                    nickname = user.nickname
                                    email = user.email
                                }
                            }
                        }

                        ProfileScreen(
                            currentUserId = currentUserId,
                            userRole = userRole,
                            nickname = nickname,
                            email = email,
                            onLogout = {

                                // 로그아웃 시 상태 삭제! (추가!)
                                authPrefs.clearLoginState()

                                isAuthenticated = false
                                currentUserId = ""
                                userRole = "STUDENT"
                                authScreen = AuthScreen.AUTH_SELECT
                            }
                        )
                    }
                }
            }
        }

        // 알림 팝업 (여기에 추가!)
        if (showNotificationPopup && latestNotification != null) {
            NotificationPopup(
                notification = latestNotification!!,
                onDismiss = { showNotificationPopup = false },
                onViewClick = {
                    showNotificationPopup = false
                    // TODO: 관련 화면으로 이동
                }
            )
        }
    }
}

@Composable
fun HomeContent(
    currentScreen: HomeScreen,
    onScreenChange: (HomeScreen) -> Unit,
    selectedDistrict: District?,
    onDistrictSelect: (District) -> Unit,
//    onApplyToPost: (PostEntity) -> Unit,
    userRole: String,  // 추가!
    currentUserId: String,
    onNavigateToChat: (PostEntity, ApplicationEntity) -> Unit  // 추가!
) {
    val scope = rememberCoroutineScope()
    when (currentScreen) {
        HomeScreen.DISTRICT_SELECT -> {
            MainScreen(
                onDistrictClick = { district ->
                    onDistrictSelect(district)
                    onScreenChange(HomeScreen.SELECT_ACTION)
                }
            )
        }

        HomeScreen.SELECT_ACTION -> {
            selectedDistrict?.let { district ->
                SelectActionScreen(
                    selectedDistrict = district,
                    userRole = userRole,  // 전달!
                    onCreatePostClick = {
                        onScreenChange(HomeScreen.CREATE_POST)
                    },
                    onViewPostsClick = {
                        onScreenChange(HomeScreen.VIEW_POSTS)
                    },
                    onBackClick = {
                        onScreenChange(HomeScreen.DISTRICT_SELECT)
                    }
                )
            }
        }

        HomeScreen.CREATE_POST -> {
            selectedDistrict?.let { district ->
                CreatePostScreen(
                    selectedDistrict = district,
                    currentUserId = currentUserId,  // 전달!
                    onPostCreate = { postEntity ->
                        val repository = PostRepository()
                        val result = repository.createPost(postEntity)
                        if (result != null) {
                            println("게시글이 성공적으로 작성되었습니다(Post created successfully): ${result.id}")
                            onScreenChange(HomeScreen.SELECT_ACTION)
                            true
                        } else {
                            println("게시글 작성에 실패했습니다(Failed to create post)")
                            false
                        }
                    },
                    onBackClick = {
                        onScreenChange(HomeScreen.SELECT_ACTION)
                    }
                )
            }
        }

        HomeScreen.VIEW_POSTS -> {
            selectedDistrict?.let { district ->
                PostListScreen(
                    selectedDistrict = district,
                    currentUserId = currentUserId,
                    onApplyClick = { post ->
                        scope.launch {
                            val appRepo = ApplicationRepository()
                            val application = appRepo.applyToPost(
                                postId = post.id,
                                userId = currentUserId
                            )

                            if (application != null) {
                                if (application.status == "CHATTING") {
                                    // 즉시 채팅 시작 → Chat 탭으로
                                    println("신청이 성공적으로 완료 되었습니다 바로 채팅으로 이동합니다(Application successful! Status): CHATTING")
                                    onNavigateToChat(post, application)  // 채팅 이동!
                                } else {
                                    // 대기열 추가됨
                                    println("신청 대기에 성공했습니다(Added to queue. Position): ${application.queuePosition}")
                                    // TODO: 대기열 안내 메시지
                                }
                            } else {
                                println("신청에 실패했습니다(Application failed (already applied or queue full))")
                                // TODO: 오류 메시지
                            }
                        }
                    },
                    onBackClick = {
                        onScreenChange(HomeScreen.SELECT_ACTION)
                    }
                )
            } ?: Box(modifier = Modifier.fillMaxSize())
        }
    }
}

@Composable
fun ChatContent(
    currentScreen: ChatScreen,
//    chatRooms: List<ChatRoomEntity>,
    selectedChatRoom: ChatRoomEntity?,
    selectedPost: PostEntity?,
    currentUserId: String,
    onChatRoomSelect: (ChatRoomEntity, PostEntity) -> Unit,
    onBackClick: () -> Unit,
    onScreenChange: (ChatScreen) -> Unit  // 추가!
) {
    val scope = rememberCoroutineScope()  // 추가

    when (currentScreen) {
        ChatScreen.CHAT_LIST -> {
            ChatListScreen(
                currentUserId = currentUserId,
                onChatRoomClick = onChatRoomSelect
            )
        }

        ChatScreen.CHAT_ROOM -> {
            if (selectedChatRoom != null && selectedPost != null) {
                org.example.testproject.screens.ChatScreen(
                    chatRoom = selectedChatRoom,
                    post = selectedPost,
                    currentUserId = currentUserId,
                    onBackClick = {
                        onScreenChange(ChatScreen.CHAT_LIST)
                    },
                    onLeaveChat = {  // 추가!
                        scope.launch {
                            val appRepo = ApplicationRepository()
                            val success = appRepo.leaveChat(
                                postId = selectedPost!!.id,
                                userId = currentUserId
                            )
                            if (success) {
                                println("Left chat successfully. Next person matched!")
                                onScreenChange(ChatScreen.CHAT_LIST)
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    currentTab: MainTab,
    onTabSelected: (MainTab) -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("홈(Home)") },
            selected = currentTab == MainTab.HOME,
            onClick = { onTabSelected(MainTab.HOME) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF6200EE),
                selectedTextColor = Color(0xFF6200EE),
                indicatorColor = Color(0xFF6200EE).copy(alpha = 0.1f)
            )
        )

        NavigationBarItem(
            icon = { Icon(Icons.Default.Chat, contentDescription = "Chat") },
            label = { Text("채팅방(Chat)") },
            selected = currentTab == MainTab.CHAT,
            onClick = { onTabSelected(MainTab.CHAT) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF6200EE),
                selectedTextColor = Color(0xFF6200EE),
                indicatorColor = Color(0xFF6200EE).copy(alpha = 0.1f)
            )
        )

        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("프로필(Profile)") },
            selected = currentTab == MainTab.PROFILE,
            onClick = { onTabSelected(MainTab.PROFILE) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF6200EE),
                selectedTextColor = Color(0xFF6200EE),
                indicatorColor = Color(0xFF6200EE).copy(alpha = 0.1f)
            )
        )
    }
}

@Composable
fun ProfileScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Profile",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Coming soon...",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun NotificationPopup(
    notification: NotificationEntity,
    onDismiss: () -> Unit,
    onViewClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Text("🔔", fontSize = 32.sp)
        },
        title = {
            Text(
                text = notification.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(notification.message)
        },
        confirmButton = {
            TextButton(onClick = onViewClick) {
                Text("보기(View)", color = Color(0xFF6200EE))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("닫기(Close)", color = Color(0xFF999999))
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

fun getCurrentTime(): String {
    return "12:00"  // 임시로 고정값
}

