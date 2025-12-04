package org.example.testproject.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.testproject.models.District
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
//import androidx.activity.compose.BackHandler
import org.example.testproject.utils.BackHandler

@Composable
fun SelectActionScreen(
    selectedDistrict: District,
    userRole: String,  // 추가!
    onCreatePostClick: () -> Unit,
    onViewPostsClick: () -> Unit,
    onBackClick: () -> Unit
) {
//    BackHandler(onBack = onBackClick)
    BackHandler(onBack = onBackClick)  // ← 여기 추가!

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
                .padding(vertical = 20.dp, horizontal = 16.dp)
        ) {
//            Text(
//                text = "← back",
//                fontSize = 16.sp,
//                color = Color.White,
//                modifier = Modifier
//                    .align(Alignment.CenterStart)
//            )
            Text(
                text = selectedDistrict.name,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // 중앙 버튼들
        if (userRole == "TEACHER") {
            // Teacher: 게시글 작성만
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF6200EE)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                onClick = onCreatePostClick
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "✏️",
                            fontSize = 56.sp
                        )
                        Text(
                            text = "게시글 작성하기(Create Post)",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "(Share your tutoring opportunity)",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        } else {
            // Student: 게시글 보기만
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF6200EE)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                onClick = onViewPostsClick
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "📋",
                            fontSize = 56.sp
                        )
                        Text(
                            text = "게시글 확인 하기(View Posts)",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "(Find tutoring opportunities)",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}