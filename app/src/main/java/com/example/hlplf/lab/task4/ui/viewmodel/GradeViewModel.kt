package com.example.hlplf.lab.task4.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.hlplf.lab.task4.data.entity.ClassSession
import com.example.hlplf.lab.task4.data.entity.Grade
import com.example.hlplf.lab.task4.data.repository.UniversityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GradeViewModel(
    private val repository: UniversityRepository,
    private val studentId: Int
) : ViewModel() {

    val grades: StateFlow<List<Grade>> = repository.observeGradesByStudent(studentId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val averageGrade: StateFlow<Float?> = repository.observeAverageGradeForStudent(studentId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val allSessions: StateFlow<List<ClassSession>> = repository.observeAllSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun submitGrade(sessionId: Int, score: Float) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            runCatching {
                repository.upsertGrade(
                    Grade(
                        studentId = studentId,
                        sessionId = sessionId,
                        score = score,
                        gradedAt = System.currentTimeMillis()
                    )
                )
            }.onSuccess {
                _uiState.value = UiState.Success("Grade saved")
            }.onFailure {
                _uiState.value = UiState.Error(it.message ?: "Failed to save grade")
            }
        }
    }

    fun deleteGrade(grade: Grade) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            runCatching {
                repository.deleteGrade(grade)
            }.onSuccess {
                _uiState.value = UiState.Success("Grade deleted")
            }.onFailure {
                _uiState.value = UiState.Error(it.message ?: "Failed to delete grade")
            }
        }
    }

    fun resetUiState() {
        _uiState.value = UiState.Idle
    }

    companion object {
        fun factory(repository: UniversityRepository, studentId: Int): ViewModelProvider.Factory =
            viewModelFactory {
                initializer { GradeViewModel(repository, studentId) }
            }
    }
}
