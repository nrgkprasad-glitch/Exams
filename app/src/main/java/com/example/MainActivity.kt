package com.example

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.data.AppDatabase
import com.example.data.ExamRepository
import com.example.ui.ExamSystemApp
import com.example.ui.ExamViewModel
import com.example.ui.ExamViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize SQLite Room database, shared preferences, and the core repository
        val database = AppDatabase.getDatabase(this)
        val repository = ExamRepository(database)
        val sharedPreferences = getSharedPreferences("exam_system_prefs", Context.MODE_PRIVATE)

        // Instantiate our shared ViewModel
        val viewModelFactory = ExamViewModelFactory(repository, sharedPreferences)
        val viewModel = ViewModelProvider(this, viewModelFactory)[ExamViewModel::class.java]

        setContent {
            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                ExamSystemApp(
                    viewModel = viewModel,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}
