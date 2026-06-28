package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Exam
import com.example.data.ExamAttempt
import com.example.data.ExamQuestion

@Composable
fun ExamActiveScreen(
    viewModel: ExamViewModel,
    modifier: Modifier = Modifier
) {
    val exam by viewModel.activeExam.collectAsState()
    val attempt by viewModel.activeAttempt.collectAsState()
    val questions by viewModel.activeQuestions.collectAsState()
    val answers by viewModel.studentAnswers.collectAsState()
    val timeLeftSeconds by viewModel.timeLeftSeconds.collectAsState()

    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var showSubmitDialog by remember { mutableStateOf(false) }

    if (exam == null || attempt == null || questions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val activeExam = exam!!
    val activeAttempt = attempt!!

    // Auto-submission state check
    if (activeAttempt.isSubmitted) {
        // Show successful auto-submission/submission state card!
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 500.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Success",
                        tint = Color.White,
                        modifier = Modifier
                            .size(64.dp)
                            .background(Color(0xFF4CAF50), RoundedCornerShape(32.dp))
                            .padding(12.dp)
                    )

                    Text(
                        text = "Examination Submitted",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Your examination has been submitted successfully. The results will be published by the Administrator.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { viewModel.exitActiveExamScreen() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("exit_exam_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Back to Student Desk", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        return
    }

    // Main Exam Interface
    val minutes = timeLeftSeconds / 60
    val seconds = timeLeftSeconds % 60
    val timerString = String.format("%02d:%02d", minutes, seconds)

    // Timer warnings
    val timerColor = if (timeLeftSeconds < 60) {
        MaterialTheme.colorScheme.error // Last 1 min is red
    } else if (timeLeftSeconds < 300) {
        Color(0xFFFF9800) // Orange under 5 mins
    } else {
        MaterialTheme.colorScheme.primary
    }

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = activeExam.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1
                        )
                        Text(
                            text = "100 Questions | Progress: ${answers.size}/100 answered",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Countdown timer widget
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(timerColor.copy(alpha = 0.12f))
                            .border(1.dp, timerColor, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.HourglassEmpty,
                            contentDescription = "Timer icon",
                            tint = timerColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = timerString,
                            color = timerColor,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            if (currentQuestionIndex > 0) currentQuestionIndex--
                        },
                        enabled = currentQuestionIndex > 0
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Prev")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Prev")
                    }

                    Button(
                        onClick = { showSubmitDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("submit_exam_btn")
                    ) {
                        Text("Finish & Submit", fontWeight = FontWeight.Bold)
                    }

                    TextButton(
                        onClick = {
                            if (currentQuestionIndex < 99) currentQuestionIndex++
                        },
                        enabled = currentQuestionIndex < 99
                    ) {
                        Text("Next")
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = "Next")
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Horizontal grid selector for jumping to questions
            Text(
                text = "Question Grid Navigator (1 to 100)",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 40.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceContainerLow,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                itemsIndexed(questions) { idx, q ->
                    val isCurrent = idx == currentQuestionIndex
                    val isAnswered = answers.containsKey(idx) && !answers[idx].isNullOrBlank()

                    val containerColor = when {
                        isCurrent -> MaterialTheme.colorScheme.primary
                        isAnswered -> Color(0xFF4CAF50).copy(alpha = 0.2f) // Light green for answered
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }

                    val contentColor = when {
                        isCurrent -> MaterialTheme.colorScheme.onPrimary
                        isAnswered -> Color(0xFF2E7D32) // Dark green text
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }

                    val border = if (isAnswered && !isCurrent) {
                        BorderStroke(1.dp, Color(0xFF4CAF50))
                    } else if (isCurrent) {
                        BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                    } else {
                        null
                    }

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(containerColor)
                            .then(if (border != null) Modifier.border(border, RoundedCornerShape(8.dp)) else Modifier)
                            .clickable { currentQuestionIndex = idx }
                            .testTag("nav_question_$idx"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (idx + 1).toString(),
                            color = contentColor,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // Current question detail card
            val currentQuestion = questions[currentQuestionIndex]
            val currentAnswer = answers[currentQuestionIndex] ?: ""

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Question Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SuggestionChip(
                            onClick = {},
                            label = { Text("Question ${currentQuestionIndex + 1} of 100") }
                        )

                        val categoryColor = if (currentQuestion.type == "MATH") {
                            MaterialTheme.colorScheme.secondaryContainer
                        } else {
                            MaterialTheme.colorScheme.tertiaryContainer
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(categoryColor)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = currentQuestion.type,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = "Category: ${currentQuestion.category}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Question Text
                    Text(
                        text = currentQuestion.questionText,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            lineHeight = 32.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // Answer input area
                    if (currentQuestion.type == "MATH") {
                        // Arithmetic Math - text field input
                        OutlinedTextField(
                            value = currentAnswer,
                            onValueChange = { viewModel.updateStudentAnswer(currentQuestionIndex, it) },
                            label = { Text("Type your answer below") },
                            placeholder = { Text("e.g. 540") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("math_answer_input"),
                            shape = RoundedCornerShape(12.dp)
                        )
                    } else {
                        // General Knowledge - multiple choice option cards
                        val options = viewModel.deserializeOptions(currentQuestion.optionsJson)
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            options.forEachIndexed { optIdx, option ->
                                val isSelected = currentAnswer == option
                                val optBgColor = if (isSelected) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surface
                                }
                                val optBorderColor = if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.outlineVariant
                                }

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.updateStudentAnswer(currentQuestionIndex, option)
                                        }
                                        .testTag("gk_option_$optIdx"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = optBgColor),
                                    border = BorderStroke(1.dp, optBorderColor)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = {
                                                viewModel.updateStudentAnswer(currentQuestionIndex, option)
                                            }
                                        )
                                        Text(
                                            text = option,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Manual Submit Confirmation Dialog
    if (showSubmitDialog) {
        AlertDialog(
            onDismissRequest = { showSubmitDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = "Warning", tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Submit Examination?") },
            text = {
                val answeredCount = answers.size
                val unansweredCount = 100 - answeredCount
                Text("You have answered $answeredCount of 100 questions.\n\nYou have $unansweredCount unanswered questions.\n\nAre you sure you want to finalize and submit? You will not be able to change any answers once submitted.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSubmitDialog = false
                        viewModel.submitActiveExam()
                    }
                ) {
                    Text("Submit Exam")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSubmitDialog = false }) {
                    Text("Go Back")
                }
            }
        )
    }
}
