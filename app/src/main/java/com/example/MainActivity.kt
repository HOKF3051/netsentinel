package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModelProvider
import com.example.ui.screens.MainScreen
import com.example.ui.viewmodel.NetworkViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    val app = application as NetSentinelApplication
    val viewModel = ViewModelProvider(
      this, 
      NetworkViewModel.provideFactory(this, app.container.repository)
    )[NetworkViewModel::class.java]

    setContent {
      MainScreen(viewModel = viewModel)
    }
  }
}

