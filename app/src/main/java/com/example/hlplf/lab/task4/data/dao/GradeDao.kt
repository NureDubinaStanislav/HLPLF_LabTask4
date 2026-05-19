package com.example.hlplf.lab.task4.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.hlplf.lab.task4.data.entity.Grade
import kotlinx.coroutines.flow.Flow

@Dao
interface GradeDao {
    @Query("SELECT * FROM grades WHERE student_id = :studentId ORDER BY gradedAt DESC")
    fun observeByStudent(studentId: Int): Flow<List<Grade>>

    @Query("SELECT * FROM grades WHERE session_id = :sessionId ORDER BY gradedAt DESC")
    fun observeBySession(sessionId: Int): Flow<List<Grade>>

    @Query("SELECT AVG(score) FROM grades WHERE student_id = :studentId")
    fun observeAverageForStudent(studentId: Int): Flow<Float?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(grade: Grade): Long

    @Update
    suspend fun update(grade: Grade)

    @Delete
    suspend fun delete(grade: Grade)

    @Query("UPDATE grades SET score = :score, gradedAt = :gradedAt WHERE gradeId = :gradeId")
    suspend fun updateScore(gradeId: Int, score: Float, gradedAt: Long = System.currentTimeMillis())
}
