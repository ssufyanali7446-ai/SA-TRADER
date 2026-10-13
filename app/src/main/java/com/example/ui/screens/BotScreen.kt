package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.BotConfig
import com.example.model.BotStrategyType
import com.example.model.TradingPair
import com.example.ui.TradingViewModel
import com.example.ui.theme.BearishRed
import com.example.ui.theme.BullishGreen
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.SlateDarkBackground
import com.example.ui.theme.SlateDarkBorder
import com.example.ui.theme.SlateDarkCard
import com.example.ui.theme.SlateDarkSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun BotScreen(
    viewModel: TradingViewModel,
    modifier: Modifier = Modifier
) {
    val botConfig by viewModel.repository.botConfig.collectAsStateWithLifecycle()
    val openPositions by viewModel.repository.openPositions.collectAsStateWithLifecycle()
    val riskSettings by viewModel.repository.riskSettings.collectAsStateWithLifecycle()

    var activeStrategy by remember(botConfig.strategy) { mutableStateOf(botConfig.strategy) }
    var lotSize by remember(botConfig.lotSize) { mutableDoubleStateOf(botConfig.lotSize) }
    var tpPips by remember(botConfig.takeProfitPips) { mutableDoubleStateOf(botConfig.takeProfitPips) }
    var slPips by remember(botConfig.stopLossPips) { mutableDoubleStateOf(botConfig.stopLossPips) }
    var trailingStop by remember(botConfig.trailingStop) { mutableStateOf(botConfig.trailingStop) }
    var maxPositions by remember(botConfig.maxOpenPositions) { mutableIntStateOf(botConfig.maxOpenPositions) }
    var targetSymbol by remember(botConfig.symbol) { mutableStateOf(botConfig.symbol) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SlateDarkBackground)
            .padding(horizontal = 14.dp)
            .testTag("bot_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Bot Master Status Banner
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateDarkCard),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    if (botConfig.enabled) BullishGreen else SlateDarkBorder
                ),
                modifier = Modifier.fillMaxWidth().testTag("bot_master_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .background(
                                        if (botConfig.enabled) BullishGreen else BearishRed,
                                        CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (botConfig.enabled) "AUTO BOT ACTIVE" else "BOT PAUSED",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (botConfig.enabled) BullishGreen else TextSecondary
                                )
                                Text(
                                    text = "Targeting ${botConfig.symbol} • ${botConfig.strategy.displayName}",
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )
                            }
                        }

                        Switch(
                            checked = botConfig.enabled,
                            onCheckedChange = { isEnabled ->
                                viewModel.updateBotConfig(
                                    botConfig.copy(
                                        enabled = isEnabled,
                                        strategy = activeStrategy,
                                        symbol = targetSymbol,
                                        lotSize = lotSize,
                                        takeProfitPips = tpPips,
                                        stopLossPips = slPips,
                                        trailingStop = trailingStop,
                                        maxOpenPositions = maxPositions
                                    )
                                )
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = BullishGreen,
                                uncheckedThumbColor = TextMuted,
                                uncheckedTrackColor = SlateDarkBackground
                            ),
                            modifier = Modifier.testTag("bot_master_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Stats in banner
                    val botOpenCount = openPositions.count { it.type.name.contains("BOT") }
                    val botTotalPnl = openPositions.filter { it.type.name.contains("BOT") }.sumOf { it.pnl }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SlateDarkBackground, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Active Bot Trades", fontSize = 10.sp, color = TextMuted)
                            Text("$botOpenCount / $maxPositions", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Column {
                            Text("Unrealized Bot PnL", fontSize = 10.sp, color = TextMuted)
                            Text(
                                text = "${if (botTotalPnl >= 0) "+$" else "-$"}${String.format("%.2f", kotlin.math.abs(botTotalPnl))}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (botTotalPnl >= 0) BullishGreen else BearishRed
                            )
                        }
                        Column {
                            Text("Daily Loss Guard", fontSize = 10.sp, color = TextMuted)
                            Text("$${String.format("%.0f", riskSettings.currentDailyLoss)} / $${String.format("%.0f", riskSettings.dailyMaxLoss)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CyanAccent)
                        }
                    }
                }
            }
        }

        // 2. Target Asset Selector
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateDarkCard),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SlateDarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Select Target Trading Pair", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        TradingPair.ALL_PAIRS.take(4).forEach { pair ->
                            val isSelected = targetSymbol == pair.symbol
                            Surface(
                                onClick = {
                                    targetSymbol = pair.symbol
                                    viewModel.updateBotConfig(botConfig.copy(symbol = pair.symbol))
                                },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else SlateDarkBackground,
                                border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, SlateDarkBorder) else null,
                                modifier = Modifier.weight(1f).height(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = pair.symbol,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.Black else TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Strategy Selection Mode
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateDarkCard),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SlateDarkBorder),
                modifier = Modifier.fillMaxWidth().testTag("strategy_picker_card")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Automated Strategy Engine", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(10.dp))

                    BotStrategyType.values().forEach { strategy ->
                        val isSelected = activeStrategy == strategy
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) SlateDarkSurface else SlateDarkBackground)
                                .border(
                                    1.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary else SlateDarkBorder,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    activeStrategy = strategy
                                    viewModel.updateBotConfig(botConfig.copy(strategy = strategy))
                                }
                                .padding(12.dp)
                                .testTag("strategy_option_${strategy.name}")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = strategy.displayName,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else TextPrimary
                                    )
                                    Text(
                                        text = strategy.description,
                                        fontSize = 11.sp,
                                        color = TextMuted,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Fine-Tuning Parameters & Risk Controls
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateDarkCard),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SlateDarkBorder),
                modifier = Modifier.fillMaxWidth().testTag("bot_params_card")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Tune, "Parameters", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Risk & Execution Parameters", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Lot Size Slider
                    Text("Lot Size per Trade: ${String.format("%.2f", lotSize)} lots", fontSize = 12.sp, color = TextSecondary)
                    Slider(
                        value = lotSize.toFloat(),
                        onValueChange = { lotSize = it.toDouble() },
                        onValueChangeFinished = { viewModel.updateBotConfig(botConfig.copy(lotSize = lotSize)) },
                        valueRange = 0.01f..1.00f,
                        steps = 19,
                        colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary, activeTrackColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.testTag("slider_bot_lot")
                    )

                    // TP Pips Slider
                    Text("Take Profit Target: ${tpPips.toInt()} pips", fontSize = 12.sp, color = BullishGreen)
                    Slider(
                        value = tpPips.toFloat(),
                        onValueChange = { tpPips = it.toDouble() },
                        onValueChangeFinished = { viewModel.updateBotConfig(botConfig.copy(takeProfitPips = tpPips)) },
                        valueRange = 10f..150f,
                        steps = 13,
                        colors = SliderDefaults.colors(thumbColor = BullishGreen, activeTrackColor = BullishGreen),
                        modifier = Modifier.testTag("slider_bot_tp")
                    )

                    // SL Pips Slider
                    Text("Stop Loss Guard: ${slPips.toInt()} pips", fontSize = 12.sp, color = BearishRed)
                    Slider(
                        value = slPips.toFloat(),
                        onValueChange = { slPips = it.toDouble() },
                        onValueChangeFinished = { viewModel.updateBotConfig(botConfig.copy(stopLossPips = slPips)) },
                        valueRange = 10f..100f,
                        steps = 8,
                        colors = SliderDefaults.colors(thumbColor = BearishRed, activeTrackColor = BearishRed),
                        modifier = Modifier.testTag("slider_bot_sl")
                    )

                    // Trailing Stop Loss Switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Trailing Stop Loss", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Text("Locks in profits as market moves in favor", fontSize = 10.sp, color = TextMuted)
                        }
                        Switch(
                            checked = trailingStop,
                            onCheckedChange = {
                                trailingStop = it
                                viewModel.updateBotConfig(botConfig.copy(trailingStop = it))
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = MaterialTheme.colorScheme.primary)
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
