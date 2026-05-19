package com.example.hlplf.lab.task4.data.entity.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.example.hlplf.lab.task4.data.entity.ClassSession
import com.example.hlplf.lab.task4.data.entity.Course

data class CourseWithSessions(
    @Embedded val course: Course,
    @Relation(
        parentColumn = "courseId",
        entityColumn = "course_id"
    )
    val sessions: List<ClassSession>
)
