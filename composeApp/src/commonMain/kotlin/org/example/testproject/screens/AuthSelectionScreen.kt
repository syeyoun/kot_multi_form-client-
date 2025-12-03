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
//import androidx.activity.compose.BackHandler

@Composable
fun AuthSelectScreen(
    onLoginClick: () -> Unit,
    onSignupClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 로고/타이틀 (Logo/Title)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "For_M",
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6200EE)
                )
                Text(
                    text = "어서오세요!(Welcome!)",
                    fontSize = 20.sp,
                    color = Color(0xFF666666)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 로그인 버튼 (Login Button)
            Button(
                onClick = onLoginClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6200EE)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "로그인(Login)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // 회원가입 버튼 (Sign Up Button)
            OutlinedButton(
                onClick = onSignupClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    width = 2.dp,
                    brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF6200EE))
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "회원가입(Sign Up)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6200EE)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 임시: 로그인 없이 계속하기 (Temporary: Continue without login)
            TextButton(onClick = { /* TODO */ }) {
                Text(
                    text = "로그인 없이 계속하기(Continue without login) (Test)",
                    color = Color(0xFF999999)
                )
            }
        }
    }
}