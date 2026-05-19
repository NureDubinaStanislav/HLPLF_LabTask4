package com.example.hlplf.lab.task4.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "teachers",
    indices = [Index(value = ["email"], unique = true)]
)
data class Teacher(
    @PrimaryKey(autoGenerate = true) val teacherId: Int = 0,
    val firstName: String,
    val lastName: String,
    val email: String,
    val department: String
)
