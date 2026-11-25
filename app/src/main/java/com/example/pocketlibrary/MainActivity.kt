package com.example.pocketlibrary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.pocketlibrary.ui.theme.PocketLibraryTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PocketLibraryTheme {
                PocketLibraryApp()
            }
        }
    }
}

@Composable
fun PocketLibraryApp() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            NavigationBar {
                val currentBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = currentBackStackEntry?.destination?.route

                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Search, "Search") },
                    label = { Text("Search") },
                    selected = currentRoute == "search",
                    onClick = { navController.navigate("search") }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Star, "My Library") },
                    label = { Text("My Library") },
                    selected = currentRoute == "library",
                    onClick = { navController.navigate("library") }
                )
            }
        }
    ) { innerPadding ->
        NavHost(navController = navController, startDestination = "search", Modifier.padding(innerPadding)) {
            composable("search") {
                SearchScreen(viewModel(factory = SearchViewModel.Factory))
            }
            composable("library") { MyLibraryScreen() }
        }
    }
}