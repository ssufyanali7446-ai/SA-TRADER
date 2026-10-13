package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.TradingViewModel
import com.example.ui.screens.BacktestScreen
import com.example.ui.screens.BotScreen
import com.example.ui.screens.DashboardAndApiScreen
import com.example.ui.screens.TerminalScreen
import com.example.ui.screens.UrduExpertsScreen
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SlateDarkBackground
import com.example.ui.theme.SlateDarkBorder
import com.example.ui.theme.SlateDarkCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

enum class NavTab(val title: String, val icon: ImageVector) {
    TERMINAL("Terminal", Icons.Default.ShowChart),
    AUTO_BOT("Auto Bot", Icons.Default.AutoMode),
    BACKTEST("Backtest", Icons.Default.Timeline),
    URDU_VIEWS("اردو ویوز", Icons.Default.Psychology),
    DASHBOARD("Portfolio", Icons.Default.Dashboard)
}

class MainActivity : ComponentActivity() {
    private val tradingViewModel: TradingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                TradingApp(viewModel = tradingViewModel)
            }
        }
    }
}

@Composable
fun TradingApp(viewModel: TradingViewModel) {
    var selectedTab by remember { mutableStateOf(NavTab.TERMINAL) }
    val recentAlert by viewModel.repository.recentTriggeredAlert.collectAsStateWithLifecycle()

    // Handle back button: return to Terminal if on a sub-screen
    BackHandler(enabled = selectedTab != NavTab.TERMINAL) {
        selectedTab = NavTab.TERMINAL
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = SlateDarkBackground,
        bottomBar = {
            NavigationBar(
                containerColor = SlateDarkCard,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag("bottom_nav_bar")
            ) {
                NavTab.values().forEach { tab ->
                    val isSelected = selectedTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextMuted
                        ),
                        modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main Screen Content
            when (selectedTab) {
                NavTab.TERMINAL -> TerminalScreen(
                    viewModel = viewModel,
                    onOpenAlerts = { selectedTab = NavTab.DASHBOARD }
                )
                NavTab.AUTO_BOT -> BotScreen(viewModel = viewModel)
                NavTab.BACKTEST -> BacktestScreen(viewModel = viewModel)
                NavTab.URDU_VIEWS -> UrduExpertsScreen(viewModel = viewModel)
                NavTab.DASHBOARD -> DashboardAndApiScreen(viewModel = viewModel)
            }

            // Floating Alert Banner Notification when price/trend triggers
            AnimatedVisibility(
                visible = recentAlert != null,
                enter = slideInVertically { -it } + fadeIn(),
                exit = slideOutVertically { -it } + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                recentAlert?.let { alert ->
                    Surface(
                        color = SlateDarkCard,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, GoldPrimary),
                        shadowElevation = 8.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("alert_banner")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = "Alert Triggered",
                                tint = GoldPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "MARKET ALERT: ${alert.symbol}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldPrimary
                                )
                                Text(
                                    text = "${alert.condition.name} target ${alert.targetValue} reached! (${alert.note})",
                                    fontSize = 11.sp,
                                    color = TextPrimary
                                )
                            }
                            IconButton(
                                onClick = { viewModel.dismissRecentAlert() },
                                modifier = Modifier.size(28.dp).testTag("dismiss_alert_btn")
                            ) {
                                Icon(Icons.Default.Close, "Dismiss", tint = TextMuted, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
