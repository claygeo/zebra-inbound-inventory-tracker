package com.example.inboundinventorytracker.ui

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.inboundinventorytracker.R
import com.example.inboundinventorytracker.util.AuditTrailLogger
import com.example.inboundinventorytracker.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    appViewModel: AppViewModel,
    navController: NavHostController
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        AuditTrailLogger.initialize(context)
        appViewModel.logout()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.app_name)) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Welcome to",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(id = R.string.app_name),
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    showError = false
                },
                label = { Text(stringResource(id = R.string.login_hint)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = showError,
                supportingText = {
                    if (showError) {
                        Text("Please enter your name", color = MaterialTheme.colorScheme.error)
                    }
                },
                keyboardOptions = KeyboardOptions(
                    autoCorrect = false,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (name.isBlank()) {
                            showError = true
                        } else {
                            appViewModel.login(name)
                            navController.navigate("main_menu") {
                                popUpTo("login") { inclusive = true }
                            }
                            AuditTrailLogger.log(name, "login", "User logged in")
                        }
                    }
                ),
                textStyle = TextStyle(textDecoration = TextDecoration.None),
                visualTransformation = VisualTransformation.None
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (name.isBlank()) {
                        showError = true
                    } else {
                        appViewModel.login(name)
                        navController.navigate("main_menu") {
                            popUpTo("login") { inclusive = true }
                        }
                        AuditTrailLogger.log(name, "login", "User logged in")
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text(stringResource(id = R.string.submit))
            }
        }
    }
}