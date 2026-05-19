package com.example.hlplf.lab.task4.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.hlplf.lab.task4.data.entity.Student
import com.example.hlplf.lab.task4.data.repository.UniversityRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class StudentViewModel(private val repository: UniversityRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val students = _searchQuery
        .debounce(300L)
        .flatMapLatest { query ->
            if (query.isBlank()) repository.observeAllStudents()
            else repository.searchStudents(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun addStudent(firstName: String, lastName: String, email: String, enrollmentYear: Int) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            runCatching {
                repository.insertStudent(Student(firstName = firstName, lastName = lastName, email = email, enrollmentYear = enrollmentYear))
            }.onSuccess {
                _uiState.value = UiState.Success("Student added")
            }.onFailure {
                _uiState.value = UiState.Error(it.message ?: "Failed to add student")
            }
        }
    }

    fun deleteStudent(student: Student) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            runCatching {
                repository.deleteStudentWithGrades(student)
            }.onSuccess {
                _uiState.value = UiState.Success("Student deleted")
            }.onFailure {
                _uiState.value = UiState.Error(it.message ?: "Failed to delete student")
            }
        }
    }

    fun resetUiState() {
        _uiState.value = UiState.Idle
    }

    companion object {
        fun factory(repository: UniversityRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { StudentViewModel(repository) }
        }
    }
}
