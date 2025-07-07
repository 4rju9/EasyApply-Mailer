package app.netlify.dev4rju9.easyapplymailer

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.netlify.dev4rju9.easyapplymailer.others.navigation.Screen
import app.netlify.dev4rju9.easyapplymailer.ui.screens.addemail.AddEmailScreen
import app.netlify.dev4rju9.easyapplymailer.ui.screens.addemail.AddEmailViewModel
import app.netlify.dev4rju9.easyapplymailer.ui.screens.home.HomeScreen
import app.netlify.dev4rju9.easyapplymailer.ui.screens.setup.SetupScreen
import app.netlify.dev4rju9.easyapplymailer.ui.theme.EasyApplyMailerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EasyApplyMailerTheme {

                val sharedPreferences = getSharedPreferences("app", Context.MODE_PRIVATE)
                val startDestination = if (sharedPreferences.getBoolean("setup", true)) Screen.Setup.route
                else Screen.Home.route
                MyAppNavHost(
                    startDestination = startDestination,
                    onSetupDone = {
                        sharedPreferences.edit().putBoolean("setup", false).apply()
                    }
                )

            }
        }
    }
}

@Composable
fun MyAppNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Setup.route,
    onSetupDone: () -> Unit
) {

    val AddEmailViewModel = hiltViewModel<AddEmailViewModel>()

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Setup.route) {
            SetupScreen(
                onSetupDone = {
                    onSetupDone()
                    while (navController.previousBackStackEntry != null) navController.popBackStack()
                    navController.navigate(Screen.Home.route)
                }
            )
        }
        composable(Screen.Add.route) {
            AddEmailScreen(
                viewModel = AddEmailViewModel,
                onSaveDone = {
                    AddEmailViewModel.loadEmail(null)
                    navController.let {
                        if (it.previousBackStackEntry != null) it.popBackStack()
                    }
                }
            )
        }
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToSetup = {
                    navController.navigate(Screen.Setup.route)
                },
                onNavigateToAddEmail = {
                    AddEmailViewModel.loadEmail(it)
                    navController.navigate(Screen.Add.route)
                }
            )
        }
    }
}