package org.example.testproject.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.example.testproject.models.PostEntity
import org.example.testproject.repository.PostRepository


@Composable
fun TestSupabaseScreen() {
    val repository = remember { PostRepository() }
    val scope = rememberCoroutineScope()
    var posts by remember { mutableStateOf<List<PostEntity>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Supabase Connection Test", style = MaterialTheme.typography.headlineMedium)

        // 테스트 게시글 생성
        Button(
            onClick = {
                scope.launch {
                    isLoading = true
                    val testPost = PostEntity(
                        userId = "9d9e8d97-03be-43ab-a404-d4ee4859bfae",
                        districtId = 1,
                        subjectId = 1,
                        title = "Test Post ${kotlinx.datetime.Clock.System.now().toEpochMilliseconds()}",
                        content = "This is a test post",
                        startDate = "2025-11-01",
                        endDate = "2025-11-30"
                    )
                    val result = repository.createPost(testPost)
                    if (result != null) {
                        errorMessage = "Success! Post ID: ${result.id}"
                    } else {
                        errorMessage = "Failed to create post"
                    }
                    isLoading = false
                }
            },
            enabled = !isLoading
        ) {
            Text("Create Test Post")
        }

        // 게시글 조회
        Button(
            onClick = {
                scope.launch {
                    isLoading = true
                    posts = repository.getAllPosts()
                    errorMessage = "Loaded ${posts.size} posts"
                    isLoading = false
                }
            },
            enabled = !isLoading
        ) {
            Text("Load All Posts")
        }

        if (isLoading) {
            CircularProgressIndicator()
        }

        if (errorMessage.isNotEmpty()) {
            Text(errorMessage, color = MaterialTheme.colorScheme.primary)
        }

        // 게시글 목록
        posts.forEach { post ->
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("ID: ${post.id}", style = MaterialTheme.typography.labelSmall)
                    Text(post.title, style = MaterialTheme.typography.titleMedium)
                    Text(post.content, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}