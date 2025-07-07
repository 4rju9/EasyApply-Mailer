package app.netlify.dev4rju9.easyapplymailer.others.navigation

sealed class Screen(val route: String) {
    object Setup : Screen("setup_screen")
    object Add : Screen("add_email_screen")
    object Home : Screen("home_screen")
}