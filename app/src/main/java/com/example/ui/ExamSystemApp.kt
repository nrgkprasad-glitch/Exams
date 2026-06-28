package com.example.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.theme.MyApplicationTheme

@Composable
fun ExamSystemApp(
    viewModel: ExamViewModel,
    modifier: Modifier = Modifier
) {
    val currentScreen by viewModel.currentScreen.collectAsState()

    MyApplicationTheme {
        Surface(
            modifier = modifier.fillMaxSize()
        ) {
            // Smoothly switch screens using Compose Crossfade transition!
            Crossfade(
                targetState = currentScreen,
                label = "ScreenTransition"
            ) { screen ->
                when (screen) {
                    is Screen.Login -> {
                        LoginScreen(viewModel = viewModel)
                    }
                    is Screen.AdminDashboard -> {
                        AdminDashboard(viewModel = viewModel)
                    }
                    is Screen.StudentDashboard -> {
                        StudentDashboard(viewModel = viewModel)
                    }
                    is Screen.ExamTaking -> {
                        ExamActiveScreen(viewModel = viewModel)
                    }
                    is Screen.ExamResults -> {
                        ExamResultsScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}
