package com.example.hlplf.lab.task4.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.hlplf.lab.task4.data.dao.ClassSessionDao
import com.example.hlplf.lab.task4.data.dao.CourseDao
import com.example.hlplf.lab.task4.data.dao.GradeDao
import com.example.hlplf.lab.task4.data.dao.StudentDao
import com.example.hlplf.lab.task4.data.dao.TeacherDao
import com.example.hlplf.lab.task4.data.entity.ClassSession
import com.example.hlplf.lab.task4.data.entity.Course
import com.example.hlplf.lab.task4.data.entity.Grade
import com.example.hlplf.lab.task4.data.entity.Student
import com.example.hlplf.lab.task4.data.entity.Teacher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Student::class, Teacher::class, Course::class, ClassSession::class, Grade::class],
    version = 1,
    exportSchema = true
)
abstract class UniversityDatabase : RoomDatabase() {
    abstract fun studentDao(): StudentDao
    abstract fun teacherDao(): TeacherDao
    abstract fun courseDao(): CourseDao
    abstract fun classSessionDao(): ClassSessionDao
    abstract fun gradeDao(): GradeDao

    companion object {
        @Volatile
        private var INSTANCE: UniversityDatabase? = null

        fun getInstance(context: Context): UniversityDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    UniversityDatabase::class.java,
                    "university.db"
                ).addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            INSTANCE?.let { database ->
                                val teacherId = database.teacherDao().insert(
                                    Teacher(firstName = "Іван", lastName = "Петренко", email = "petenko@uni.ua", department = "Комп'ютерні науки")
                                ).toInt()
                                val courseId = database.courseDao().insert(
                                    Course(title = "Програмування", description = "Основи програмування", credits = 4, teacherId = teacherId)
                                ).toInt()
                                database.classSessionDao().insert(
                                    ClassSession(courseId = courseId, sessionDate = System.currentTimeMillis(), topic = "Вступне заняття", roomNumber = "101", sessionType = "Лекція")
                                )
                            }
                        }
                    }
                }).build().also { INSTANCE = it }
            }
        }
    }
}
