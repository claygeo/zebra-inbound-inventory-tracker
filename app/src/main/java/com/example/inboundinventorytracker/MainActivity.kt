package com.example.inboundinventorytracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.inboundinventorytracker.ui.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.inboundinventorytracker.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                App()
            }
        }
    }
}

@Composable
fun App(appViewModel: AppViewModel = viewModel()) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(appViewModel, navController)
        }
        composable("main_menu") {
            MainMenuScreen(appViewModel, navController)
        }
        composable("prepped_batches") {
            PreppedBatchesScreen(appViewModel, navController)
        }
        composable("inbound_shipment") {
            InboundShipmentScreen(appViewModel, navController)
        }
        composable("audit_logs") {
            AuditLogScreen(appViewModel, navController)
        }
    }
}