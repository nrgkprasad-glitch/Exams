package com.example.ui

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import com.example.data.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboard(
    viewModel: ExamViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Students, 1 = Exams, 2 = Monitoring & Reports
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Admin Console",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "School Exam Controller",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    // Change admin password trigger
                    var showPasswordDialog by remember { mutableStateOf(false) }
                    IconButton(
                        onClick = { showPasswordDialog = true },
                        modifier = Modifier.testTag("admin_settings_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings")
                    }

                    IconButton(
                        onClick = { viewModel.logout() },
                        modifier = Modifier.testTag("admin_logout_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Logout",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }

                    if (showPasswordDialog) {
                        AdminChangePasswordDialog(
                            viewModel = viewModel,
                            onDismiss = { showPasswordDialog = false }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.People, contentDescription = "Students") },
                    label = { Text("Students") },
                    modifier = Modifier.testTag("nav_students_tab")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Assignment, contentDescription = "Examinations") },
                    label = { Text("Examinations") },
                    modifier = Modifier.testTag("nav_exams_tab")
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.BarChart, contentDescription = "Monitoring & Reports") },
                    label = { Text("Reports") },
                    modifier = Modifier.testTag("nav_reports_tab")
                )
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            when (selectedTab) {
                0 -> StudentManagementTab(viewModel)
                1 -> ExamManagementTab(viewModel)
                2 -> MonitoringAndReportsTab(viewModel)
            }
        }
    }
}

// -------------------------------------------------------------
// Change Password Dialog
// -------------------------------------------------------------
@Composable
fun AdminChangePasswordDialog(
    viewModel: ExamViewModel,
    onDismiss: () -> Unit
) {
    var oldPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var errMsg by remember { mutableStateOf<String?>(null) }
    var successMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Admin Password") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "You can change the administrator password from the default 'admin123' to keep your exam bank safe.",
                    style = MaterialTheme.typography.bodySmall
                )

                OutlinedTextField(
                    value = oldPass,
                    onValueChange = { oldPass = it },
                    label = { Text("Current Password") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = newPass,
                    onValueChange = { newPass = it },
                    label = { Text("New Password") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                errMsg?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                successMsg?.let {
                    Text(it, color = Color(0xFF4CAF50), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    errMsg = null
                    successMsg = null
                    viewModel.changeAdminPassword(
                        old = oldPass,
                        new = newPass,
                        onSuccess = {
                            successMsg = "Password changed successfully!"
                            oldPass = ""
                            newPass = ""
                        },
                        onError = { errMsg = it }
                    )
                }
            ) {
                Text("Update Password")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

// -------------------------------------------------------------
// STUDENT MANAGEMENT TAB
// -------------------------------------------------------------
@Composable
fun StudentManagementTab(viewModel: ExamViewModel) {
    val students by viewModel.studentsList.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingStudent by remember { mutableStateOf<Student?>(null) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedClassFilter by remember { mutableStateOf("All Classes") }

    val distinctClasses = remember(students) {
        listOf("All Classes") + students.map { it.className }.distinct().sorted()
    }

    val filteredStudents = remember(students, searchQuery, selectedClassFilter) {
        students.filter { student ->
            val matchesQuery = student.name.contains(searchQuery, ignoreCase = true) ||
                    student.username.contains(searchQuery, ignoreCase = true)
            val matchesClass = selectedClassFilter == "All Classes" || student.className == selectedClassFilter
            matchesQuery && matchesClass
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_student_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Register New Student")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Registered Students",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )

            // Search and filters
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by name/username") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("student_search"),
                    singleLine = true
                )

                // Simple Class Filter Dropdown (Scrollable Box in popup/sheet for simplicity)
                var expandedFilter by remember { mutableStateOf(false) }
                Box {
                    Button(
                        onClick = { expandedFilter = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                    ) {
                        Text(selectedClassFilter)
                        Icon(Icons.Default.FilterList, contentDescription = "Filter", modifier = Modifier.padding(start = 4.dp))
                    }
                    DropdownMenu(
                        expanded = expandedFilter,
                        onDismissRequest = { expandedFilter = false }
                    ) {
                        distinctClasses.forEach { cls ->
                            DropdownMenuItem(
                                text = { Text(cls) },
                                onClick = {
                                    selectedClassFilter = cls
                                    expandedFilter = false
                                }
                            )
                        }
                    }
                }
            }

            if (filteredStudents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No students registered yet.\nClick the '+' button to add your children/students.",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredStudents) { student ->
                        StudentCard(
                            student = student,
                            onEdit = { editingStudent = student },
                            onDelete = { viewModel.deleteStudent(student) }
                        )
                    }
                }
            }
        }

        if (showAddDialog) {
            StudentFormDialog(
                onDismiss = { showAddDialog = false },
                onSubmit = { name, username, password, className, roll, school, mobile, parent, isActive, onError ->
                    viewModel.registerStudent(
                        name, username, password, className, roll, school, mobile, parent, isActive,
                        onSuccess = { showAddDialog = false },
                        onError = onError
                    )
                }
            )
        }

        editingStudent?.let { student ->
            StudentFormDialog(
                student = student,
                onDismiss = { editingStudent = null },
                onSubmit = { name, username, password, className, roll, school, mobile, parent, isActive, onError ->
                    viewModel.updateStudentDetails(
                        id = student.id,
                        name = name,
                        username = username,
                        newPasswordText = password.ifEmpty { null },
                        className = className,
                        rollNumber = roll,
                        schoolName = school,
                        mobileNumber = mobile,
                        parentName = parent,
                        isActive = isActive,
                        onSuccess = { editingStudent = null },
                        onError = onError
                    )
                }
            )
        }
    }
}

@Composable
fun StudentCard(
    student: Student,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = student.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    SuggestionChip(
                        onClick = {},
                        label = { Text(student.className) }
                    )
                }
                Text("Username: ${student.username}", style = MaterialTheme.typography.bodySmall)
                student.rollNumber?.let { Text("Roll No: $it", style = MaterialTheme.typography.bodySmall) }
                student.parentName?.let { Text("Parent: $it", style = MaterialTheme.typography.bodySmall) }

                // Active/Inactive status indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                color = if (student.isActive) Color(0xFF4CAF50) else Color(0xFFF44336),
                                shape = RoundedCornerShape(4.dp)
                            )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (student.isActive) "Active" else "Inactive",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (student.isActive) Color(0xFF4CAF50) else Color(0xFFF44336)
                    )
                }
            }

            Row {
                IconButton(onClick = onEdit, modifier = Modifier.testTag("edit_student_${student.username}")) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit student details", tint = MaterialTheme.colorScheme.primary)
                }

                var showDeleteConfirm by remember { mutableStateOf(false) }
                IconButton(onClick = { showDeleteConfirm = true }, modifier = Modifier.testTag("delete_student_${student.username}")) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete student", tint = MaterialTheme.colorScheme.error)
                }

                if (showDeleteConfirm) {
                    AlertDialog(
                        onDismissRequest = { showDeleteConfirm = false },
                        title = { Text("Delete Account") },
                        text = { Text("Are you sure you want to permanently delete student '${student.name}'? This will permanently wipe all exam logs for this student.") },
                        confirmButton = {
                            Button(
                                onClick = {
                                    showDeleteConfirm = false
                                    onDelete()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Delete")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDeleteConfirm = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun StudentFormDialog(
    student: Student? = null,
    onDismiss: () -> Unit,
    onSubmit: (
        name: String,
        username: String,
        passwordText: String,
        className: String,
        rollNumber: String?,
        schoolName: String?,
        mobileNumber: String?,
        parentName: String?,
        isActive: Boolean,
        onError: (String) -> Unit
    ) -> Unit
) {
    var name by remember { mutableStateOf(student?.name ?: "") }
    var username by remember { mutableStateOf(student?.username ?: "") }
    var password by remember { mutableStateOf("") }
    var className by remember { mutableStateOf(student?.className ?: "") }
    var rollNumber by remember { mutableStateOf(student?.rollNumber ?: "") }
    var schoolName by remember { mutableStateOf(student?.schoolName ?: "") }
    var mobileNumber by remember { mutableStateOf(student?.mobileNumber ?: "") }
    var parentName by remember { mutableStateOf(student?.parentName ?: "") }
    var isActive by remember { mutableStateOf(student?.isActive ?: true) }

    var errMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (student == null) "Register Student" else "Edit Student details") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Student Full Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Login Username *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = student == null // Can't change username of existing students to prevent key breakage
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(if (student == null) "Login Password *" else "Reset Password (leave empty to keep current)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = className,
                    onValueChange = { className = it },
                    label = { Text("Class / Grade (e.g. Class 5) *") },
                    placeholder = { Text("e.g. Grade 5") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = rollNumber,
                    onValueChange = { rollNumber = it },
                    label = { Text("Roll Number (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = schoolName,
                    onValueChange = { schoolName = it },
                    label = { Text("School Name (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = parentName,
                    onValueChange = { parentName = it },
                    label = { Text("Parent Name (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = mobileNumber,
                    onValueChange = { mobileNumber = it },
                    label = { Text("Parent Mobile Number (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Active status toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Account Status (Active)")
                    Switch(
                        checked = isActive,
                        onCheckedChange = { isActive = it }
                    )
                }

                errMsg?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    errMsg = null
                    onSubmit(
                        name, username, password, className, rollNumber, schoolName, mobileNumber, parentName, isActive
                    ) { errMsg = it }
                }
            ) {
                Text(if (student == null) "Create Student" else "Save Details")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// -------------------------------------------------------------
// EXAMINATIONS MANAGEMENT TAB
// -------------------------------------------------------------
@Composable
fun ExamManagementTab(viewModel: ExamViewModel) {
    val exams by viewModel.examsList.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_exam_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create New Examination")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Scheduled Examinations",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )

            if (exams.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No examinations scheduled.\nClick '+' to schedule a Math & GK Examination.",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(exams) { exam ->
                        ExamManagementCard(
                            exam = exam,
                            onPublishToggle = { viewModel.toggleExamPublish(exam) },
                            onResultsToggle = { viewModel.toggleResultPublish(exam) },
                            onDelete = { viewModel.deleteExam(exam.id) }
                        )
                    }
                }
            }
        }

        if (showAddDialog) {
            ExamFormDialog(
                onDismiss = { showAddDialog = false },
                onSubmit = { name, date, time, duration, onError ->
                    viewModel.scheduleExam(
                        name = name,
                        date = date,
                        startTime = time,
                        durationMinutes = duration,
                        onSuccess = { showAddDialog = false },
                        onError = onError
                    )
                }
            )
        }
    }
}

@Composable
fun ExamManagementCard(
    exam: Exam,
    onPublishToggle: () -> Unit,
    onResultsToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (exam.isPublished) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (exam.isPublished) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        )
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
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = exam.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SuggestionChip(
                            onClick = {},
                            label = { Text("⏱ ${exam.durationMinutes} Mins") }
                        )
                        SuggestionChip(
                            onClick = {},
                            label = { Text("📐 60 Math | 🌍 40 GK") }
                        )
                    }
                }

                // Delete button
                var showDeleteConfirm by remember { mutableStateOf(false) }
                IconButton(onClick = { showDeleteConfirm = true }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete examination", tint = MaterialTheme.colorScheme.error)
                }

                if (showDeleteConfirm) {
                    AlertDialog(
                        onDismissRequest = { showDeleteConfirm = false },
                        title = { Text("Delete Examination?") },
                        text = { Text("Are you sure you want to delete this examination? This will wipe all exam questions and student attempt scores for this exam.") },
                        confirmButton = {
                            Button(
                                onClick = {
                                    showDeleteConfirm = false
                                    onDelete()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Delete")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDeleteConfirm = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }
            }

            Divider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column {
                    Text("Date", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(exam.date, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text("Start Time", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(exam.startTime, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Action switches
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Checkbox(
                        checked = exam.isPublished,
                        onCheckedChange = { onPublishToggle() },
                        modifier = Modifier.testTag("publish_checkbox_${exam.id}")
                    )
                    Column {
                        Text("Published (Live)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Text(
                            text = if (exam.isPublished) "Available to students" else "Locked (Unavailable)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Checkbox(
                        checked = exam.isResultPublished,
                        onCheckedChange = { onResultsToggle() },
                        enabled = exam.isPublished, // Result can only be published if the exam itself was published
                        modifier = Modifier.testTag("results_checkbox_${exam.id}")
                    )
                    Column {
                        Text("Results Published", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Text(
                            text = if (exam.isResultPublished) "Scores visible" else "Scores hidden",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ExamFormDialog(
    onDismiss: () -> Unit,
    onSubmit: (
        name: String,
        date: String,
        startTime: String,
        durationMinutes: Int,
        onError: (String) -> Unit
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("2026-06-28") }
    var startTime by remember { mutableStateOf("10:00") }
    var durationMinutes by remember { mutableIntStateOf(45) }

    var errMsg by remember { mutableStateOf<String?>(null) }
    val durations = listOf(30, 45, 60, 90, 120)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Schedule New Examination") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Each examination will automatically generate exactly 60 Mathematics and 40 General Knowledge randomized questions (100 total).",
                    style = MaterialTheme.typography.bodySmall
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Examination Name *") },
                    placeholder = { Text("e.g. Mid-Term Assessment") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date (YYYY-MM-DD) *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = startTime,
                    onValueChange = { startTime = it },
                    label = { Text("Start Time (e.g. 10:00 AM) *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Select Duration
                Text("Select Timer Duration (Minutes) *", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    durations.forEach { dur ->
                        val isSelected = durationMinutes == dur
                        FilterChip(
                            selected = isSelected,
                            onClick = { durationMinutes = dur },
                            label = { Text("$dur min") }
                        )
                    }
                }

                errMsg?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    errMsg = null
                    onSubmit(name, date, startTime, durationMinutes) { errMsg = it }
                }
            ) {
                Text("Schedule & Generate")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// -------------------------------------------------------------
// MONITORING AND REPORTS TAB
// -------------------------------------------------------------
@Composable
fun MonitoringAndReportsTab(viewModel: ExamViewModel) {
    val exams by viewModel.examsList.collectAsState()
    var selectedExam by remember { mutableStateOf<Exam?>(null) }

    // Start monitoring if an exam is selected
    LaunchedEffect(selectedExam) {
        if (selectedExam != null) {
            viewModel.startMonitoringExam(selectedExam!!.id)
        } else {
            viewModel.stopMonitoring()
        }
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.stopMonitoring() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (selectedExam == null) {
            Text(
                text = "Reports & Live Tracking",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "Select scheduled examination to monitor live attempts, view student reports, and export grading sheets:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (exams.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No scheduled exams available.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(exams) { exam ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedExam = exam }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(exam.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    Text("Scheduled: ${exam.date} at ${exam.startTime}", style = MaterialTheme.typography.bodySmall)
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 4.dp)) {
                                        SuggestionChip(
                                            onClick = {},
                                            label = { Text(if (exam.isPublished) "Live" else "Locked") }
                                        )
                                        SuggestionChip(
                                            onClick = {},
                                            label = { Text(if (exam.isResultPublished) "Results Published" else "Results Pending") }
                                        )
                                    }
                                }
                                Icon(Icons.Default.ArrowForward, contentDescription = "Open reports")
                            }
                        }
                    }
                }
            }
        } else {
            // Detailed exam tracking & reports screen
            ExamMonitoringConsole(
                exam = selectedExam!!,
                viewModel = viewModel,
                onBack = { selectedExam = null }
            )
        }
    }
}

@Composable
fun ExamMonitoringConsole(
    exam: Exam,
    viewModel: ExamViewModel,
    onBack: () -> Unit
) {
    val monitoredAttempts by viewModel.monitoringAttempts.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
        IconButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Go Back")
        }
        Text(
            text = exam.name,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        // Export button
        Button(
            onClick = {
                val reportText = buildExportReportText(exam, monitoredAttempts)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "Exam Report: ${exam.name}")
                    putExtra(Intent.EXTRA_TEXT, reportText)
                }
                context.startActivity(Intent.createChooser(intent, "Share/Export Exam Report"))
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
        ) {
            Icon(Icons.Default.Share, contentDescription = "Share Report", modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Export")
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total Questions: 100", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                Text("Duration: ${exam.durationMinutes} mins", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Status: ${if (exam.isPublished) "LIVE (Published)" else "LOCKED (Draft)"}", style = MaterialTheme.typography.bodySmall)
                Text("Results: ${if (exam.isResultPublished) "PUBLISHED" else "PENDING"}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }

    Text(
        text = "Student Progress & Scores",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        modifier = Modifier.padding(top = 8.dp)
    )

    if (monitoredAttempts.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "No student attempt recorded yet.\nWhen students start or complete the examination, their statuses will show up here.",
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(monitoredAttempts) { record ->
                val attempt = record.attempt
                val student = record.student

                val percentage = attempt.score // Since total questions is 100, score equals percentage!
                val grade = when {
                    percentage >= 90 -> "A+"
                    percentage >= 80 -> "A"
                    percentage >= 70 -> "B"
                    percentage >= 60 -> "C"
                    percentage >= 50 -> "D"
                    else -> "F"
                }

                val timeSpentMinutes = if (attempt.endTimeMillis > 0) {
                    ((attempt.endTimeMillis - attempt.startTimeMillis) / 60000).toInt()
                } else {
                    -1
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            // Let the admin click a student record to review their completed, detailed graded sheet!
                            viewModel.loadResultDetailsForAdmin(student.id, exam.id, attempt.id)
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(student.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                            Text("Class: ${student.className} | Roll: ${student.rollNumber ?: "N/A"}", style = MaterialTheme.typography.bodySmall)

                            if (attempt.isSubmitted) {
                                Text(
                                    text = "Completed in ${if (timeSpentMinutes >= 0) "$timeSpentMinutes mins" else "N/A"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF4CAF50),
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Correct: ${attempt.correctAnswersCount} | Wrong: ${attempt.wrongAnswersCount} | Unanswered: ${attempt.unansweredCount}",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            } else {
                                Text(
                                    text = "Taking Exam... (Live)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        if (attempt.isSubmitted) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${attempt.score}/100",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Grade: $grade ($percentage%)",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Click to review",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            Text(
                                text = "PENDING",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
}

// Helper to format a shareable text report of an exam
private fun buildExportReportText(exam: Exam, records: List<AttemptWithStudent>): String {
    val sb = java.lang.StringBuilder()
    sb.append("EXAMINATION REPORT: ${exam.name}\n")
    sb.append("=========================================\n")
    sb.append("Scheduled Date: ${exam.date} | Start Time: ${exam.startTime}\n")
    sb.append("Duration: ${exam.durationMinutes} Minutes\n")
    sb.append("Total Questions: 100 (60 Mathematics | 40 World General Knowledge)\n")
    sb.append("=========================================\n\n")

    sb.append("STUDENT SCORES LIST:\n")
    sb.append(String.format("%-20s %-8s %-8s %-6s %-12s %-6s %-5s\n", "Student Name", "Class", "Roll No", "Score", "Time Spent", "Correct", "Grade"))
    sb.append("---------------------------------------------------------------------------------\n")

    records.forEach { record ->
        val student = record.student
        val attempt = record.attempt
        if (attempt.isSubmitted) {
            val percentage = attempt.score
            val grade = when {
                percentage >= 90 -> "A+"
                percentage >= 80 -> "A"
                percentage >= 70 -> "B"
                percentage >= 60 -> "C"
                percentage >= 50 -> "D"
                else -> "F"
            }
            val timeSpentMin = ((attempt.endTimeMillis - attempt.startTimeMillis) / 60000).toInt()

            sb.append(
                String.format(
                    "%-20s %-8s %-8s %-6s %-12s %-6s %-5s\n",
                    student.name.take(19),
                    student.className.take(7),
                    (student.rollNumber ?: "N/A").take(7),
                    "${attempt.score}/100",
                    "$timeSpentMin mins",
                    "${attempt.correctAnswersCount} correct",
                    grade
                )
            )
        } else {
            sb.append(
                String.format(
                    "%-20s %-8s %-8s %-6s %-12s %-6s %-5s\n",
                    student.name.take(19),
                    student.className.take(7),
                    (student.rollNumber ?: "N/A").take(7),
                    "LIVE",
                    "Attempting",
                    "--",
                    "--"
                )
            )
        }
    }
    sb.append("\n=========================================\n")
    sb.append("Report generated securely by Online Exam System.")
    return sb.toString()
}
