package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.data.repository.ImageUploadRepository
import com.example.data.repository.ItemRepository
import com.example.ui.screens.AddItemScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.RinRinTheme
import com.example.ui.viewmodels.HomeViewModel
import com.example.ui.viewmodels.LoginViewModel
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {

    private val itemRepository = ItemRepository()
    private val imageUploadRepository = ImageUploadRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RinRinTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = "splash"
                    ) {
                        composable("splash") {
                            SplashScreen(
                                onNavigateToHome = {
                                    navController.navigate("home") {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                },
                                onNavigateToLogin = {
                                    navController.navigate("login") {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable("login") {
                            val loginViewModel: LoginViewModel = viewModel {
                                LoginViewModel(FirebaseAuth.getInstance())
                            }
                            LoginScreen(
                                viewModel = loginViewModel,
                                onLoginSuccess = {
                                    navController.navigate("home") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable("home") {
                            val homeViewModel: HomeViewModel = viewModel {
                                HomeViewModel(itemRepository)
                            }
                            HomeScreen(
                                viewModel = homeViewModel,
                                onNavigateToAddItem = {
                                    navController.navigate("addItem")
                                },
                                onLogout = {
                                    FirebaseAuth.getInstance().signOut()
                                    navController.navigate("login") {
                                        popUpTo("home") { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable("addItem") {
                            AddItemScreen(
                                itemRepository = itemRepository,
                                imageUploadRepository = imageUploadRepository,
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
