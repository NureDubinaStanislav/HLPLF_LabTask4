package com.example.hlplf.lab.task4.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.hlplf.lab.task4.data.entity.ClassSession
import com.example.hlplf.lab.task4.data.entity.Course
import com.example.hlplf.lab.task4.data.entity.Teacher
import com.example.hlplf.lab.task4.ui.viewmodel.CourseViewModel
import com.example.hlplf.lab.task4.ui.viewmodel.UiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoursesScreen(viewModel: CourseViewModel) {
    val courses by viewModel.allCourses.collectAsState()
    val teachers by viewModel.allTeachers.collectAsState()
    val sessions by viewModel.allSessions.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var showAddCourseDialog by remember { mutableStateOf(false) }
    var showAddTeacherDialog by remember { mutableStateOf(false) }
    var showAddSessionDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is UiState.Success -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetUiState()
            }
            is UiState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetUiState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Courses") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                when (selectedTab) {
                    0 -> showAddCourseDialog = true
                    1 -> showAddTeacherDialog = true
                    2 -> showAddSessionDialog = true
                }
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Courses") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Teachers") })
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Sessions") })
            }
            when (selectedTab) {
                0 -> LazyColumn {
                    items(courses, key = { it.courseId }) { course ->
                        val teacher = teachers.find { it.teacherId == course.teacherId }
                        val teacherName = teacher?.let { "${it.firstName} ${it.lastName}" } ?: "Teacher #${course.teacherId}"
                        ListItem(
                            headlineContent = { Text(course.title) },
                            supportingContent = { Text("${course.credits} credits · $teacherName") },
                            trailingContent = {
                                IconButton(onClick = { viewModel.deleteCourse(course) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                                }
                            }
                        )
                    }
                }
                1 -> LazyColumn {
                    items(teachers, key = { it.teacherId }) { teacher ->
                        ListItem(
                            headlineContent = { Text("${teacher.firstName} ${teacher.lastName}") },
                            supportingContent = { Text(teacher.department) },
                            trailingContent = {
                                IconButton(onClick = { viewModel.deleteTeacher(teacher) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                                }
                            }
                        )
                    }
                }
                2 -> SessionsTab(sessions = sessions, courses = courses, onDelete = { viewModel.deleteSession(it) })
            }
        }
    }

    if (showAddCourseDialog) {
        AddCourseDialog(
            teachers = teachers,
            onDismiss = { showAddCourseDialog = false },
            onConfirm = { title, description, credits, teacherId ->
                viewModel.addCourse(title, description, credits, teacherId)
                showAddCourseDialog = false
            }
        )
    }

    if (showAddTeacherDialog) {
        AddTeacherDialog(
            onDismiss = { showAddTeacherDialog = false },
            onConfirm = { firstName, lastName, email, department ->
                viewModel.addTeacher(firstName, lastName, email, department)
                showAddTeacherDialog = false
            }
        )
    }

    if (showAddSessionDialog) {
        AddSessionDialog(
            courses = courses,
            onDismiss = { showAddSessionDialog = false },
            onConfirm = { courseId, topic, roomNumber, sessionType, dateMillis ->
                viewModel.addSession(courseId, topic, roomNumber, sessionType, dateMillis)
                showAddSessionDialog = false
            }
        )
    }
}

@Composable
private fun SessionsTab(
    sessions: List<ClassSession>,
    courses: List<Course>,
    onDelete: (ClassSession) -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()) }
    LazyColumn {
        items(sessions, key = { it.sessionId }) { session ->
            val courseName = courses.find { it.courseId == session.courseId }?.title ?: "Course #${session.courseId}"
            ListItem(
                headlineContent = { Text(session.topic) },
                supportingContent = { Text("$courseName · ${session.roomNumber} · ${dateFormat.format(Date(session.sessionDate))}") },
                trailingContent = {
                    IconButton(onClick = { onDelete(session) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddCourseDialog(
    teachers: List<Teacher>,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Int, Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var creditsText by remember { mutableStateOf("") }
    var selectedTeacher by remember { mutableStateOf<Teacher?>(null) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Course") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = creditsText,
                    onValueChange = { creditsText = it },
                    label = { Text("Credits") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    OutlinedTextField(
                        value = selectedTeacher?.let { "${it.firstName} ${it.lastName}" } ?: "Select Teacher",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        teachers.forEach { teacher ->
                            DropdownMenuItem(
                                text = { Text("${teacher.firstName} ${teacher.lastName}") },
                                onClick = {
                                    selectedTeacher = teacher
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val credits = creditsText.toIntOrNull() ?: return@TextButton
                    val teacher = selectedTeacher ?: return@TextButton
                    if (title.isNotBlank()) {
                        onConfirm(title, description, credits, teacher.teacherId)
                    }
                }
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun AddTeacherDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String) -> Unit
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Teacher") },
        text = {
            Column {
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("First Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Last Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = department,
                    onValueChange = { department = it },
                    label = { Text("Department") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (firstName.isNotBlank() && lastName.isNotBlank() && email.isNotBlank() && department.isNotBlank()) {
                        onConfirm(firstName, lastName, email, department)
                    }
                }
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddSessionDialog(
    courses: List<Course>,
    onDismiss: () -> Unit,
    onConfirm: (Int, String, String, String, Long) -> Unit
) {
    var selectedCourse by remember { mutableStateOf<Course?>(null) }
    var courseExpanded by remember { mutableStateOf(false) }
    var topic by remember { mutableStateOf("") }
    var roomNumber by remember { mutableStateOf("") }
    var sessionType by remember { mutableStateOf("") }
    var sessionTypeExpanded by remember { mutableStateOf(false) }
    var dateText by remember { mutableStateOf("") }
    val sessionTypes = listOf("Lecture", "Lab", "Seminar", "Exam")
    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Session") },
        text = {
            Column {
                ExposedDropdownMenuBox(
                    expanded = courseExpanded,
                    onExpandedChange = { courseExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedCourse?.title ?: "Select Course",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Course") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = courseExpanded) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = courseExpanded, onDismissRequest = { courseExpanded = false }) {
                        courses.forEach { course ->
                            DropdownMenuItem(
                                text = { Text(course.title) },
                                onClick = { selectedCourse = course; courseExpanded = false }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = topic,
                    onValueChange = { topic = it },
                    label = { Text("Topic") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = roomNumber,
                    onValueChange = { roomNumber = it },
                    label = { Text("Room") },
                    modifier = Modifier.fillMaxWidth()
                )
                ExposedDropdownMenuBox(
                    expanded = sessionTypeExpanded,
                    onExpandedChange = { sessionTypeExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = sessionType.ifBlank { "Select Type" },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sessionTypeExpanded) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = sessionTypeExpanded, onDismissRequest = { sessionTypeExpanded = false }) {
                        sessionTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = { sessionType = type; sessionTypeExpanded = false }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = dateText,
                    onValueChange = { dateText = it },
                    label = { Text("Date (dd.MM.yyyy)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val course = selectedCourse ?: return@TextButton
                    if (topic.isBlank() || roomNumber.isBlank() || sessionType.isBlank()) return@TextButton
                    val dateMillis = runCatching { dateFormat.parse(dateText)?.time }.getOrNull()
                        ?: return@TextButton
                    onConfirm(course.courseId, topic, roomNumber, sessionType, dateMillis)
                }
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
