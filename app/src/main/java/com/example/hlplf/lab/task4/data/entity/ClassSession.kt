package com.example.hlplf.lab.task4.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "class_sessions",
    foreignKeys = [
        ForeignKey(
            entity = Course::class,
            parentColumns = ["courseId"],
            childColumns = ["course_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("course_id")]
)
data class ClassSession(
    @PrimaryKey(autoGenerate = true) val sessionId: Int = 0,
    @ColumnInfo(name = "course_id") val courseId: Int,
    val sessionDate: Long,
    val topic: String,
    val roomNumber: String,
    val sessionType: String
)
