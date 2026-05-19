package com.example.hlplf.lab.task4.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "students",
    indices = [Index(value = ["email"], unique = true)]
)
data class Student(
    @PrimaryKey(autoGenerate = true) val studentId: Int = 0,
    val firstName: String,
    val lastName: String,
    val email: String,
    val enrollmentYear: Int
)
