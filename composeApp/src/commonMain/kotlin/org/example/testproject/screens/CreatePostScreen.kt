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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.testproject.models.District
import org.example.testproject.models.Subject
import org.example.testproject.models.subjects
import androidx.compose.material3.CircularProgressIndicator
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import org.example.testproject.models.PostEntity
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import kotlinx.datetime.toLocalDateTime
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
//import androidx.activity.compose.BackHandler
//import org.example.testproject.utils.PlatformBackHandler
import org.example.testproject.utils.BackHandler  // import 추가!

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    selectedDistrict: District,
//    onPostCreate: (String, Subject, String, String, String) -> Unit,
    currentUserId: String,  // 추가!
    onPostCreate: suspend (PostEntity) -> Boolean,
    onBackClick: () -> Unit
) {

    BackHandler(onBack = onBackClick)
    var selectedSubject by remember { mutableStateOf<Subject?>(null) }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Header
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
                text = "게시글 작성(Create Post)",
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Selected district
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "지역(District)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )
                    Text(
                        text = selectedDistrict.name,
                        fontSize = 16.sp,
                        color = Color(0xFF6200EE)
                    )
                }
            }

            // Subject selection
            Column {
                Text(
                    text = "과목 선택(Select Subject)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),  // 추가!,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    subjects.forEach { subject ->
                        SubjectChip(
                            subject = subject,
                            isSelected = selectedSubject == subject,
                            onClick = { selectedSubject = subject }
                        )
                    }
                }
            }

            // Date selection
            // 날짜 선택 (수정!)
            Column {
                Text(
                    text = "기간(Period)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 시작일
                    DatePickerField(
                        label = "시작일(Start Date)",
                        date = startDate,
                        onClick = { showStartDatePicker = true },
                        modifier = Modifier.weight(1f)
                    )

                    // 종료일
                    DatePickerField(
                        label = "종료일(End Date)",
                        date = endDate,
                        onClick = { showEndDatePicker = true },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Title input
            Column {
                Text(
                    text = "제목(Title)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("제목을 입력하세요(Enter the title)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF6200EE),
                        focusedLabelColor = Color(0xFF6200EE)
                    )
                )
            }

            // Content input
            Column {
                Text(
                    text = "상세 내용(Details)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    placeholder = { Text("상세 내용을 입력하세요(Enter detailed content)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF6200EE),
                        focusedLabelColor = Color(0xFF6200EE)
                    ),
                    maxLines = 10
                )
            }

            Button(
                onClick = {
                    if (selectedSubject != null && startDate.isNotEmpty() &&
                        endDate.isNotEmpty() && title.isNotEmpty()
                    ) {
                        scope.launch {
                            isLoading = true
                            val postEntity = PostEntity(
//                                userId = "9d9e8d97-03be-43ab-a404-d4ee4859bfae",  // 나중에 실제 user ID로 변경
                                userId = currentUserId,  // 실제 userId 사용!
                                districtId = selectedDistrict.id,
                                subjectId = selectedSubject!!.id,
                                title = title,
                                content = content,
                                startDate = startDate,
                                endDate = endDate
                            )
                            val success = onPostCreate(postEntity)
                            isLoading = false
                            if (success) {
                                // 초기화
                                title = ""
                                content = ""
                                startDate = ""
                                endDate = ""
                                selectedSubject = null
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
                enabled = !isLoading && selectedSubject != null &&
                        startDate.isNotEmpty() && endDate.isNotEmpty() &&
                        title.isNotEmpty()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Text(
                        text = "작성 완료(Submit)",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    // DatePicker Dialogs
    if (showStartDatePicker) {
        DatePickerDialog(
            onDateSelected = { date ->
                startDate = date
                showStartDatePicker = false
            },
            onDismiss = { showStartDatePicker = false }
        )
    }

    if (showEndDatePicker) {
        DatePickerDialog(
            onDateSelected = { date ->
                endDate = date
                showEndDatePicker = false
            },
            onDismiss = { showEndDatePicker = false }
        )
    }
}

    @Composable
    fun SubjectChip(
        subject: Subject,
        isSelected: Boolean,
        onClick: () -> Unit
    ) {
        Card(
            modifier = Modifier.clickable(onClick = onClick),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isSelected) Color(0xFF6200EE) else Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Text(
                text = subject.name,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                color = if (isSelected) Color.White else Color(0xFF333333),
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
@Composable
fun DatePickerField(
    label: String,
    date: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(56.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color(0xFF999999)
            )
            Text(
                text = date.ifEmpty { "날짜 선택(Select date)" },
                fontSize = 16.sp,
                color = if (date.isEmpty()) Color(0xFFCCCCCC) else Color(0xFF333333),
                fontWeight = if (date.isEmpty()) FontWeight.Normal else FontWeight.Medium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
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

