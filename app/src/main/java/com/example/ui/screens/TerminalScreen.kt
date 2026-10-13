package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CandlestickChart
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.OrderSide
import com.example.model.TradingPair
import com.example.ui.TradingViewModel
import com.example.ui.components.InteractiveTradingChart
import com.example.ui.components.PairSelectorBar
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

@Composable
fun TerminalScreen(
    viewModel: TradingViewModel,
    onOpenDeposit: () -> Unit = {},
    onOpenAuth: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val selectedPair by viewModel.selectedPair.collectAsStateWithLifecycle()
    val liveTicks by viewModel.repository.liveTicks.collectAsStateWithLifecycle()
    val candles by viewModel.currentCandles.collectAsStateWithLifecycle()
    val openPositions by viewModel.repository.openPositions.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnlineSynced.collectAsStateWithLifecycle()
    val isChartVisible by viewModel.isChartVisible.collectAsStateWithLifecycle()
    val goldSpotData by viewModel.goldSpotData.collectAsStateWithLifecycle()

    val selectedTf by viewModel.selectedTimeframe.collectAsStateWithLifecycle()
    val isCandleMode by viewModel.isCandlestickMode.collectAsStateWithLifecycle()
    val showEma by viewModel.showEma.collectAsStateWithLifecycle()
    val showBollinger by viewModel.showBollinger.collectAsStateWithLifecycle()
    val showRsi by viewModel.showRsi.collectAsStateWithLifecycle()

    val currentTick = liveTicks[selectedPair.symbol]

    var tradeLotSize by remember { mutableDoubleStateOf(0.10) }
    var tpPips by remember { mutableDoubleStateOf(50.0) }
    var slPips by remember { mutableDoubleStateOf(30.0) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SlateDarkBackground)
            .padding(horizontal = 14.dp)
            .testTag("terminal_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Top Bar: Asset Symbol, Live Rate, 24h Stats, Feed Status & Hide/Show Chart Icon
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateDarkCard),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SlateDarkBorder),
                modifier = Modifier.fillMaxWidth().testTag("pair_header_card")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = selectedPair.symbol,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                            if (selectedPair.isGold) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = GoldPrimary.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(4.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary)
                                ) {
                                    Text(
                                        text = "GOLD BULLION",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GoldPrimary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Toggle Live Chart Icon button
                            Surface(
                                onClick = { viewModel.toggleChartVisibility() },
                                color = if (isChartVisible) SlateDarkBackground else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isChartVisible) SlateDarkBorder else MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.padding(end = 6.dp).testTag("toggle_chart_visibility_btn")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isChartVisible) Icons.Default.VisibilityOff else Icons.Default.ShowChart,
                                        contentDescription = if (isChartVisible) "Hide Chart" else "Show Chart",
                                        tint = if (isChartVisible) TextSecondary else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isChartVisible) "Hide Chart" else "Show Chart",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isChartVisible) TextSecondary else MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            // Online feed badge
                            Surface(
                                color = if (isOnline) BullishGreen.copy(alpha = 0.15f) else BearishRed.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.padding(end = 4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isOnline) Icons.Default.Wifi else Icons.Default.WifiOff,
                                        contentDescription = "Feed Status",
                                        tint = if (isOnline) BullishGreen else BearishRed,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = if (isOnline) "LIVE" else "OFFLINE",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isOnline) BullishGreen else BearishRed
                                    )
                                }
                            }

                            // Quick Deposit Button ($1 to $500 EasyPaisa/JazzCash)
                            Surface(
                                onClick = onOpenDeposit,
                                color = BullishGreen,
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.padding(end = 4.dp).testTag("header_deposit_btn")
                            ) {
                                Text(
                                    text = "+ ڈپازٹ",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.Black,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                                )
                            }

                            // Profile / Sign In button
                            IconButton(
                                onClick = onOpenAuth,
                                modifier = Modifier.size(32.dp).testTag("header_auth_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = "User Account",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // Large Live Price & 24h change
                    val price = currentTick?.price ?: selectedPair.initialPrice
                    val changeP = currentTick?.changePercent24h ?: 0.0
                    val isUp = changeP >= 0.0

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = String.format("%.${selectedPair.decimals}f", price),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (currentTick?.isUpTick == true) BullishGreen else BearishRed
                            )
                            Text(
                                text = selectedPair.name,
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Surface(
                                color = if (isUp) BullishGreen.copy(alpha = 0.15f) else BearishRed.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "${if (isUp) "+" else ""}${String.format("%.2f", changeP)}%",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isUp) BullishGreen else BearishRed,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Text(
                                text = "24h H: ${String.format("%.${selectedPair.decimals}f", currentTick?.high24h ?: price)} | L: ${String.format("%.${selectedPair.decimals}f", currentTick?.low24h ?: price)}",
                                fontSize = 10.sp,
                                color = TextSecondary,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // 2. Dedicated Gold Live Price Spot Rates (AUX/USD) Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateDarkCard),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth().testTag("gold_live_spot_card")
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Gold Bullion",
                                tint = GoldPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "AUX/USD Gold Spot Live Feed",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldPrimary
                            )
                        }
                        Surface(
                            color = GoldPrimary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "24K SPOT RATE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 3 Gold Rates Grid: 1 Troy Ounce, 1 Gram 24K, 1 Tola
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 1 Ounce
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(SlateDarkBackground, RoundedCornerShape(8.dp))
                                .border(1.dp, SlateDarkBorder, RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Column {
                                Text("1 Troy Ounce (oz)", fontSize = 10.sp, color = TextMuted)
                                Text(
                                    text = "$${String.format("%,.2f", goldSpotData.ouncePrice)}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = GoldPrimary,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }

                        // 1 Gram
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(SlateDarkBackground, RoundedCornerShape(8.dp))
                                .border(1.dp, SlateDarkBorder, RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Column {
                                Text("1 Gram (24K)", fontSize = 10.sp, color = TextMuted)
                                Text(
                                    text = "$${String.format("%.2f", goldSpotData.gram24kPrice)}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }

                        // 1 Tola
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(SlateDarkBackground, RoundedCornerShape(8.dp))
                                .border(1.dp, SlateDarkBorder, RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Column {
                                Text("1 Tola (فی تولہ)", fontSize = 10.sp, color = TextMuted)
                                Text(
                                    text = "$${String.format("%,.0f", goldSpotData.tolaPriceUsd)}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyanAccent,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                                Text(
                                    text = "Rs ${String.format("%,.0f", goldSpotData.tolaPricePkr)}",
                                    fontSize = 9.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Pair Selector Horizontal Row
        item {
            PairSelectorBar(
                pairs = TradingPair.ALL_PAIRS,
                selectedPair = selectedPair,
                liveTicks = liveTicks,
                onPairSelected = { pair ->
                    viewModel.selectPair(pair)
                }
            )
        }

        // 4. Interactive Live Chart (Hidden with Icon or Expanded)
        item {
            AnimatedVisibility(
                visible = isChartVisible,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                InteractiveTradingChart(
                    candles = candles,
                    pair = selectedPair,
                    isCandlestickMode = isCandleMode,
                    showEma = showEma,
                    showBollinger = showBollinger,
                    showRsi = showRsi,
                    selectedTimeframe = selectedTf,
                    onTimeframeSelected = { viewModel.setTimeframe(it) },
                    onToggleChartMode = { viewModel.toggleCandlestickMode() },
                    onToggleEma = { viewModel.toggleEma() },
                    onToggleBollinger = { viewModel.toggleBollinger() },
                    onToggleRsi = { viewModel.toggleRsi() },
                    onQuickBuy = { lot -> viewModel.placeQuickOrder(OrderSide.BUY, lot, 50.0, 30.0) },
                    onQuickSell = { lot -> viewModel.placeQuickOrder(OrderSide.SELL, lot, 50.0, 30.0) }
                )
            }

            // Compact Bar when Live Chart is Hidden
            if (!isChartVisible) {
                Surface(
                    onClick = { viewModel.setChartVisible(true) },
                    color = SlateDarkCard,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SlateDarkBorder),
                    modifier = Modifier.fillMaxWidth().testTag("chart_collapsed_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ShowChart,
                                contentDescription = "Show Chart",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Live Chart Hidden",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Tap here to expand ${selectedPair.symbol} interactive candlestick chart",
                                    fontSize = 10.sp,
                                    color = TextMuted
                                )
                            }
                        }

                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Visibility, null, tint = Color.Black, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Expand", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            }
                        }
                    }
                }
            }
        }

        // 5. All Pairs Live Price Market Board (Watchlist with Icon Live Charts)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateDarkCard),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SlateDarkBorder),
                modifier = Modifier.fillMaxWidth().testTag("all_pairs_market_board")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(BullishGreen, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "All Pairs Live Price Feed",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Text(
                            text = "${TradingPair.ALL_PAIRS.size} Real Pairs",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    TradingPair.ALL_PAIRS.forEach { pair ->
                        val tick = liveTicks[pair.symbol]
                        val isSelected = pair.symbol == selectedPair.symbol
                        val changeP = tick?.changePercent24h ?: 0.0
                        val isUp = changeP >= 0.0
                        val price = tick?.price ?: pair.initialPrice

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) SlateDarkBackground else Color.Transparent)
                                .border(
                                    if (isSelected) 1.dp else 0.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    viewModel.selectPair(pair)
                                }
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                                .testTag("all_pairs_row_${pair.symbol.replace('/', '_')}"),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Pair Name & Symbol
                            Column(modifier = Modifier.weight(1.1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = pair.symbol,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else TextPrimary
                                    )
                                    if (pair.isGold) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(Icons.Default.Star, "Gold", tint = GoldPrimary, modifier = Modifier.size(13.dp))
                                    }
                                }
                                Text(
                                    text = pair.name.take(18),
                                    fontSize = 10.sp,
                                    color = TextMuted
                                )
                            }

                            // Live Price (flashes with tick)
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    text = String.format("%.${pair.decimals}f", price),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (tick?.isUpTick == true) BullishGreen else BearishRed
                                )
                                Text(
                                    text = "Spread: ${if (pair.isGold) "$0.25" else "1.2 pips"}",
                                    fontSize = 9.sp,
                                    color = TextSecondary
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // 24h Change Pill
                            Surface(
                                color = if (isUp) BullishGreen.copy(alpha = 0.15f) else BearishRed.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.width(62.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 3.dp)) {
                                    Text(
                                        text = "${if (isUp) "+" else ""}${String.format("%.2f", changeP)}%",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isUp) BullishGreen else BearishRed
                                    )
                                }
                            }

                            // Quick SELL & BUY Buttons for this pair (Hr chart K sath buy or sell ka option)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                                modifier = Modifier.padding(start = 2.dp)
                            ) {
                                Surface(
                                    onClick = {
                                        viewModel.selectPair(pair)
                                        viewModel.placeQuickOrder(OrderSide.SELL, 0.10, 50.0, 30.0)
                                    },
                                    color = BearishRed,
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.height(24.dp).testTag("quick_sell_${pair.symbol.replace('/', '_')}")
                                ) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 5.dp)) {
                                        Text("SELL", fontSize = 8.5.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                                    }
                                }

                                Surface(
                                    onClick = {
                                        viewModel.selectPair(pair)
                                        viewModel.placeQuickOrder(OrderSide.BUY, 0.10, 50.0, 30.0)
                                    },
                                    color = BullishGreen,
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.height(24.dp).testTag("quick_buy_${pair.symbol.replace('/', '_')}")
                                ) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 5.dp)) {
                                        Text("BUY", fontSize = 8.5.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                                    }
                                }

                                // Icon Live Chart button (loads & displays live chart for this pair)
                                IconButton(
                                    onClick = {
                                        viewModel.selectPair(pair)
                                        viewModel.setChartVisible(true)
                                    },
                                    modifier = Modifier
                                        .size(28.dp)
                                        .testTag("pair_chart_icon_${pair.symbol.replace('/', '_')}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CandlestickChart,
                                        contentDescription = "View ${pair.symbol} Live Chart",
                                        tint = if (isSelected && isChartVisible) MaterialTheme.colorScheme.primary else TextSecondary,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Active Open Positions on selected pair (Fast Monitor)
        val activePositionsForPair = openPositions.filter { it.symbol == selectedPair.symbol }
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateDarkCard),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SlateDarkBorder),
                modifier = Modifier.fillMaxWidth().testTag("open_positions_card")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Active Positions (${activePositionsForPair.size})",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        val totalPairPnl = activePositionsForPair.sumOf { it.pnl }
                        Text(
                            text = "${if (totalPairPnl >= 0) "+$" else "-$"}${String.format("%.2f", abs(totalPairPnl))}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (totalPairPnl >= 0) BullishGreen else BearishRed
                        )
                    }

                    if (activePositionsForPair.isEmpty()) {
                        Text(
                            text = "No open positions on ${selectedPair.symbol}. Automated bot or manual orders will appear here in real-time.",
                            fontSize = 12.sp,
                            color = TextMuted,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        Spacer(modifier = Modifier.height(8.dp))
                        activePositionsForPair.forEach { order ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SlateDarkBackground, RoundedCornerShape(8.dp))
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            color = if (order.side == OrderSide.BUY) BullishGreen.copy(alpha = 0.2f) else BearishRed.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = order.side.name,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (order.side == OrderSide.BUY) BullishGreen else BearishRed,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("${order.lots} lots", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                    }
                                    Text(
                                        text = "@ ${String.format("%.${selectedPair.decimals}f", order.openPrice)} | Now: ${String.format("%.${selectedPair.decimals}f", order.currentPrice)}",
                                        fontSize = 11.sp,
                                        color = TextSecondary,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "${if (order.pnl >= 0) "+$" else "-$"}${String.format("%.2f", abs(order.pnl))}",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (order.pnl >= 0) BullishGreen else BearishRed
                                        )
                                        Text(
                                            text = order.strategyTag,
                                            fontSize = 9.sp,
                                            color = TextMuted
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(
                                        onClick = { viewModel.closePosition(order.id) },
                                        modifier = Modifier.size(28.dp).testTag("close_order_${order.id}")
                                    ) {
                                        Icon(Icons.Default.Close, "Close Order", tint = TextMuted, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
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
