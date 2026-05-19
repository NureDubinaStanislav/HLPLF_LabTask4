package com.example.hlplf.lab.task4.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.hlplf.lab.task4.data.entity.ClassSession
import com.example.hlplf.lab.task4.data.entity.Grade
import com.example.hlplf.lab.task4.ui.viewmodel.GradeViewModel
import com.example.hlplf.lab.task4.ui.viewmodel.UiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradesScreen(viewModel: GradeViewModel) {
    val grades by viewModel.grades.collectAsState()
    val sessions by viewModel.allSessions.collectAsState()
    val average by viewModel.averageGrade.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var showAddDialog by remember { mutableStateOf(false) }

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
        topBar = { TopAppBar(title = { Text("Grades") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Grade")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Average Grade", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = average?.let { it.toInt().toString() } ?: "—",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            LazyColumn {
                items(grades, key = { it.gradeId }) { grade ->
                    val sessionTopic = sessions.find { it.sessionId == grade.sessionId }?.topic
                        ?: "Session #${grade.sessionId}"
                    SwipeToDismissGradeItem(
                        grade = grade,
                        sessionTopic = sessionTopic,
                        onDismiss = { viewModel.deleteGrade(grade) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        val sessions by viewModel.allSessions.collectAsState()
        AddGradeDialog(
            sessions = sessions,
            onDismiss = { showAddDialog = false },
            onConfirm = { sessionId, score ->
                viewModel.submitGrade(sessionId, score)
                showAddDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDismissGradeItem(grade: Grade, sessionTopic: String, onDismiss: () -> Unit) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDismiss()
                true
            } else false
        }
    )
    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()) }

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {},
        content = {
            ListItem(
                headlineContent = { Text("Score: ${grade.score.toInt()}") },
                supportingContent = {
                    Text("$sessionTopic · ${dateFormat.format(Date(grade.gradedAt))}")
                }
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddGradeDialog(
    sessions: List<ClassSession>,
    onDismiss: () -> Unit,
    onConfirm: (Int, Float) -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()) }
    var selectedSession by remember { mutableStateOf<ClassSession?>(null) }
    var expanded by remember { mutableStateOf(false) }
    var score by remember { mutableFloatStateOf(50f) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Grade") },
        text = {
            Column {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedSession?.let {
                            "${it.topic} (${dateFormat.format(Date(it.sessionDate))})"
                        } ?: "Select Session",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Session") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        if (sessions.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("No sessions available") },
                                onClick = { expanded = false }
                            )
                        } else {
                            sessions.forEach { session ->
                                DropdownMenuItem(
                                    text = { Text("${session.topic} · ${dateFormat.format(Date(session.sessionDate))}") },
                                    onClick = {
                                        selectedSession = session
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                Text(
                    text = "Score: ${score.toInt()}",
                    modifier = Modifier.padding(top = 16.dp)
                )
                Slider(
                    value = score,
                    onValueChange = { score = it.toInt().toFloat() },
                    valueRange = 0f..100f,
                    steps = 99,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val session = selectedSession ?: return@TextButton
                    onConfirm(session.sessionId, score)
                }
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
