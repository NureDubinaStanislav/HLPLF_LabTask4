package com.example.hlplf.lab.task4.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.hlplf.lab.task4.data.entity.ClassSession
import com.example.hlplf.lab.task4.data.entity.Course
import com.example.hlplf.lab.task4.data.entity.Teacher
import com.example.hlplf.lab.task4.data.repository.UniversityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CourseViewModel(private val repository: UniversityRepository) : ViewModel() {

    val allCourses: StateFlow<List<Course>> = repository.allCourses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val allTeachers: StateFlow<List<Teacher>> = repository.observeAllTeachers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val allSessions: StateFlow<List<ClassSession>> = repository.observeAllSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun addCourse(title: String, description: String, credits: Int, teacherId: Int) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            runCatching {
                repository.insertCourse(Course(title = title, description = description, credits = credits, teacherId = teacherId))
            }.onSuccess {
                _uiState.value = UiState.Success("Course added")
            }.onFailure {
                _uiState.value = UiState.Error(it.message ?: "Failed to add course")
            }
        }
    }

    fun addTeacher(firstName: String, lastName: String, email: String, department: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            runCatching {
                repository.insertTeacher(Teacher(firstName = firstName, lastName = lastName, email = email, department = department))
            }.onSuccess {
                _uiState.value = UiState.Success("Teacher added")
            }.onFailure {
                _uiState.value = UiState.Error(it.message ?: "Failed to add teacher")
            }
        }
    }

    fun deleteTeacher(teacher: Teacher) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            runCatching {
                repository.deleteTeacher(teacher)
            }.onSuccess {
                _uiState.value = UiState.Success("Teacher deleted")
            }.onFailure {
                _uiState.value = UiState.Error("Cannot delete: teacher has courses")
            }
        }
    }

    fun addSession(courseId: Int, topic: String, roomNumber: String, sessionType: String, dateMillis: Long) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            runCatching {
                repository.insertSession(
                    ClassSession(
                        courseId = courseId,
                        topic = topic,
                        roomNumber = roomNumber,
                        sessionType = sessionType,
                        sessionDate = dateMillis
                    )
                )
            }.onSuccess {
                _uiState.value = UiState.Success("Session added")
            }.onFailure {
                _uiState.value = UiState.Error(it.message ?: "Failed to add session")
            }
        }
    }

    fun deleteSession(session: ClassSession) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            runCatching {
                repository.deleteSession(session)
            }.onSuccess {
                _uiState.value = UiState.Success("Session deleted")
            }.onFailure {
                _uiState.value = UiState.Error(it.message ?: "Failed to delete session")
            }
        }
    }

    fun deleteCourse(course: Course) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            runCatching {
                repository.deleteCourse(course)
            }.onSuccess {
                _uiState.value = UiState.Success("Course deleted")
            }.onFailure {
                _uiState.value = UiState.Error(it.message ?: "Failed to delete course")
            }
        }
    }

    fun resetUiState() {
        _uiState.value = UiState.Idle
    }

    companion object {
        fun factory(repository: UniversityRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { CourseViewModel(repository) }
        }
    }
}
