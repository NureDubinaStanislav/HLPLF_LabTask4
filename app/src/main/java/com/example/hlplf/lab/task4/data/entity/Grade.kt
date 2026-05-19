package com.example.hlplf.lab.task4.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "grades",
    foreignKeys = [
        ForeignKey(
            entity = Student::class,
            parentColumns = ["studentId"],
            childColumns = ["student_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ClassSession::class,
            parentColumns = ["sessionId"],
            childColumns = ["session_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("student_id"),
        Index("session_id"),
        Index(value = ["student_id", "session_id"], unique = true)
    ]
)
data class Grade(
    @PrimaryKey(autoGenerate = true) val gradeId: Int = 0,
    @ColumnInfo(name = "student_id") val studentId: Int,
    @ColumnInfo(name = "session_id") val sessionId: Int,
    val score: Float,
    val gradedAt: Long = System.currentTimeMillis()
)
