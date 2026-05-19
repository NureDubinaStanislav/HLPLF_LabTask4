package com.example.hlplf.lab.task4.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.hlplf.lab.task4.data.entity.ClassSession
import kotlinx.coroutines.flow.Flow

@Dao
interface ClassSessionDao {
    @Query("SELECT * FROM class_sessions ORDER BY sessionDate")
    fun observeAll(): Flow<List<ClassSession>>

    @Query("SELECT * FROM class_sessions WHERE course_id = :courseId ORDER BY sessionDate")
    fun observeByCourse(courseId: Int): Flow<List<ClassSession>>

    @Query("SELECT * FROM class_sessions WHERE sessionId = :id")
    suspend fun getById(id: Int): ClassSession?

    @Insert
    suspend fun insert(session: ClassSession): Long

    @Update
    suspend fun update(session: ClassSession)

    @Delete
    suspend fun delete(session: ClassSession)
}
