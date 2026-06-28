package com.example.ui

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class Screen {
    object Login : Screen()
    object AdminDashboard : Screen()
    object StudentDashboard : Screen()
    data class ExamTaking(val examId: Int, val attemptId: Int) : Screen()
    data class ExamResults(val examId: Int, val attemptId: Int) : Screen()
}

sealed class UserSession {
    object Admin : UserSession()
    data class StudentSession(val student: Student) : UserSession()
}

class ExamViewModel(
    private val repository: ExamRepository,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    // --- Authentication & Session States ---
    private val _currentUser = MutableStateFlow<UserSession?>(null)
    val currentUser: StateFlow<UserSession?> = _currentUser.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    // --- Screen Navigation State ---
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Login)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // --- Admin Data Flows ---
    val studentsList: StateFlow<List<Student>> = repository.getAllStudentsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val examsList: StateFlow<List<Exam>> = repository.getAllExamsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Active Testing State (For Students) ---
    private val _activeExam = MutableStateFlow<Exam?>(null)
    val activeExam: StateFlow<Exam?> = _activeExam.asStateFlow()

    private val _activeAttempt = MutableStateFlow<ExamAttempt?>(null)
    val activeAttempt: StateFlow<ExamAttempt?> = _activeAttempt.asStateFlow()

    private val _activeQuestions = MutableStateFlow<List<ExamQuestion>>(emptyList())
    val activeQuestions: StateFlow<List<ExamQuestion>> = _activeQuestions.asStateFlow()

    // questionIndex -> answerText
    private val _studentAnswers = MutableStateFlow<Map<Int, String>>(emptyMap())
    val studentAnswers: StateFlow<Map<Int, String>> = _studentAnswers.asStateFlow()

    // Timer States
    private val _timeLeftSeconds = MutableStateFlow<Long>(0)
    val timeLeftSeconds: StateFlow<Long> = _timeLeftSeconds.asStateFlow()

    private var timerJob: Job? = null

    // For results viewing
    private val _viewingResultsAttempt = MutableStateFlow<ExamAttempt?>(null)
    val viewingResultsAttempt: StateFlow<ExamAttempt?> = _viewingResultsAttempt.asStateFlow()

    private val _viewingResultsQuestions = MutableStateFlow<List<ExamQuestion>>(emptyList())
    val viewingResultsQuestions: StateFlow<List<ExamQuestion>> = _viewingResultsQuestions.asStateFlow()

    private val _viewingResultsAnswers = MutableStateFlow<List<StudentAnswer>>(emptyList())
    val viewingResultsAnswers: StateFlow<List<StudentAnswer>> = _viewingResultsAnswers.asStateFlow()

    // Admin monitoring states
    private val _monitoringAttempts = MutableStateFlow<List<AttemptWithStudent>>(emptyList())
    val monitoringAttempts: StateFlow<List<AttemptWithStudent>> = _monitoringAttempts.asStateFlow()
    private var monitoringJob: Job? = null

    init {
        // Clear login error when screen changes
        viewModelScope.launch {
            _currentScreen.collect {
                _loginError.value = null
            }
        }
    }

    // -------------------------------------------------------------
    // Authentication Operations
    // -------------------------------------------------------------

    fun login(username: String, passwordText: String) {
        viewModelScope.launch {
            _loginError.value = null
            val cleanedUsername = username.trim()

            if (cleanedUsername == "admin") {
                // Check against stored admin password hash or default to admin123
                val defaultHash = SecurityUtils.hashPassword("admin123")
                val savedHash = sharedPreferences.getString("admin_password_hash", defaultHash) ?: defaultHash
                val enteredHash = SecurityUtils.hashPassword(passwordText)

                if (enteredHash == savedHash) {
                    _currentUser.value = UserSession.Admin
                    _currentScreen.value = Screen.AdminDashboard
                } else {
                    _loginError.value = "Invalid administrator password."
                }
            } else {
                // Check student
                val student = repository.getStudentByUsername(cleanedUsername)
                if (student != null) {
                    val enteredHash = SecurityUtils.hashPassword(passwordText)
                    if (student.passwordHash == enteredHash) {
                        if (student.isActive) {
                            _currentUser.value = UserSession.StudentSession(student)
                            _currentScreen.value = Screen.StudentDashboard
                        } else {
                            _loginError.value = "Your account is currently disabled. Please contact your parents/admin."
                        }
                    } else {
                        _loginError.value = "Invalid password."
                    }
                } else {
                    _loginError.value = "Username not found."
                }
            }
        }
    }

    fun logout() {
        stopTimer()
        stopMonitoring()
        _currentUser.value = null
        _currentScreen.value = Screen.Login
        _activeExam.value = null
        _activeAttempt.value = null
        _activeQuestions.value = emptyList()
        _studentAnswers.value = emptyMap()
    }

    fun changeAdminPassword(old: String, new: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val defaultHash = SecurityUtils.hashPassword("admin123")
        val savedHash = sharedPreferences.getString("admin_password_hash", defaultHash) ?: defaultHash
        val oldHash = SecurityUtils.hashPassword(old)

        if (oldHash != savedHash) {
            onError("Incorrect old password.")
            return
        }
        if (new.trim().length < 4) {
            onError("Password must be at least 4 characters long.")
            return
        }

        val newHash = SecurityUtils.hashPassword(new)
        sharedPreferences.edit().putString("admin_password_hash", newHash).apply()
        onSuccess()
    }

    // -------------------------------------------------------------
    // Admin Student CRUD Operations
    // -------------------------------------------------------------

    fun registerStudent(
        name: String,
        username: String,
        passwordText: String,
        className: String,
        rollNumber: String?,
        schoolName: String?,
        mobileNumber: String?,
        parentName: String?,
        isActive: Boolean,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val trimmedUsername = username.trim()
            if (trimmedUsername.isEmpty() || name.trim().isEmpty() || passwordText.isEmpty() || className.trim().isEmpty()) {
                onError("Please fill in all mandatory fields.")
                return@launch
            }

            val existing = repository.getStudentByUsername(trimmedUsername)
            if (existing != null || trimmedUsername == "admin") {
                onError("Username is already taken.")
                return@launch
            }

            val hash = SecurityUtils.hashPassword(passwordText)
            val student = Student(
                name = name.trim(),
                username = trimmedUsername,
                passwordHash = hash,
                className = className.trim(),
                rollNumber = rollNumber?.trim()?.ifEmpty { null },
                schoolName = schoolName?.trim()?.ifEmpty { null },
                mobileNumber = mobileNumber?.trim()?.ifEmpty { null },
                parentName = parentName?.trim()?.ifEmpty { null },
                isActive = isActive
            )

            repository.createStudent(student)
            onSuccess()
        }
    }

    fun updateStudentDetails(
        id: Int,
        name: String,
        username: String,
        newPasswordText: String?,
        className: String,
        rollNumber: String?,
        schoolName: String?,
        mobileNumber: String?,
        parentName: String?,
        isActive: Boolean,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val trimmedUsername = username.trim()
            if (trimmedUsername.isEmpty() || name.trim().isEmpty() || className.trim().isEmpty()) {
                onError("Please fill in all mandatory fields.")
                return@launch
            }

            val existing = repository.getStudentByUsername(trimmedUsername)
            if (existing != null && existing.id != id) {
                onError("Username is already taken.")
                return@launch
            }

            val currentStudent = repository.getStudentById(id)
            if (currentStudent == null) {
                onError("Student not found.")
                return@launch
            }

            val hash = if (!newPasswordText.isNullOrEmpty()) {
                SecurityUtils.hashPassword(newPasswordText)
            } else {
                currentStudent.passwordHash
            }

            val updated = currentStudent.copy(
                name = name.trim(),
                username = trimmedUsername,
                passwordHash = hash,
                className = className.trim(),
                rollNumber = rollNumber?.trim()?.ifEmpty { null },
                schoolName = schoolName?.trim()?.ifEmpty { null },
                mobileNumber = mobileNumber?.trim()?.ifEmpty { null },
                parentName = parentName?.trim()?.ifEmpty { null },
                isActive = isActive
            )

            repository.updateStudent(updated)
            onSuccess()
        }
    }

    fun deleteStudent(student: Student) {
        viewModelScope.launch {
            repository.deleteStudentById(student.id)
        }
    }

    // -------------------------------------------------------------
    // Admin Exam CRUD Operations
    // -------------------------------------------------------------

    fun scheduleExam(
        name: String,
        date: String,
        startTime: String,
        durationMinutes: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            if (name.trim().isEmpty() || date.trim().isEmpty() || startTime.trim().isEmpty()) {
                onError("Please fill in all details.")
                return@launch
            }

            val exam = Exam(
                name = name.trim(),
                date = date,
                startTime = startTime,
                durationMinutes = durationMinutes,
                isPublished = false,
                isResultPublished = false
            )

            repository.createExam(exam)
            onSuccess()
        }
    }

    fun toggleExamPublish(exam: Exam) {
        viewModelScope.launch {
            repository.updateExam(exam.copy(isPublished = !exam.isPublished))
        }
    }

    fun toggleResultPublish(exam: Exam) {
        viewModelScope.launch {
            repository.updateExam(exam.copy(isResultPublished = !exam.isResultPublished))
        }
    }

    fun deleteExam(examId: Int) {
        viewModelScope.launch {
            repository.deleteExamById(examId)
        }
    }

    // -------------------------------------------------------------
    // Admin Exam Monitoring
    // -------------------------------------------------------------

    fun startMonitoringExam(examId: Int) {
        stopMonitoring()
        monitoringJob = viewModelScope.launch {
            repository.getAttemptsWithStudentForExamFlow(examId).collect { list ->
                _monitoringAttempts.value = list
            }
        }
    }

    fun stopMonitoring() {
        monitoringJob?.cancel()
        monitoringJob = null
        _monitoringAttempts.value = emptyList()
    }

    // -------------------------------------------------------------
    // Student Testing / Attempt Operations
    // -------------------------------------------------------------

    fun enterExamScreen(exam: Exam) {
        val session = currentUser.value as? UserSession.StudentSession ?: return
        viewModelScope.launch {
            val attempt = repository.startAttempt(session.student.id, exam.id)
            val questions = repository.getQuestionsForExam(exam.id)

            _activeExam.value = exam
            _activeAttempt.value = attempt
            _activeQuestions.value = questions

            // Load any existing intermediate answers from DB (in case of crash/re-entry)
            val answers = repository.getAnswersForAttempt(attempt.id)
            val loadedAnswersMap = answers.associate { it.questionIndex to it.studentAnswerText }
            _studentAnswers.value = loadedAnswersMap

            _currentScreen.value = Screen.ExamTaking(exam.id, attempt.id)

            // Start countdown timer
            startTimer(exam, attempt)
        }
    }

    private fun startTimer(exam: Exam, attempt: ExamAttempt) {
        stopTimer()
        timerJob = viewModelScope.launch {
            val totalDurationMillis = exam.durationMinutes * 60 * 1000L
            while (true) {
                val elapsedMillis = System.currentTimeMillis() - attempt.startTimeMillis
                val remainingMillis = totalDurationMillis - elapsedMillis
                val remainingSeconds = remainingMillis / 1000

                if (remainingSeconds <= 0) {
                    _timeLeftSeconds.value = 0
                    // Trigger auto submission
                    submitActiveExam()
                    break
                } else {
                    _timeLeftSeconds.value = remainingSeconds
                }
                delay(1000)
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
        _timeLeftSeconds.value = 0
    }

    fun updateStudentAnswer(questionIndex: Int, answer: String) {
        // Local memory update
        _studentAnswers.value = _studentAnswers.value.toMutableMap().apply {
            put(questionIndex, answer)
        }
    }

    fun submitActiveExam() {
        val attempt = _activeAttempt.value ?: return
        stopTimer()
        viewModelScope.launch {
            val finalAttempt = repository.submitAttempt(attempt.id, _studentAnswers.value)
            _activeAttempt.value = finalAttempt

            // Stay on exam screen or show a results pending screen
            // Since student dashboard lists exams, they will see 'Result Pending'
            // We can show a full-screen confirmation and then navigate to Dashboard
        }
    }

    fun exitActiveExamScreen() {
        stopTimer()
        _activeExam.value = null
        _activeAttempt.value = null
        _activeQuestions.value = emptyList()
        _studentAnswers.value = emptyMap()
        _currentScreen.value = Screen.StudentDashboard
    }

    // -------------------------------------------------------------
    // Results & Reports View Operations
    // -------------------------------------------------------------

    fun loadResultDetails(examId: Int, attemptId: Int) {
        viewModelScope.launch {
            val attempt = repository.getAttemptFlow(
                (currentUser.value as? UserSession.StudentSession)?.student?.id ?: 0,
                examId
            ).firstOrNull() ?: return@launch

            val questions = repository.getQuestionsForExam(examId)
            val answers = repository.getAnswersForAttempt(attemptId)

            _viewingResultsAttempt.value = attempt
            _viewingResultsQuestions.value = questions
            _viewingResultsAnswers.value = answers

            _currentScreen.value = Screen.ExamResults(examId, attemptId)
        }
    }

    fun loadResultDetailsForAdmin(studentId: Int, examId: Int, attemptId: Int) {
        viewModelScope.launch {
            val questions = repository.getQuestionsForExam(examId)
            val answers = repository.getAnswersForAttempt(attemptId)

            // Since we need attempt details, let's look through monitored list or query
            val attemptsList = repository.getAttemptsWithStudentForExam(examId)
            val attemptWithStudent = attemptsList.find { it.attempt.id == attemptId } ?: return@launch

            _viewingResultsAttempt.value = attemptWithStudent.attempt
            _viewingResultsQuestions.value = questions
            _viewingResultsAnswers.value = answers

            _currentScreen.value = Screen.ExamResults(examId, attemptId)
        }
    }

    fun exitResultsView() {
        _viewingResultsAttempt.value = null
        _viewingResultsQuestions.value = emptyList()
        _viewingResultsAnswers.value = emptyList()

        if (currentUser.value is UserSession.Admin) {
            _currentScreen.value = Screen.AdminDashboard
        } else {
            _currentScreen.value = Screen.StudentDashboard
        }
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun getAttemptFlow(studentId: Int, examId: Int): Flow<ExamAttempt?> {
        return repository.getAttemptFlow(studentId, examId)
    }

    fun deserializeOptions(optionsJson: String?): List<String> {
        return repository.deserializeOptions(optionsJson)
    }
}

class ExamViewModelFactory(
    private val repository: ExamRepository,
    private val sharedPreferences: SharedPreferences
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExamViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExamViewModel(repository, sharedPreferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
