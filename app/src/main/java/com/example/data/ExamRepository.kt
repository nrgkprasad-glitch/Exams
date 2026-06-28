package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.security.MessageDigest

object SecurityUtils {
    fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(password.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}

class ExamRepository(private val database: AppDatabase) {

    private val studentDao = database.studentDao()
    private val examDao = database.examDao()
    private val questionDao = database.questionDao()
    private val attemptDao = database.attemptDao()

    // -------------------------------------------------------------
    // Student Operations
    // -------------------------------------------------------------

    fun getAllStudentsFlow(): Flow<List<Student>> = studentDao.getAllStudentsFlow()

    suspend fun getAllStudents(): List<Student> = withContext(Dispatchers.IO) {
        studentDao.getAllStudents()
    }

    suspend fun getStudentByUsername(username: String): Student? = withContext(Dispatchers.IO) {
        studentDao.getStudentByUsername(username)
    }

    suspend fun getStudentById(id: Int): Student? = withContext(Dispatchers.IO) {
        studentDao.getStudentById(id)
    }

    suspend fun createStudent(student: Student): Long = withContext(Dispatchers.IO) {
        studentDao.insertStudent(student)
    }

    suspend fun updateStudent(student: Student) = withContext(Dispatchers.IO) {
        studentDao.updateStudent(student)
    }

    suspend fun deleteStudentById(id: Int) = withContext(Dispatchers.IO) {
        studentDao.deleteStudentById(id)
    }

    // -------------------------------------------------------------
    // Exam Operations
    // -------------------------------------------------------------

    fun getAllExamsFlow(): Flow<List<Exam>> = examDao.getAllExamsFlow()

    suspend fun getExamById(id: Int): Exam? = withContext(Dispatchers.IO) {
        examDao.getExamById(id)
    }

    suspend fun createExam(exam: Exam): Long = withContext(Dispatchers.IO) {
        val examId = examDao.insertExam(exam).toInt()

        // Generate and insert questions for this exam immediately
        val questions = generateQuestionsForExam(examId)
        questionDao.insertQuestions(questions)

        examId.toLong()
    }

    suspend fun updateExam(exam: Exam) = withContext(Dispatchers.IO) {
        examDao.updateExam(exam)
    }

    suspend fun deleteExamById(id: Int) = withContext(Dispatchers.IO) {
        // Cascade delete questions
        questionDao.deleteQuestionsForExam(id)
        examDao.deleteExamById(id)
    }

    // -------------------------------------------------------------
    // Question Operations
    // -------------------------------------------------------------

    suspend fun getQuestionsForExam(examId: Int): List<ExamQuestion> = withContext(Dispatchers.IO) {
        questionDao.getQuestionsForExam(examId)
    }

    private fun generateQuestionsForExam(examId: Int): List<ExamQuestion> {
        val questions = mutableListOf<ExamQuestion>()

        // Generate 60 procedural Math questions
        val mathTemplates = MathQuestionGenerator.generate60Questions()
        mathTemplates.forEachIndexed { i, q ->
            questions.add(
                ExamQuestion(
                    examId = examId,
                    questionIndex = i, // 0..59
                    type = "MATH",
                    category = q.category,
                    questionText = q.text,
                    optionsJson = null,
                    correctAnswer = q.answer,
                    difficulty = "Medium"
                )
            )
        }

        // Generate 40 procedural GK questions
        val gkTemplates = GKQuestionGenerator.generate40Questions()
        gkTemplates.forEachIndexed { i, q ->
            questions.add(
                ExamQuestion(
                    examId = examId,
                    questionIndex = 60 + i, // 60..99
                    type = "GK",
                    category = q.topic,
                    questionText = q.text,
                    optionsJson = q.options.joinToString("###"), // Delimiter serialization
                    correctAnswer = q.correctAnswer,
                    difficulty = q.difficulty
                )
            )
        }

        return questions
    }

    // Deserialize options helper using delimiter
    fun deserializeOptions(optionsJson: String?): List<String> {
        if (optionsJson.isNullOrEmpty()) return emptyList()
        return optionsJson.split("###")
    }

    // -------------------------------------------------------------
    // Exam Attempt Operations
    // -------------------------------------------------------------

    suspend fun getAttempt(studentId: Int, examId: Int): ExamAttempt? = withContext(Dispatchers.IO) {
        attemptDao.getAttempt(studentId, examId)
    }

    fun getAttemptFlow(studentId: Int, examId: Int): Flow<ExamAttempt?> {
        return attemptDao.getAttemptFlow(studentId, examId)
    }

    fun getAttemptsWithStudentForExamFlow(examId: Int): Flow<List<AttemptWithStudent>> {
        return attemptDao.getAttemptsWithStudentForExamFlow(examId)
    }

    suspend fun getAttemptsWithStudentForExam(examId: Int): List<AttemptWithStudent> = withContext(Dispatchers.IO) {
        attemptDao.getAttemptsWithStudentForExam(examId)
    }

    suspend fun startAttempt(studentId: Int, examId: Int): ExamAttempt = withContext(Dispatchers.IO) {
        val existing = attemptDao.getAttempt(studentId, examId)
        if (existing != null) return@withContext existing

        val attempt = ExamAttempt(
            studentId = studentId,
            examId = examId,
            startTimeMillis = System.currentTimeMillis(),
            isSubmitted = false
        )
        val id = attemptDao.insertAttempt(attempt).toInt()
        attempt.copy(id = id)
    }

    suspend fun getAnswersForAttempt(attemptId: Int): List<StudentAnswer> = withContext(Dispatchers.IO) {
        attemptDao.getAnswersForAttempt(attemptId)
    }

    fun getAnswersForAttemptFlow(attemptId: Int): Flow<List<StudentAnswer>> {
        return attemptDao.getAnswersForAttemptFlow(attemptId)
    }

    suspend fun submitAttempt(
        attemptId: Int,
        answersMap: Map<Int, String> // questionIndex -> studentAnswerText
    ): ExamAttempt? = withContext(Dispatchers.IO) {
        val attempt = attemptDao.getAttemptById(attemptId) ?: return@withContext null
        if (attempt.isSubmitted) return@withContext attempt

        // Fetch the exam questions for this exam
        val questions = questionDao.getQuestionsForExam(attempt.examId)

        var correctCount = 0
        var wrongCount = 0
        var unansweredCount = 0

        val studentAnswers = mutableListOf<StudentAnswer>()

        for (q in questions) {
            val rawAns = answersMap[q.questionIndex]
            val cleanedAns = rawAns?.trim() ?: ""

            val isCorrect: Boolean
            if (cleanedAns.isEmpty()) {
                unansweredCount++
                isCorrect = false
            } else {
                // Perform comparison
                isCorrect = if (q.type == "MATH") {
                    cleanedAns == q.correctAnswer.trim()
                } else {
                    cleanedAns.equals(q.correctAnswer.trim(), ignoreCase = true)
                }

                if (isCorrect) {
                    correctCount++
                } else {
                    wrongCount++
                }
            }

            studentAnswers.add(
                StudentAnswer(
                    attemptId = attemptId,
                    questionIndex = q.questionIndex,
                    studentAnswerText = cleanedAns,
                    isCorrect = isCorrect
                )
            )
        }

        // Save answers in bulk
        attemptDao.insertAnswers(studentAnswers)

        // Update the attempt object
        val finalAttempt = attempt.copy(
            endTimeMillis = System.currentTimeMillis(),
            isSubmitted = true,
            score = correctCount, // 1 point per question
            correctAnswersCount = correctCount,
            wrongAnswersCount = wrongCount,
            unansweredCount = unansweredCount
        )

        attemptDao.updateAttempt(finalAttempt)
        finalAttempt
    }
}
