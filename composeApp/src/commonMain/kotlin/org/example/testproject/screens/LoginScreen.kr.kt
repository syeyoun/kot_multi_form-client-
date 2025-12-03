package org.example.testproject.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.example.testproject.repository.AuthRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: (String) -> Unit,
    onSignupClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val repository = remember { AuthRepository() }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

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
                text = "로그인(Login)",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "돌아오신걸 환영합니다!(Welcome Back!)",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333)
            )

            Text(
                text = "로그인 필요(Login to continue)",
                fontSize = 16.sp,
                color = Color(0xFF666666)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 이메일
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    errorMessage = ""
                },
                label = { Text("이메일(Email)") },
                placeholder = { Text("example@email.com") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF6200EE),
                    focusedLabelColor = Color(0xFF6200EE)
                ),
                singleLine = true
            )

            // 비밀번호
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    errorMessage = ""
                },
                label = { Text("비밀번호(Password)") },
                placeholder = { Text("비밀번호를 입력해주세요(Enter your password)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF6200EE),
                    focusedLabelColor = Color(0xFF6200EE)
                ),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true
            )

            // 에러 메시지
            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp
                )
            }

            // 비밀번호 찾기
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "비밀번호를 잊어버리셨나요?(Forgot password?)",
                    color = Color(0xFF6200EE),
                    fontSize = 14.sp,
                    modifier = Modifier.clickable {
                        // TODO: 비밀번호 찾기
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 로그인 버튼
            Button(
                onClick = {
                    when {
                        email.isEmpty() || password.isEmpty() -> {
                            errorMessage = "전부 작성해야 합니다(Please fill in all fields)"
                        }
                        !email.contains("@") -> {
                            errorMessage = "메일 형식에 맞게 작성해주세요(Invalid email format)"
                        }
                        else -> {
                            scope.launch {
                                isLoading = true
                                val result = repository.login(email, password)
                                isLoading = false

                                if (result != null) {
                                    if (!result.approved) {
                                        errorMessage = "승인 대기중입니다. 관리자의 승인을 기다려주세요.(Account pending approval. Please wait for admin approval.)"
                                    } else {
                                        val user = repository.getUserById(result.id)
                                        if (user != null) {
                                            onLoginSuccess(user.id)
                                        }
                                    }
                                } else {
                                    errorMessage = "이메일 또는 비밀번호가 잘못되었습니다(Invalid email or password)"
                                }
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6200EE)
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Text(
                        text = "로그인(Login)",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 구분선
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    thickness = DividerDefaults.Thickness,
                    color = Color(0xFFDDDDDD)
                )
                Text(
                    text = " 또는(OR) ",
                    color = Color(0xFF999999),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    thickness = DividerDefaults.Thickness,
                    color = Color(0xFFDDDDDD)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 회원가입으로 이동
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "계정이 없으신가요?(Don't have an account?) ",
                    color = Color(0xFF666666)
                )
                Text(
                    text = "회원가입(Sign Up)",
                    color = Color(0xFF6200EE),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onSignupClick() }
                )
            }
        }
    }
}

//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun LoginScreen(
//    onLoginSuccess: (String) -> Unit,  // userId 반환
//    onSignupClick: () -> Unit,
//    onBackClick: () -> Unit
//) {
//    val repository = remember { AuthRepository() }
//    val scope = rememberCoroutineScope()
//    val scrollState = rememberScrollState()
//
//    var email by remember { mutableStateOf("") }
//    var password by remember { mutableStateOf("") }
//    var isLoading by remember { mutableStateOf(false) }
//    var errorMessage by remember { mutableStateOf("") }
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
//                text = "← 뒤로(Back)",
//                fontSize = 16.sp,
//                color = Color.White,
//                modifier = Modifier
//                    .clickable { onBackClick() }
//                    .align(Alignment.CenterStart)
//            )
//            Text(
//                text = "로그인(Login)",
//                fontSize = 20.sp,
//                fontWeight = FontWeight.Bold,
//                color = Color.White,
//                modifier = Modifier.align(Alignment.Center)
//            )
//        }
//
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .verticalScroll(scrollState)
//                .padding(24.dp),
//            verticalArrangement = Arrangement.spacedBy(16.dp)
//        ) {
//            Spacer(modifier = Modifier.height(32.dp))
//
//            Text(
//                text = "돌아오신걸 환영합니다!(Welcome Back!)",
//                fontSize = 32.sp,
//                fontWeight = FontWeight.Bold,
//                color = Color(0xFF333333)
//            )
//
//            Text(
//                text = "로그인 필요(Login to continue)",
//                fontSize = 16.sp,
//                color = Color(0xFF666666)
//            )
//
//            Spacer(modifier = Modifier.height(24.dp))
//
//            // 이메일
//            OutlinedTextField(
//                value = email,
//                onValueChange = {
//                    email = it
//                    errorMessage = ""
//                },
//                label = { Text("이메일(Email)") },
//                placeholder = { Text("example@email.com") },
//                modifier = Modifier.fillMaxWidth(),
//                colors = OutlinedTextFieldDefaults.colors(
//                    focusedBorderColor = Color(0xFF6200EE),
//                    focusedLabelColor = Color(0xFF6200EE)
//                ),
//                singleLine = true
//            )
//
//            // 비밀번호
//            OutlinedTextField(
//                value = password,
//                onValueChange = {
//                    password = it
//                    errorMessage = ""
//                },
//                label = { Text("비밀번호(Password)") },
//                placeholder = { Text("비밀번호를 입력해주세요(Enter your password)") },
//                modifier = Modifier.fillMaxWidth(),
//                colors = OutlinedTextFieldDefaults.colors(
//                    focusedBorderColor = Color(0xFF6200EE),
//                    focusedLabelColor = Color(0xFF6200EE)
//                ),
//                visualTransformation = PasswordVisualTransformation(),
//                singleLine = true
//            )
//
//            // 에러 메시지
//            if (errorMessage.isNotEmpty()) {
//                Text(
//                    text = errorMessage,
//                    color = MaterialTheme.colorScheme.error,
//                    fontSize = 14.sp
//                )
//            }
//
//            // 비밀번호 찾기 (TODO)
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.End
//            ) {
//                Text(
//                    text = "(비밀번호를 잊어버리셨나요?)Forgot password?",
//                    color = Color(0xFF6200EE),
//                    fontSize = 14.sp,
//                    modifier = Modifier.clickable {
//                        // TODO: 비밀번호 찾기
//                    }
//                )
//            }
//
//            Spacer(modifier = Modifier.height(8.dp))
//
//            // 로그인 버튼
//            Button(
//                onClick = {
//                    when {
//                        email.isEmpty() || password.isEmpty() -> {
//                            errorMessage = "전부 작성해야 합니다(Please fill in all fields)"
//                        }
//                        !email.contains("@") -> {
//                            errorMessage = "메일 형식에 맞게 작성해주세요(Invalid email format)"
//                        }
//                        else -> {
//                            scope.launch {
//                                isLoading = true
//                                val result = repository.login(email, password)
//                                isLoading = false
//
//                                if (result != null) {
//                                    if (!result.approved) {
//                                        errorMessage = "Account pending approval. Please wait for admin approval."
//                                    } else {
//                                        val user = repository.getUserById(result.id)  // authRepo 대신 repository
//                                        if (user != null) {
//                                            onLoginSuccess(user.id)
//                                        }
//                                    }
//                                } else {
//                                    errorMessage = "Invalid email or password"
//                                }
//                            }
//                        }
//                    }
//                },
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(56.dp),
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = Color(0xFF6200EE)
//                ),
//                shape = RoundedCornerShape(12.dp),
//                enabled = !isLoading
//            ) {
//                if (isLoading) {
//                    CircularProgressIndicator(
//                        modifier = Modifier.size(24.dp),
//                        color = Color.White
//                    )
//                } else {
//                    Text(
//                        text = "Login",
//                        fontSize = 18.sp,
//                        fontWeight = FontWeight.Bold
//                    )
//                }
//            }
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            // 구분선
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                HorizontalDivider(
//                    modifier = Modifier.weight(1f),
//                    thickness = DividerDefaults.Thickness,
//                    color = Color(0xFFDDDDDD)
//                )
//                Text(
//                    text = " OR ",
//                    color = Color(0xFF999999),
//                    modifier = Modifier.padding(horizontal = 8.dp)
//                )
//                HorizontalDivider(
//                    modifier = Modifier.weight(1f),
//                    thickness = DividerDefaults.Thickness,
//                    color = Color(0xFFDDDDDD)
//                )
//            }
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            // 회원가입으로 이동
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.Center
//            ) {
//                Text(
//                    text = "Don't have an account? ",
//                    color = Color(0xFF666666)
//                )
//                Text(
//                    text = "Sign Up",
//                    color = Color(0xFF6200EE),
//                    fontWeight = FontWeight.Bold,
//                    modifier = Modifier.clickable { onSignupClick() }
//                )
//            }
//        }
//    }
//}