package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "students",
    indices = [Index(value = ["username"], unique = true)]
)
data class Student(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val username: String,
    val passwordHash: String, // Securely stored hashed password
    val className: String,
    val rollNumber: String? = null,
    val schoolName: String? = null,
    val mobileNumber: String? = null,
    val parentName: String? = null,
    val isActive: Boolean = true
)

@Entity(tableName = "exams")
data class Exam(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val date: String, // YYYY-MM-DD
    val startTime: String, // HH:MM
    val durationMinutes: Int, // 30, 45, 60, 90, 120
    val totalQuestions: Int = 100,
    val mathQuestionsCount: Int = 60,
    val gkQuestionsCount: Int = 40,
    val isPublished: Boolean = false, // locked until admin publishes
    val isResultPublished: Boolean = false // results hidden until admin publishes
)

@Entity(
    tableName = "exam_questions",
    indices = [Index(value = ["examId"])]
)
data class ExamQuestion(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val examId: Int,
    val questionIndex: Int, // 0..99
    val type: String, // "MATH" or "GK"
    val category: String, // category name
    val questionText: String,
    val optionsJson: String?, // List of 4 options serialized as JSON for GK MCQs, null for Math
    val correctAnswer: String, // correct option string or numeric string
    val difficulty: String // "Easy", "Medium", "Hard", "Olympiad"
)

@Entity(
    tableName = "exam_attempts",
    indices = [Index(value = ["studentId", "examId"], unique = true)] // A student can attempt an exam only once
)
data class ExamAttempt(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val studentId: Int,
    val examId: Int,
    val startTimeMillis: Long, // Start timestamp
    val endTimeMillis: Long = 0, // Submission timestamp
    val isSubmitted: Boolean = false,
    val score: Int = 0,
    val correctAnswersCount: Int = 0,
    val wrongAnswersCount: Int = 0,
    val unansweredCount: Int = 0
)

@Entity(
    tableName = "student_answers",
    indices = [Index(value = ["attemptId", "questionIndex"], unique = true)]
)
data class StudentAnswer(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val attemptId: Int,
    val questionIndex: Int, // 0..99
    val studentAnswerText: String,
    val isCorrect: Boolean = false
)
