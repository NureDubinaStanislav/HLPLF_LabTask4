package com.example.hlplf.lab.task4.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.hlplf.lab.task4.data.entity.Course
import com.example.hlplf.lab.task4.data.entity.relations.CourseWithSessions
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {
    @Query("SELECT * FROM courses ORDER BY title")
    fun observeAll(): Flow<List<Course>>

    @Query("SELECT * FROM courses WHERE courseId = :id")
    suspend fun getById(id: Int): Course?

    @Query("SELECT * FROM courses WHERE teacher_id = :teacherId ORDER BY title")
    fun observeByTeacher(teacherId: Int): Flow<List<Course>>

    @Insert
    suspend fun insert(course: Course): Long

    @Update
    suspend fun update(course: Course)

    @Delete
    suspend fun delete(course: Course)

    @Transaction
    @Query("SELECT * FROM courses ORDER BY title")
    fun observeWithSessions(): Flow<List<CourseWithSessions>>
}
