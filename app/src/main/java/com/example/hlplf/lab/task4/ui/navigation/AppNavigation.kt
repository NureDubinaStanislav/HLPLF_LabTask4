package com.example.hlplf.lab.task4.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.hlplf.lab.task4.data.repository.UniversityRepository
import com.example.hlplf.lab.task4.ui.screens.CoursesScreen
import com.example.hlplf.lab.task4.ui.screens.GradesScreen
import com.example.hlplf.lab.task4.ui.screens.StudentsScreen
import com.example.hlplf.lab.task4.ui.viewmodel.CourseViewModel
import com.example.hlplf.lab.task4.ui.viewmodel.GradeViewModel
import com.example.hlplf.lab.task4.ui.viewmodel.StudentViewModel

sealed class Screen(val route: String) {
    object Students : Screen("students")
    object Courses : Screen("courses")
    object Grades : Screen("grades/{studentId}") {
        fun createRoute(id: Int) = "grades/$id"
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    repository: UniversityRepository,
    modifier: Modifier = Modifier
) {
    NavHost(navController = navController, startDestination = Screen.Students.route, modifier = modifier) {
        composable(Screen.Students.route) {
            val vm: StudentViewModel = viewModel(factory = StudentViewModel.factory(repository))
            StudentsScreen(
                viewModel = vm,
                onStudentClick = { student ->
                    navController.navigate(Screen.Grades.createRoute(student.studentId))
                }
            )
        }
        composable(Screen.Courses.route) {
            val vm: CourseViewModel = viewModel(factory = CourseViewModel.factory(repository))
            CoursesScreen(viewModel = vm)
        }
        composable(
            route = Screen.Grades.route,
            arguments = listOf(navArgument("studentId") { type = NavType.IntType })
        ) { backStackEntry ->
            val studentId = backStackEntry.arguments?.getInt("studentId") ?: return@composable
            val vm: GradeViewModel = viewModel(factory = GradeViewModel.factory(repository, studentId))
            GradesScreen(viewModel = vm)
        }
    }
}
