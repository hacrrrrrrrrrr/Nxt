package com.example

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.alpha
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import kotlinx.coroutines.launch
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.AddMoneyScreen
import com.example.ui.HomeScreen
import com.example.ui.LeaderboardScreen
import com.example.ui.PaymentConfirmationScreen
import com.example.ui.ProfileScreen
import com.example.ui.TournamentsScreen
import com.example.ui.WalletScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PrimaryOrange
import com.example.ui.theme.TextDark

import com.example.ui.AuthScreen
import io.github.jan.supabase.auth.auth
import com.example.network.SupabaseClient

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                
                val sessionStatus by SupabaseClient.client.auth.sessionStatus.collectAsState()
                
                LaunchedEffect(sessionStatus) {
                    when (sessionStatus) {
                        is io.github.jan.supabase.auth.status.SessionStatus.Authenticated -> {
                            kotlinx.coroutines.delay(1500L)
                            navController.navigate("main") { popUpTo("splash") { inclusive = true } }
                        }
                        is io.github.jan.supabase.auth.status.SessionStatus.NotAuthenticated -> {
                            kotlinx.coroutines.delay(1500L)
                            navController.navigate("auth") { popUpTo("splash") { inclusive = true } }
                        }
                        else -> {
                            // LoadingFromStorage or NetworkError, stay on splash for now
                        }
                    }
                }
                
                NavHost(navController = navController, startDestination = "splash") {
                    composable("splash") {
                        SplashScreen()
                    }
                        composable("auth") {
                            AuthScreen(
                                onAuthSuccess = {
                                    navController.navigate("main") {
                                        popUpTo("auth") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("main") {
                            val scope = rememberCoroutineScope()
                            MainScreen(
                                onNavigateToAddMoney = { navController.navigate("add_money") },
                                onLogout = {
                                    scope.launch {
                                        SupabaseClient.client.auth.signOut()
                                        navController.navigate("auth") {
                                            popUpTo(0) { inclusive = true }
                                        }
                                    }
                                }
                            )
                        }
                    composable("add_money") {
                        AddMoneyScreen(
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToConfirmation = { amount -> 
                                navController.navigate("payment_confirmation/$amount") {
                                    popUpTo("main")
                                }
                            }
                        )
                    }
                    composable("payment_confirmation/{amount}") { backStackEntry ->
                        val amount = backStackEntry.arguments?.getString("amount") ?: "0"
                        PaymentConfirmationScreen(
                            amount = amount,
                            onNavigateHome = {
                                navController.navigate("main") {
                                    popUpTo("main") { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MainScreen(
    onNavigateToAddMoney: () -> Unit,
    onLogout: () -> Unit
) {
    val homeViewModel: com.example.ui.HomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val uiState by homeViewModel.uiState.collectAsState()

    var selectedItem by remember { mutableStateOf(0) }
    val items = listOf("Home", "Matches", "Wallet", "Ranks", "Profile")
    val icons = listOf(
        Icons.Default.Home,
        Icons.Default.EmojiEvents,
        Icons.Default.AccountBalanceWallet,
        Icons.Default.Leaderboard,
        Icons.Default.Person
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                modifier = Modifier.navigationBarsPadding(),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(icons[index], contentDescription = item) },
                        label = { Text(item, fontSize = 10.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) },
                        selected = selectedItem == index,
                        onClick = { selectedItem = index },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PrimaryOrange,
                            selectedTextColor = PrimaryOrange,
                            indicatorColor = PrimaryOrange.copy(alpha = 0.1f),
                            unselectedIconColor = TextDark.copy(alpha = 0.5f),
                            unselectedTextColor = TextDark.copy(alpha = 0.5f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedItem) {
                0 -> HomeScreen(viewModel = homeViewModel, onAddMoneyClick = onNavigateToAddMoney)
                1 -> TournamentsScreen()
                2 -> WalletScreen(walletBalance = uiState.walletBalance, onAddMoneyClick = onNavigateToAddMoney)
                3 -> LeaderboardScreen()
                4 -> ProfileScreen(onLogout = onLogout)
            }
        }
    }
}


@Composable
fun SplashScreen() {
    var startAnimation by remember { mutableStateOf(false) }
    val scale = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.5f,
        animationSpec = tween(durationMillis = 1000)
    )
    val alpha = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1000)
    )

    LaunchedEffect(key1 = true) {
        startAnimation = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "App Logo",
            modifier = Modifier
                .size(200.dp)
                .scale(scale.value)
                .alpha(alpha.value)
        )
    }
}
