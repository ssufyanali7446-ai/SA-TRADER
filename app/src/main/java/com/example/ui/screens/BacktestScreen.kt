package com.example.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.BacktestParams
import com.example.model.BotStrategyType
import com.example.model.OrderSide
import com.example.model.TradingPair
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
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@Composable
fun BacktestScreen(
    viewModel: TradingViewModel,
    modifier: Modifier = Modifier
) {
    val backtestParams by viewModel.backtestParams.collectAsStateWithLifecycle()
    val isBacktesting by viewModel.isBacktesting.collectAsStateWithLifecycle()
    val backtestResult by viewModel.backtestResult.collectAsStateWithLifecycle()

    var selectedSymbol by remember(backtestParams.symbol) { mutableStateOf(backtestParams.symbol) }
    var selectedStrategy by remember(backtestParams.strategy) { mutableStateOf(backtestParams.strategy) }
    var selectedDays by remember(backtestParams.periodDays) { mutableIntStateOf(backtestParams.periodDays) }
    var lotSize by remember(backtestParams.lotSize) { mutableDoubleStateOf(backtestParams.lotSize) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SlateDarkBackground)
            .padding(horizontal = 14.dp)
            .testTag("backtest_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Backtesting Strategy Configuration Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateDarkCard),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SlateDarkBorder),
                modifier = Modifier.fillMaxWidth().testTag("backtest_config_card")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Timeline, "Backtesting", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Strategy Validation & Backtesting Engine", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Asset Selection
                    Text("Select Pair:", fontSize = 12.sp, color = TextSecondary)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        TradingPair.ALL_PAIRS.take(4).forEach { pair ->
                            val isSel = selectedSymbol == pair.symbol
                            Surface(
                                onClick = { selectedSymbol = pair.symbol },
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSel) MaterialTheme.colorScheme.primary else SlateDarkBackground,
                                modifier = Modifier.weight(1f).height(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        pair.symbol,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSel) Color.Black else TextSecondary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Strategy Selection
                    Text("Select Strategy:", fontSize = 12.sp, color = TextSecondary)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        BotStrategyType.values().forEach { st ->
                            val isSel = selectedStrategy == st
                            Surface(
                                onClick = { selectedStrategy = st },
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSel) CyanAccent.copy(alpha = 0.2f) else SlateDarkBackground,
                                border = if (isSel) androidx.compose.foundation.BorderStroke(1.dp, CyanAccent) else null,
                                modifier = Modifier.weight(1f).height(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(2.dp)) {
                                    Text(
                                        text = when(st) {
                                            BotStrategyType.RSI_REVERSAL -> "RSI"
                                            BotStrategyType.EMA_CROSSOVER -> "EMA Cross"
                                            BotStrategyType.BOLLINGER_BREAKOUT -> "Bollinger"
                                            BotStrategyType.GRID_SCALPER -> "Grid"
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSel) CyanAccent else TextMuted
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Duration Selection
                    Text("Historical Simulation Period:", fontSize = 12.sp, color = TextSecondary)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(7, 30, 90, 180).forEach { days ->
                            val isSel = selectedDays == days
                            Surface(
                                onClick = { selectedDays = days },
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSel) MaterialTheme.colorScheme.primary else SlateDarkBackground,
                                modifier = Modifier.weight(1f).height(30.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        "${days}D",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isSel) Color.Black else TextSecondary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Execute Backtest Button
                    Button(
                        onClick = {
                            viewModel.updateBacktestParams(
                                BacktestParams(
                                    symbol = selectedSymbol,
                                    strategy = selectedStrategy,
                                    periodDays = selectedDays,
                                    lotSize = lotSize
                                )
                            )
                            viewModel.runBacktest()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(10.dp),
                        enabled = !isBacktesting,
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("run_backtest_btn")
                    ) {
                        if (isBacktesting) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Simulating Historical Bars...", color = Color.Black, fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Run Backtest on $selectedSymbol", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 2. Backtest Metrics Scorecard
        backtestResult?.let { res ->
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SlateDarkCard),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SlateDarkBorder),
                    modifier = Modifier.fillMaxWidth().testTag("backtest_results_card")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Validation Performance", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Surface(
                                color = if (res.totalPnl >= 0) BullishGreen.copy(alpha = 0.2f) else BearishRed.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = if (res.totalPnl >= 0) "STRATEGY PROFITABLE" else "STRATEGY LOSS",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (res.totalPnl >= 0) BullishGreen else BearishRed,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // 4 Big Metrics in Grid
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            MetricCard(title = "Win Rate", value = "${String.format("%.1f", res.winRate)}%", color = BullishGreen, modifier = Modifier.weight(1f))
                            MetricCard(title = "Net PnL", value = "${if (res.totalPnl >= 0) "+$" else "-$"}${String.format("%.2f", abs(res.totalPnl))}", color = if (res.totalPnl >= 0) BullishGreen else BearishRed, modifier = Modifier.weight(1f))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            MetricCard(title = "Profit Factor", value = String.format("%.2f", res.profitFactor), color = CyanAccent, modifier = Modifier.weight(1f))
                            MetricCard(title = "Max Drawdown", value = "${String.format("%.2f", res.maxDrawdownPercent)}%", color = BearishRed, modifier = Modifier.weight(1f))
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth().background(SlateDarkBackground, RoundedCornerShape(6.dp)).padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Executed Trades: ${res.totalTrades}", fontSize = 11.sp, color = TextSecondary)
                            Text("Wins: ${res.wins} | Losses: ${res.losses}", fontSize = 11.sp, color = TextPrimary)
                        }
                    }
                }
            }

            // 3. Equity Curve Chart
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SlateDarkCard),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SlateDarkBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Equity Growth Curve ($10,000 Starting)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .background(SlateDarkBackground, RoundedCornerShape(8.dp))
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                                val pts = res.equityCurve
                                if (pts.size < 2) return@Canvas

                                val w = size.width
                                val h = size.height
                                val minBal = pts.minOf { it.second }
                                val maxBal = pts.maxOf { it.second }
                                val range = if (maxBal - minBal > 0) maxBal - minBal else 100.0

                                val path = Path()
                                val stepX = w / (pts.size - 1)

                                pts.forEachIndexed { i, pt ->
                                    val x = i * stepX
                                    val norm = (maxBal - pt.second) / range
                                    val y = (norm * h).toFloat()
                                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                                }

                                drawPath(path, BullishGreen, style = Stroke(width = 2.5f))

                                // Starting $10,000 baseline
                                val baseNorm = (maxBal - 10000.0) / range
                                val baseY = (baseNorm * h).toFloat().coerceIn(0f, h)
                                drawLine(Color.Gray.copy(alpha = 0.4f), Offset(0f, baseY), Offset(w, baseY), strokeWidth = 1f)
                            }
                        }
                    }
                }
            }

            // 4. Trades breakdown list
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SlateDarkCard),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SlateDarkBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Simulated Trade Logs (${res.trades.size})", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Spacer(modifier = Modifier.height(8.dp))

                        res.trades.take(10).forEach { trade ->
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
                                        Text(
                                            text = trade.side.name,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (trade.side == OrderSide.BUY) BullishGreen else BearishRed
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("${trade.lots} lots", fontSize = 11.sp, color = TextPrimary)
                                    }
                                    Text(
                                        text = "${trade.closeReason} @ ${trade.closePrice}",
                                        fontSize = 10.sp,
                                        color = TextMuted
                                    )
                                }

                                Text(
                                    text = "${if (trade.pnl >= 0) "+$" else "-$"}${String.format("%.2f", abs(trade.pnl))}",
                                    fontSize = 13.sp,
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
}

@Composable
private fun MetricCard(
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
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = color, modifier = Modifier.padding(top = 2.dp))
        }
    }
}
