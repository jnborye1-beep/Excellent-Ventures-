package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AppDatabase
import com.example.data.PageantRepository
import com.example.ui.PageantMainScreen
import com.example.ui.PageantViewModel
import com.example.ui.PageantViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    
    // Initialize Database and Repository
    val database = AppDatabase.getDatabase(this)
    val repository = PageantRepository(database.pageantDao())

    setContent {
      MyApplicationTheme {
        // Build the ViewModel using our custom Factory
        val factory = PageantViewModelFactory(repository)
        val viewModel: PageantViewModel = viewModel(factory = factory)

        Surface(
          modifier = Modifier.fillMaxSize(),
          color = androidx.compose.ui.graphics.Color.Transparent
        ) {
          PageantMainScreen(viewModel = viewModel)
        }
      }
    }
  }
}
