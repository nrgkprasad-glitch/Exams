package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// Relation classes for complex queries
data class AttemptWithStudent(
    @Embedded val attempt: ExamAttempt,
    @Relation(
        parentColumn = "studentId",
        entityColumn = "id"
    )
    val student: Student
)

data class StudentWithAttempts(
    @Embedded val student: Student,
    @Relation(
        parentColumn = "id",
        entityColumn = "studentId"
    )
    val attempts: List<ExamAttempt>
)

@Dao
interface StudentDao {
    @Query("SELECT * FROM students ORDER BY name ASC")
    fun getAllStudentsFlow(): Flow<List<Student>>

    @Query("SELECT * FROM students ORDER BY name ASC")
    suspend fun getAllStudents(): List<Student>

    @Query("SELECT * FROM students WHERE id = :id")
    suspend fun getStudentById(id: Int): Student?

    @Query("SELECT * FROM students WHERE username = :username LIMIT 1")
    suspend fun getStudentByUsername(username: String): Student?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertStudent(student: Student): Long

    @Update
    suspend fun updateStudent(student: Student)

    @Delete
    suspend fun deleteStudent(student: Student)

    @Query("DELETE FROM students WHERE id = :id")
    suspend fun deleteStudentById(id: Int)
}

@Dao
interface ExamDao {
    @Query("SELECT * FROM exams ORDER BY date DESC, startTime DESC")
    fun getAllExamsFlow(): Flow<List<Exam>>

    @Query("SELECT * FROM exams ORDER BY date DESC, startTime DESC")
    suspend fun getAllExams(): List<Exam>

    @Query("SELECT * FROM exams WHERE id = :id")
    suspend fun getExamById(id: Int): Exam?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExam(exam: Exam): Long

    @Update
    suspend fun updateExam(exam: Exam)

    @Delete
    suspend fun deleteExam(exam: Exam)

    @Query("DELETE FROM exams WHERE id = :id")
    suspend fun deleteExamById(id: Int)
}

@Dao
interface QuestionDao {
    @Query("SELECT * FROM exam_questions WHERE examId = :examId ORDER BY questionIndex ASC")
    suspend fun getQuestionsForExam(examId: Int): List<ExamQuestion>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<ExamQuestion>)

    @Query("DELETE FROM exam_questions WHERE examId = :examId")
    suspend fun deleteQuestionsForExam(examId: Int)
}

@Dao
interface AttemptDao {
    @Query("SELECT * FROM exam_attempts WHERE id = :id LIMIT 1")
    suspend fun getAttemptById(id: Int): ExamAttempt?

    @Query("SELECT * FROM exam_attempts WHERE studentId = :studentId AND examId = :examId LIMIT 1")
    suspend fun getAttempt(studentId: Int, examId: Int): ExamAttempt?

    @Query("SELECT * FROM exam_attempts WHERE studentId = :studentId AND examId = :examId LIMIT 1")
    fun getAttemptFlow(studentId: Int, examId: Int): Flow<ExamAttempt?>

    @Transaction
    @Query("SELECT * FROM exam_attempts WHERE examId = :examId")
    fun getAttemptsWithStudentForExamFlow(examId: Int): Flow<List<AttemptWithStudent>>

    @Transaction
    @Query("SELECT * FROM exam_attempts WHERE examId = :examId")
    suspend fun getAttemptsWithStudentForExam(examId: Int): List<AttemptWithStudent>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttempt(attempt: ExamAttempt): Long

    @Update
    suspend fun updateAttempt(attempt: ExamAttempt)

    @Query("SELECT * FROM student_answers WHERE attemptId = :attemptId ORDER BY questionIndex ASC")
    suspend fun getAnswersForAttempt(attemptId: Int): List<StudentAnswer>

    @Query("SELECT * FROM student_answers WHERE attemptId = :attemptId ORDER BY questionIndex ASC")
    fun getAnswersForAttemptFlow(attemptId: Int): Flow<List<StudentAnswer>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnswer(answer: StudentAnswer)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnswers(answers: List<StudentAnswer>)
}
