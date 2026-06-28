package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.example.data.ExamAttempt
import com.example.data.ExamQuestion
import com.example.data.StudentAnswer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamResultsScreen(
    viewModel: ExamViewModel,
    modifier: Modifier = Modifier
) {
    val attempt by viewModel.viewingResultsAttempt.collectAsState()
    val questions by viewModel.viewingResultsQuestions.collectAsState()
    val answers by viewModel.viewingResultsAnswers.collectAsState()

    if (attempt == null || questions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val activeAttempt = attempt!!
    val percentage = activeAttempt.score // Score is out of 100
    val grade = when {
        percentage >= 90 -> "A+"
        percentage >= 80 -> "A"
        percentage >= 70 -> "B"
        percentage >= 60 -> "C"
        percentage >= 50 -> "D"
        else -> "F"
    }

    val durationMin = ((activeAttempt.endTimeMillis - activeAttempt.startTimeMillis) / 60000).toInt()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Performance Scorecard", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.exitResultsView() }, modifier = Modifier.testTag("results_back_btn")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Main score display card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "YOUR FINAL GRADE",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = grade,
                        style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold),
                        color = if (percentage >= 50) Color(0xFF4CAF50) else Color(0xFFF44336)
                    )

                    Text(
                        text = "Total Score: ${activeAttempt.score} / 100  ($percentage%)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Text(
                        text = "Time Taken: $durationMin Minutes",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Stat breakdown pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Correct
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    border = BorderStroke(1.dp, Color(0xFF81C784))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Correct", tint = Color(0xFF2E7D32))
                        Text("${activeAttempt.correctAnswersCount}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text("Correct", style = MaterialTheme.typography.labelSmall, color = Color(0xFF2E7D32))
                    }
                }

                // Wrong
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    border = BorderStroke(1.dp, Color(0xFFE57373))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Cancel, contentDescription = "Wrong", tint = Color(0xFFC62828))
                        Text("${activeAttempt.wrongAnswersCount}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text("Wrong", style = MaterialTheme.typography.labelSmall, color = Color(0xFFC62828))
                    }
                }

                // Unanswered
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDE7)),
                    border = BorderStroke(1.dp, Color(0xFFFFF176))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.HourglassEmpty, contentDescription = "Unanswered", tint = Color(0xFFF57F17))
                        Text("${activeAttempt.unansweredCount}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text("Skipped", style = MaterialTheme.typography.labelSmall, color = Color(0xFFF57F17))
                    }
                }
            }

            Text(
                text = "Graded Answer Key Review",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(top = 8.dp)
            )

            // Dynamic full answer key scroll lists
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(questions) { idx, q ->
                    // Find the student's answer for this index
                    val ansRecord = answers.find { it.questionIndex == q.questionIndex }
                    val studentAnswer = ansRecord?.studentAnswerText ?: ""
                    val isCorrect = ansRecord?.isCorrect ?: false

                    val itemBorderColor = when {
                        studentAnswer.isEmpty() -> Color(0xFFFFF176) // Yellow border for skipped
                        isCorrect -> Color(0xFF81C784) // Green border for correct
                        else -> Color(0xFFE57373) // Red border for wrong
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, itemBorderColor)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Question ${idx + 1} (${q.type})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                val textStatus: String
                                val colorStatus: Color
                                if (studentAnswer.isEmpty()) {
                                    textStatus = "Skipped"
                                    colorStatus = Color(0xFFF57F17)
                                } else if (isCorrect) {
                                    textStatus = "Correct"
                                    colorStatus = Color(0xFF2E7D32)
                                } else {
                                    textStatus = "Wrong"
                                    colorStatus = Color(0xFFC62828)
                                }

                                Text(
                                    text = textStatus,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = colorStatus
                                )
                            }

                            Text(
                                text = q.questionText,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Your Answer", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = studentAnswer.ifEmpty { "None" },
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (studentAnswer.isEmpty()) {
                                            Color.Gray
                                        } else if (isCorrect) {
                                            Color(0xFF2E7D32)
                                        } else {
                                            Color(0xFFC62828)
                                        }
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Correct Answer", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = q.correctAnswer,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF2E7D32)
                                    )
                                }
                            }

                            Text(
                                text = "Category: ${q.category} | Difficulty: ${q.difficulty}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
