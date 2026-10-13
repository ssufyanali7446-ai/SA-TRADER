package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.AlertCondition
import com.example.model.ApiConnection
import com.example.model.OrderSide
import com.example.model.RiskSettings
import com.example.ui.TradingViewModel
import com.example.ui.theme.BearishRed
import com.example.ui.theme.BullishGreen
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.SlateDarkBackground
import com.example.ui.theme.SlateDarkBorder
import com.example.ui.theme.SlateDarkCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@Composable
fun DashboardAndApiScreen(
    viewModel: TradingViewModel,
    modifier: Modifier = Modifier
) {
    val balance by viewModel.repository.balance.collectAsStateWithLifecycle()
    val unrealizedPnl by viewModel.repository.unrealizedPnl.collectAsStateWithLifecycle()
    val realizedPnl by viewModel.repository.realizedPnl.collectAsStateWithLifecycle()
    val openPositions by viewModel.repository.openPositions.collectAsStateWithLifecycle()
    val tradeHistory by viewModel.repository.tradeHistory.collectAsStateWithLifecycle()
    val riskSettings by viewModel.repository.riskSettings.collectAsStateWithLifecycle()
    val alerts by viewModel.repository.alerts.collectAsStateWithLifecycle()
    val apiConnections by viewModel.repository.apiConnections.collectAsStateWithLifecycle()

    var showCreateAlertDialog by remember { mutableStateOf(false) }
    var historyFilter by remember { mutableStateOf("ALL") } // ALL, WON, LOST

    var editingApiIndex by remember { mutableStateOf<Int?>(null) }
    var editApiKeyText by remember { mutableStateOf("") }
    var editApiSecretText by remember { mutableStateOf("") }

    val equity = balance + unrealizedPnl

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SlateDarkBackground)
            .padding(horizontal = 14.dp)
            .testTag("dashboard_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Portfolio Balance & Equity Header
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateDarkCard),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SlateDarkBorder),
                modifier = Modifier.fillMaxWidth().testTag("portfolio_overview_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Total Account Equity", fontSize = 12.sp, color = TextMuted)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$${String.format("%,.2f", equity)}",
                            fontSize = 30.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )

                        Surface(
                            color = if (unrealizedPnl >= 0) BullishGreen.copy(alpha = 0.2f) else BearishRed.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "Floating: ${if (unrealizedPnl >= 0) "+$" else "-$"}${String.format("%.2f", abs(unrealizedPnl))}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (unrealizedPnl >= 0) BullishGreen else BearishRed,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Secondary stats row: Balance, Realized PnL, Used Margin
                    val usedMargin = openPositions.sumOf { it.lots * 1000.0 } // 1:100 leverage simulation
                    val freeMargin = (equity - usedMargin).coerceAtLeast(0.0)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SlateDarkBackground, RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Balance", fontSize = 10.sp, color = TextMuted)
                            Text("$${String.format("%,.2f", balance)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Column {
                            Text("Realized PnL", fontSize = 10.sp, color = TextMuted)
                            Text(
                                text = "${if (realizedPnl >= 0) "+$" else "-$"}${String.format("%.2f", abs(realizedPnl))}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (realizedPnl >= 0) BullishGreen else BearishRed
                            )
                        }
                        Column {
                            Text("Free Margin", fontSize = 10.sp, color = TextMuted)
                            Text("$${String.format("%,.2f", freeMargin)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CyanAccent)
                        }
                    }
                }
            }
        }

        // 2. Trade Performance Summary
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateDarkCard),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SlateDarkBorder),
                modifier = Modifier.fillMaxWidth().testTag("performance_summary_card")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Trade Performance Analytics", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)

                    val totalClosed = tradeHistory.size
                    val wins = tradeHistory.count { it.pnl > 0 }
                    val losses = tradeHistory.count { it.pnl <= 0 }
                    val winRate = if (totalClosed > 0) (wins.toDouble() / totalClosed) * 100.0 else 0.0
                    val bestTrade = tradeHistory.maxOfOrNull { it.pnl } ?: 0.0
                    val worstTrade = tradeHistory.minOfOrNull { it.pnl } ?: 0.0

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PerformanceMetricBox(title = "Win Rate", value = "${String.format("%.1f", winRate)}%", color = BullishGreen, modifier = Modifier.weight(1f))
                        PerformanceMetricBox(title = "Closed Trades", value = "$totalClosed ($wins W / $losses L)", color = TextPrimary, modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PerformanceMetricBox(title = "Best Trade", value = "+$${String.format("%.2f", bestTrade)}", color = BullishGreen, modifier = Modifier.weight(1f))
                        PerformanceMetricBox(title = "Worst Trade", value = "${if (worstTrade >= 0) "+$" else "-$"}${String.format("%.2f", abs(worstTrade))}", color = BearishRed, modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // 3. Risk Management Engine
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateDarkCard),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SlateDarkBorder),
                modifier = Modifier.fillMaxWidth().testTag("risk_management_card")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, "Risk Guard", tint = CyanAccent, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Risk Management & Safety Guards", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Daily Max Loss Slider
                    Text(
                        text = "Daily Max Loss Limit: $${riskSettings.dailyMaxLoss.toInt()}",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Slider(
                        value = riskSettings.dailyMaxLoss.toFloat(),
                        onValueChange = {
                            viewModel.updateRiskSettings(riskSettings.copy(dailyMaxLoss = it.toDouble()))
                        },
                        valueRange = 100f..2000f,
                        steps = 18,
                        colors = SliderDefaults.colors(thumbColor = CyanAccent, activeTrackColor = CyanAccent),
                        modifier = Modifier.testTag("slider_daily_max_loss")
                    )

                    // Circuit Breaker Switch
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Emergency Circuit Breaker", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Text("Auto-halts bot if daily loss limit is reached", fontSize = 10.sp, color = TextMuted)
                        }
                        Switch(
                            checked = riskSettings.emergencyCircuitBreakerEnabled,
                            onCheckedChange = {
                                viewModel.updateRiskSettings(riskSettings.copy(emergencyCircuitBreakerEnabled = it))
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = CyanAccent)
                        )
                    }
                }
            }
        }

        // 4. Market Trend Alerts Section
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateDarkCard),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SlateDarkBorder),
                modifier = Modifier.fillMaxWidth().testTag("alerts_card")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.NotificationsActive, "Alerts", tint = GoldPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Market Trend & Price Alerts (${alerts.size})", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }

                        Button(
                            onClick = { showCreateAlertDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(30.dp).testTag("btn_add_alert")
                        ) {
                            Icon(Icons.Default.Add, null, tint = Color.Black, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (alerts.isEmpty()) {
                        Text("No active alerts set.", fontSize = 12.sp, color = TextMuted)
                    } else {
                        alerts.forEach { alert ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .background(SlateDarkBackground, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(alert.symbol, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            color = if (alert.isTriggered) BullishGreen.copy(alpha = 0.2f) else SlateDarkCard,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = if (alert.isTriggered) "TRIGGERED" else "ACTIVE",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (alert.isTriggered) BullishGreen else TextMuted,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "${alert.condition.name}: ${alert.targetValue} (${alert.note})",
                                        fontSize = 10.sp,
                                        color = TextSecondary
                                    )
                                }

                                IconButton(
                                    onClick = { viewModel.deleteAlert(alert.id) },
                                    modifier = Modifier.size(26.dp)
                                ) {
                                    Icon(Icons.Default.Delete, "Delete", tint = TextMuted, modifier = Modifier.size(15.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. Secure Exchange API Connectivity
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateDarkCard),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SlateDarkBorder),
                modifier = Modifier.fillMaxWidth().testTag("api_connections_card")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Key, "API Keys", tint = GoldPrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Secure Exchange API Connectivity", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    Text("Encrypted credentials for MT5 & exchange trading dispatch", fontSize = 11.sp, color = TextMuted)

                    Spacer(modifier = Modifier.height(10.dp))

                    apiConnections.forEachIndexed { index, conn ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .background(SlateDarkBackground, RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(conn.exchangeName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = if (conn.isConnected) BullishGreen.copy(alpha = 0.2f) else SlateDarkBorder,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = if (conn.isConnected) "ONLINE (${conn.pingMs}ms)" else "DISCONNECTED",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (conn.isConnected) BullishGreen else TextMuted,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = if (conn.apiKey.isNotEmpty()) "Key: ${conn.apiKey.take(10)}... | ${conn.accountIdOrServer}" else "No API Key configured",
                                    fontSize = 10.sp,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }

                            Button(
                                onClick = {
                                    editingApiIndex = index
                                    editApiKeyText = conn.apiKey
                                    editApiSecretText = conn.apiSecret
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SlateDarkCard),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text("Edit", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }

        // 6. Trade History Log Section
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateDarkCard),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SlateDarkBorder),
                modifier = Modifier.fillMaxWidth().testTag("trade_history_card")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Trade History", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)

                        // Filters
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("ALL", "WON", "LOST").forEach { f ->
                                Surface(
                                    onClick = { historyFilter = f },
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (historyFilter == f) MaterialTheme.colorScheme.primary else SlateDarkBackground,
                                    modifier = Modifier.height(24.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 6.dp)) {
                                        Text(f, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (historyFilter == f) Color.Black else TextSecondary)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val filteredHistory = tradeHistory.filter {
                        when (historyFilter) {
                            "WON" -> it.pnl > 0
                            "LOST" -> it.pnl <= 0
                            else -> true
                        }
                    }

                    if (filteredHistory.isEmpty()) {
                        Text("No trades match filter.", fontSize = 11.sp, color = TextMuted, modifier = Modifier.padding(vertical = 8.dp))
                    } else {
                        filteredHistory.forEach { trade ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .background(SlateDarkBackground, RoundedCornerShape(6.dp))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(trade.symbol, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            color = if (trade.side == OrderSide.BUY) BullishGreen.copy(alpha = 0.2f) else BearishRed.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(3.dp)
                                        ) {
                                            Text(trade.side.name, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (trade.side == OrderSide.BUY) BullishGreen else BearishRed, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                        }
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("${trade.lots} lots", fontSize = 10.sp, color = TextMuted)
                                    }
                                    val dateStr = SimpleDateFormat("dd MMM HH:mm", Locale.getDefault()).format(Date(trade.openTime))
                                    Text(
                                        text = "$dateStr • ${trade.strategyTag} (${trade.closeReason ?: "Closed"})",
                                        fontSize = 10.sp,
                                        color = TextSecondary,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }

                                Text(
                                    text = "${if (trade.pnl >= 0) "+$" else "-$"}${String.format("%.2f", abs(trade.pnl))}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (trade.pnl >= 0) BullishGreen else BearishRed
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // Create Alert Dialog
    if (showCreateAlertDialog) {
        var alertSymbol by remember { mutableStateOf("AUX/USD") }
        var alertTargetPrice by remember { mutableStateOf("2700.0") }
        var alertNote by remember { mutableStateOf("Breakout Alert") }

        AlertDialog(
            onDismissRequest = { showCreateAlertDialog = false },
            title = { Text("Set Market Alert", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Select Pair:", fontSize = 12.sp, color = TextSecondary)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("AUX/USD", "EUR/USD", "GBP/USD").forEach { sym ->
                            Surface(
                                onClick = { alertSymbol = sym },
                                shape = RoundedCornerShape(4.dp),
                                color = if (alertSymbol == sym) MaterialTheme.colorScheme.primary else SlateDarkCard
                            ) {
                                Text(sym, fontSize = 11.sp, color = if (alertSymbol == sym) Color.Black else TextPrimary, modifier = Modifier.padding(6.dp))
                            }
                        }
                    }

                    OutlinedTextField(
                        value = alertTargetPrice,
                        onValueChange = { alertTargetPrice = it },
                        label = { Text("Target Price Threshold") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary),
                        modifier = Modifier.fillMaxWidth().testTag("input_alert_price")
                    )

                    OutlinedTextField(
                        value = alertNote,
                        onValueChange = { alertNote = it },
                        label = { Text("Alert Description / Note") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val priceNum = alertTargetPrice.toDoubleOrNull() ?: 2700.0
                        viewModel.createAlert(alertSymbol, AlertCondition.PRICE_ABOVE, priceNum, alertNote)
                        showCreateAlertDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Save Alert", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateAlertDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = SlateDarkCard
        )
    }

    // Edit API Connection Dialog
    editingApiIndex?.let { idx ->
        val conn = apiConnections[idx]
        AlertDialog(
            onDismissRequest = { editingApiIndex = null },
            title = { Text("Configure ${conn.exchangeName}", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter API credentials for trade execution:", fontSize = 12.sp, color = TextMuted)
                    OutlinedTextField(
                        value = editApiKeyText,
                        onValueChange = { editApiKeyText = it },
                        label = { Text("API Key") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editApiSecretText,
                        onValueChange = { editApiSecretText = it },
                        label = { Text("API Secret / Password") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val updated = conn.copy(
                            apiKey = editApiKeyText,
                            apiSecret = editApiSecretText,
                            isConnected = editApiKeyText.isNotEmpty()
                        )
                        viewModel.updateApiConnection(idx, updated)
                        editingApiIndex = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Save & Connect", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingApiIndex = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = SlateDarkCard
        )
    }
}

@Composable
private fun PerformanceMetricBox(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(SlateDarkBackground, RoundedCornerShape(8.dp))
            .border(1.dp, SlateDarkBorder, RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Column {
            Text(title, fontSize = 10.sp, color = TextMuted)
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = color, modifier = Modifier.padding(top = 2.dp))
        }
    }
}
