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
import org.example.testproject.models.User
import org.example.testproject.repository.AuthRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(
    onSignupSuccess: (String, String) -> Unit,  // userId 반환 (return userId)
    onBackClick: () -> Unit
) {
    val repository = remember { AuthRepository() }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordConfirm by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("STUDENT") }

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
//            Text(
//                text = "← 뒤로(Back)",
//                fontSize = 16.sp,
//                color = Color.White,
//                modifier = Modifier
//                    .clickable { onBackClick() }
//                    .align(Alignment.CenterStart)
//            )
            Text(
                text = "회원 가입(Sign Up)",
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
            Text(
                text = "계정 만들기(Create Account)",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333)
            )

            Text(
                text = "오늘 For_M에 가입하세요!(Join For_M today!)",
                fontSize = 16.sp,
                color = Color(0xFF666666)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 이메일 (Email)
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

            // 닉네임 (Nickname)
            OutlinedTextField(
                value = nickname,
                onValueChange = {
                    nickname = it
                    errorMessage = ""
                },
                label = { Text("닉네임(Nickname)") },
                placeholder = { Text("당신의 닉네임(Your nickname)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF6200EE),
                    focusedLabelColor = Color(0xFF6200EE)
                ),
                singleLine = true
            )

            Column {
                Text(
                    text = "나는...(I am a...)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Student 버튼 (Student button)
                    RoleButton(
                        text = "학생(Student)",
                        isSelected = selectedRole == "STUDENT",
                        onClick = { selectedRole = "STUDENT" },
                        modifier = Modifier.weight(1f)
                    )

                    // Teacher 버튼 (Teacher button)
                    RoleButton(
                        text = "선생님(Teacher)",
                        isSelected = selectedRole == "TEACHER",
                        onClick = { selectedRole = "TEACHER" },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 비밀번호 (Password)
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    errorMessage = ""
                },
                label = { Text("비밀번호(Password)") },
                placeholder = { Text("최소 6자 이상(At least 6 characters)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF6200EE),
                    focusedLabelColor = Color(0xFF6200EE)
                ),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true
            )

            // 비밀번호 확인 (Confirm Password)
            OutlinedTextField(
                value = passwordConfirm,
                onValueChange = {
                    passwordConfirm = it
                    errorMessage = ""
                },
                label = { Text("비밀번호 확인(Confirm Password)") },
                placeholder = { Text("비밀번호 재입력(Re-enter password)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF6200EE),
                    focusedLabelColor = Color(0xFF6200EE)
                ),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true
            )

            // 에러 메시지 (Error message)
            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 회원가입 버튼 (Sign up button)
            Button(
                onClick = {
                    when {
                        email.isEmpty() || nickname.isEmpty() ||
                                password.isEmpty() || passwordConfirm.isEmpty() -> {
                            errorMessage = "모든 항목을 입력해주세요(Please fill in all fields)"
                        }
                        !email.contains("@") -> {
                            errorMessage = "올바른 이메일 형식이 아닙니다(Invalid email format)"
                        }
                        password.length < 6 -> {
                            errorMessage = "비밀번호는 최소 6자 이상이어야 합니다(Password must be at least 6 characters)"
                        }
                        password != passwordConfirm -> {
                            errorMessage = "비밀번호가 일치하지 않습니다(Passwords do not match)"
                        }
                        else -> {
                            scope.launch {
                                isLoading = true
                                val result = repository.signUp(
                                    email = email,
                                    password = password,
                                    nickname = nickname,
                                    role = selectedRole  // role 추가! (Added role!)
                                )
                                isLoading = false

                                if (result != null) {
                                    errorMessage = "회원가입이 완료 되었습니다 관리자 승인을 기다려주세요!(Signup successful! Please wait for admin approval.)"
                                    // onSignupSuccess 호출 안 함! (Don't call onSignupSuccess!)
                                } else {
                                    errorMessage = "회원가입 실패. 이메일이 이미 존재할 수 있습니다(Signup failed. Email may already exist.)"
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
                        text = "계정 만들기(Create Account)",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // 로그인으로 이동 (Go to login)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "이미 계정이 있으신가요?(Already have an account?) ",
                    color = Color(0xFF666666)
                )
                Text(
                    text = "로그인(Login)",
                    color = Color(0xFF6200EE),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onBackClick() }
                )
            }
        }
    }
}

@Composable
fun RoleButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(60.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF6200EE) else Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 2.dp
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.White else Color(0xFF333333)
            )
        }
    }
}