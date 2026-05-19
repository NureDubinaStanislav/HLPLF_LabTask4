package com.example.hlplf.lab.task4.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.hlplf.lab.task4.data.entity.Student
import com.example.hlplf.lab.task4.data.entity.relations.StudentWithGrades
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Query("SELECT * FROM students ORDER BY lastName, firstName")
    fun observeAll(): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE firstName LIKE '%' || :query || '%' OR lastName LIKE '%' || :query || '%' OR email LIKE '%' || :query || '%' ORDER BY lastName, firstName")
    fun search(query: String): Flow<List<Student>>

    @Insert
    suspend fun insert(student: Student): Long

    @Update
    suspend fun update(student: Student)

    @Delete
    suspend fun delete(student: Student)

    @Transaction
    @Query("SELECT * FROM students WHERE studentId = :id")
    fun observeWithGrades(id: Int): Flow<StudentWithGrades?>
}
