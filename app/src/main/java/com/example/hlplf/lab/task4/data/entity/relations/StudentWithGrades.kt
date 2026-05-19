package com.example.hlplf.lab.task4.data.entity.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.example.hlplf.lab.task4.data.entity.Grade
import com.example.hlplf.lab.task4.data.entity.Student

data class StudentWithGrades(
    @Embedded val student: Student,
    @Relation(
        parentColumn = "studentId",
        entityColumn = "student_id"
    )
    val grades: List<Grade>
)
