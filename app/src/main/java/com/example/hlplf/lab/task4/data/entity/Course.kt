package com.example.hlplf.lab.task4.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "courses",
    foreignKeys = [
        ForeignKey(
            entity = Teacher::class,
            parentColumns = ["teacherId"],
            childColumns = ["teacher_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("teacher_id")]
)
data class Course(
    @PrimaryKey(autoGenerate = true) val courseId: Int = 0,
    val title: String,
    val description: String,
    val credits: Int,
    @ColumnInfo(name = "teacher_id") val teacherId: Int
)
