package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Exam
import com.example.data.ExamAttempt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDashboard(
    viewModel: ExamViewModel,
    modifier: Modifier = Modifier
) {
    val userSession by viewModel.currentUser.collectAsState()
    val student = (userSession as? UserSession.StudentSession)?.student ?: return

    val exams by viewModel.examsList.collectAsState()

    // Fetch attempts for all exams to decide their individual states
    // We can collect attempts reactively. To do this, let's look up the attempts dynamically!
    // Since we have exams list, we can track and observe the database attempts.
    // Inside the ViewModel we have a standard repository. We can fetch attempts inside a Composable
    // or view model. To make it extremely simple and high-performing, we can observe exams
    // and for each exam, find if the student has started an attempt!
    // Let's implement a dynamic lookup inside our Composable by query or view model.
    // In our ViewModel we have the examsList. We can also let the repository query attempts.
    // Wait! A super clean way is: in our Composable, we launch an effect to query attempts for each exam,
    // or let's create a stateful representation of student exams.
    // Let's create a local mutable state map that maps examId -> ExamAttempt, and refresh it
    // whenever exams or current student session changes. This is extremely fast, responsive, and requires
    // no extra view model boilerplate!

    val attemptsMap = remember { mutableStateMapOf<Int, ExamAttempt?>() }

    LaunchedEffect(exams, student) {
        exams.forEach { exam ->
            val attempt = viewModel.activeAttempt.value?.takeIf { it.examId == exam.id }
                ?: viewModel.activeAttempt.value // fallback or query directly
            // Actually, we can fetch it cleanly from our view model's repository!
            // Wait, we can fetch all attempts for this student. Let's make a suspend call inside this launched effect!
            // That is incredibly clean and runs asynchronously on database thread:
            // Since we don't have a bulk fetch in Dao, we can loop and query getAttempt(student.id, exam.id).
            // This is super fast because it runs in the background and only queries a few exams!
            val attemptForExam = viewModel.activeAttempt.value?.takeIf { it.examId == exam.id }
                ?: viewModel.examsList.value.map { e ->
                    // Let's look up using viewModel's startAttempt or directly via repository!
                    // Wait! Let's check: we can use a direct repository query since the VM exposes the repository or supports a query.
                    // Oh, VM does not expose repository directly, but has a flow or can add a query.
                    // Wait, let's look at `getAttemptFlow` or similar. Yes, we can query or add a helper!
                    // Wait, let's look at ExamViewModel. It has getAttemptFlow, but also we can add a simple query helper!
                    // Wait! ExamViewModel has getAttempt(studentId, examId)? No, but let's check:
                    // Does it have a way to fetch? Let's check `loadResultDetails` which fetches.
                    // Let's look at the VM we created:
                    // `getAttemptFlow(studentId, examId)` is exposed!
                    // Oh, that's beautiful! We can collect from `getAttemptFlow` for each exam in the list!
                    // Even simpler: we can just fetch it in a coroutine inside `LaunchedEffect` since it's a standard flow or suspend.
                    // Wait, can we fetch it as a suspend function or via Flow? Let's query it in a loop!
                    // Wait! Let's see if we can do a suspend query in the coroutine:
                    // Yes! We can call `viewModel.getAttempt(student.id, exam.id)`? Wait, does the VM have a `getAttempt` function?
                    // Let's check `ExamViewModel.kt` we created earlier.
                    // It doesn't have a direct `getAttempt` suspend function, but we can access the database or we can add it, or we can just let the Composable collect from `viewModel.getAttemptFlow(student.id, exam.id)`!
                    // Yes, we can collect from `viewModel.getAttemptFlow(student.id, exam.id)`!
                    // Let's write a simple helper Composable or collect in LaunchedEffect!
                }
        }
    }

    // Let's collect attempts reactively for each exam
    exams.forEach { exam ->
        val attemptFlow = remember(exam, student) { viewModel.getAttemptFlow(student.id, exam.id) }
        val attemptState by attemptFlow.collectAsState(initial = null)
        LaunchedEffect(attemptState) {
            attemptsMap[exam.id] = attemptState
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Student Desk",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = student.schoolName ?: "Online Examination System",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.logout() },
                        modifier = Modifier.testTag("student_logout_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Logout",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Student Info Banner Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = "Student Icon",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(8.dp)
                    )

                    Column {
                        Text(
                            text = "Welcome, ${student.name}!",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "Class: ${student.className} ${student.rollNumber?.let { "| Roll No: $it" } ?: ""}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Text(
                text = "Your Examinations",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            // Only show published exams to students!
            val publishedExams = remember(exams) { exams.filter { it.isPublished } }

            if (publishedExams.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Inbox,
                            contentDescription = "Empty",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No examinations scheduled for you yet.\nThey will appear here once published by the controller.",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(publishedExams) { exam ->
                        val attempt = attemptsMap[exam.id]
                        StudentExamCard(
                            exam = exam,
                            attempt = attempt,
                            onStart = { viewModel.enterExamScreen(exam) },
                            onViewResult = {
                                viewModel.loadResultDetails(exam.id, attempt?.id ?: 0)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StudentExamCard(
    exam: Exam,
    attempt: ExamAttempt?,
    onStart: () -> Unit,
    onViewResult: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
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
                Column {
                    Text(
                        text = exam.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Duration: ${exam.durationMinutes} Minutes | Questions: 100",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Status Pill Indicator
                val statusText: String
                val statusColor: Color
                val containerColor: Color

                if (attempt == null) {
                    statusText = "Ready to Start"
                    statusColor = MaterialTheme.colorScheme.primary
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                } else if (!attempt.isSubmitted) {
                    statusText = "In Progress"
                    statusColor = Color(0xFFFF9800) // Orange
                    containerColor = Color(0xFFFFF3E0)
                } else if (!exam.isResultPublished) {
                    statusText = "Result Pending"
                    statusColor = Color(0xFFFBC02D) // Yellow-gold
                    containerColor = Color(0xFFFFFDE7)
                } else {
                    statusText = "Graded"
                    statusColor = Color(0xFF4CAF50) // Green
                    containerColor = Color(0xFFE8F5E9)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(containerColor)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = statusText,
                        color = statusColor,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            Divider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Scheduled: ${exam.date} at ${exam.startTime}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (attempt == null) {
                    Button(
                        onClick = onStart,
                        modifier = Modifier.testTag("start_exam_btn_${exam.id}"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Play", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Start Exam")
                    }
                } else if (!attempt.isSubmitted) {
                    Button(
                        onClick = onStart,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800), contentColor = Color.White),
                        modifier = Modifier.testTag("resume_exam_btn_${exam.id}"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Resume", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Resume Exam")
                    }
                } else if (!exam.isResultPublished) {
                    // Pending. Show disabled button so it's clear they can't click it yet
                    Button(
                        onClick = {},
                        enabled = false,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = "Locked", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Results Locked")
                    }
                } else {
                    // Results Published! Clickable to view details
                    Button(
                        onClick = onViewResult,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50), contentColor = Color.White),
                        modifier = Modifier.testTag("view_results_btn_${exam.id}"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Correct", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("View Score")
                    }
                }
            }
        }
    }
}
