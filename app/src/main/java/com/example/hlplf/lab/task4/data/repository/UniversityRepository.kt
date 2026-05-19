package com.example.hlplf.lab.task4.data.repository

import androidx.collection.LruCache
import androidx.room.withTransaction
import com.example.hlplf.lab.task4.data.database.UniversityDatabase
import com.example.hlplf.lab.task4.data.entity.ClassSession
import com.example.hlplf.lab.task4.data.entity.Course
import com.example.hlplf.lab.task4.data.entity.Grade
import com.example.hlplf.lab.task4.data.entity.Student
import com.example.hlplf.lab.task4.data.entity.Teacher
import com.example.hlplf.lab.task4.data.entity.relations.CourseWithSessions
import com.example.hlplf.lab.task4.data.entity.relations.StudentWithGrades
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach

class UniversityRepository(private val db: UniversityDatabase) {

    private val courseCache = LruCache<Int, Course>(50)
    private val gradeCache = LruCache<Int, Grade>(100)

    // --- Students ---

    fun observeAllStudents(): Flow<List<Student>> = db.studentDao().observeAll()

    fun searchStudents(query: String): Flow<List<Student>> = db.studentDao().search(query)

    fun observeStudentWithGrades(studentId: Int): Flow<StudentWithGrades?> =
        db.studentDao().observeWithGrades(studentId)

    suspend fun insertStudent(student: Student): Long = db.studentDao().insert(student)

    suspend fun updateStudent(student: Student) = db.studentDao().update(student)

    suspend fun deleteStudentWithGrades(student: Student) {
        db.withTransaction {
            // Invalidate grade cache for this student's grades
            val studentGradeIds = mutableListOf<Int>()
            gradeCache.snapshot().forEach { (id, grade) ->
                if (grade.studentId == student.studentId) studentGradeIds.add(id)
            }
            studentGradeIds.forEach { gradeCache.remove(it) }
            db.studentDao().delete(student)
        }
    }

    // --- Teachers ---

    fun observeAllTeachers(): Flow<List<Teacher>> = db.teacherDao().observeAll()

    suspend fun insertTeacher(teacher: Teacher): Long = db.teacherDao().insert(teacher)

    suspend fun updateTeacher(teacher: Teacher) = db.teacherDao().update(teacher)

    suspend fun deleteTeacher(teacher: Teacher) = db.teacherDao().delete(teacher)

    // --- Courses ---

    val allCourses: Flow<List<Course>> = db.courseDao().observeAll()
        .onEach { list -> list.forEach { courseCache.put(it.courseId, it) } }

    fun observeCoursesByTeacher(teacherId: Int): Flow<List<Course>> =
        db.courseDao().observeByTeacher(teacherId)

    fun observeCoursesWithSessions(): Flow<List<CourseWithSessions>> =
        db.courseDao().observeWithSessions()

    suspend fun getCourseById(id: Int): Course? =
        courseCache.get(id) ?: db.courseDao().getById(id)?.also { courseCache.put(id, it) }

    suspend fun insertCourse(course: Course): Long = db.courseDao().insert(course)

    suspend fun updateCourse(course: Course) {
        db.withTransaction {
            db.courseDao().update(course)
            courseCache.put(course.courseId, course)
        }
    }

    suspend fun deleteCourse(course: Course) {
        db.withTransaction {
            db.courseDao().delete(course)
            courseCache.remove(course.courseId)
        }
    }

    // --- Class Sessions ---

    fun observeAllSessions(): Flow<List<ClassSession>> = db.classSessionDao().observeAll()

    fun observeSessionsByCourse(courseId: Int): Flow<List<ClassSession>> =
        db.classSessionDao().observeByCourse(courseId)

    suspend fun getSessionById(id: Int): ClassSession? = db.classSessionDao().getById(id)

    suspend fun insertSession(session: ClassSession): Long = db.classSessionDao().insert(session)

    suspend fun updateSession(session: ClassSession) = db.classSessionDao().update(session)

    suspend fun deleteSession(session: ClassSession) = db.classSessionDao().delete(session)

    // --- Grades ---

    fun observeGradesByStudent(studentId: Int): Flow<List<Grade>> =
        db.gradeDao().observeByStudent(studentId)
            .onEach { list -> list.forEach { gradeCache.put(it.gradeId, it) } }

    fun observeGradesBySession(sessionId: Int): Flow<List<Grade>> =
        db.gradeDao().observeBySession(sessionId)

    fun observeAverageGradeForStudent(studentId: Int): Flow<Float?> =
        db.gradeDao().observeAverageForStudent(studentId)

    suspend fun upsertGrade(grade: Grade): Long = db.gradeDao().upsert(grade)

    suspend fun updateGradesBatch(grades: List<Grade>) {
        db.withTransaction {
            grades.forEach { grade ->
                db.gradeDao().update(grade)
                gradeCache.put(grade.gradeId, grade)
            }
        }
    }

    suspend fun deleteGrade(grade: Grade) {
        db.withTransaction {
            db.gradeDao().delete(grade)
            gradeCache.remove(grade.gradeId)
        }
    }
}
