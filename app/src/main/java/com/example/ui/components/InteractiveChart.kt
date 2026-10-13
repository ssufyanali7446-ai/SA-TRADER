package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CandlestickChart
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Candle
import com.example.model.TradingPair
import com.example.ui.theme.BearishRed
import com.example.ui.theme.BullishGreen
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.IndicatorBollinger
import com.example.ui.theme.IndicatorEMA20
import com.example.ui.theme.IndicatorRSI
import com.example.ui.theme.IndicatorSMA50
import com.example.ui.theme.SlateDarkBackground
import com.example.ui.theme.SlateDarkBorder
import com.example.ui.theme.SlateDarkCard
import com.example.ui.theme.SlateDarkSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.max
import kotlin.math.min

@Composable
fun InteractiveTradingChart(
    candles: List<Candle>,
    pair: TradingPair,
    isCandlestickMode: Boolean,
    showEma: Boolean,
    showBollinger: Boolean,
    showRsi: Boolean,
    selectedTimeframe: String,
    onTimeframeSelected: (String) -> Unit,
    onToggleChartMode: () -> Unit,
    onToggleEma: () -> Unit,
    onToggleBollinger: () -> Unit,
    onToggleRsi: () -> Unit,
    onQuickBuy: (lotSize: Double) -> Unit = {},
    onQuickSell: (lotSize: Double) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var touchX by remember { mutableStateOf<Float?>(null) }
    val timeframes = listOf("1M", "5M", "15M", "1H", "4H", "1D")

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SlateDarkCard, RoundedCornerShape(12.dp))
            .padding(12.dp)
            .testTag("interactive_trading_chart")
    ) {
        // Chart Control Header: Timeframes & Indicator Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Timeframe selection buttons
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                timeframes.forEach { tf ->
                    val isSelected = tf == selectedTimeframe
                    Surface(
                        onClick = { onTimeframeSelected(tf) },
                        shape = RoundedCornerShape(6.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                        border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, SlateDarkBorder) else null,
                        modifier = Modifier
                            .height(28.dp)
                            .testTag("timeframe_btn_$tf")
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(horizontal = 7.dp)
                        ) {
                            Text(
                                text = tf,
                                style = TextStyle(
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.Black else TextSecondary
                                )
                            )
                        }
                    }
                }
            }

            // Mode toggle: Candlestick vs Line
            IconButton(
                onClick = onToggleChartMode,
                modifier = Modifier
                    .size(32.dp)
                    .testTag("chart_mode_toggle")
            ) {
                Icon(
                    imageVector = if (isCandlestickMode) Icons.Default.CandlestickChart else Icons.Default.ShowChart,
                    contentDescription = "Toggle Chart Style",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        var chartHeightIndex by remember { mutableIntStateOf(1) } // 0: Compact, 1: Standard, 2: Large
        val chartHeights = listOf(200.dp, 275.dp, 390.dp)
        val chartSizeNames = listOf("چھوٹا", "درمیانہ", "بڑا")

        var zoomCount by remember { mutableIntStateOf(40) } // 22: Zoom In, 40: Normal, 70: Zoom Out

        // Indicator Toggle Chips & Sizing (Chota / Bara) Controls Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Technical Indicator Chips
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IndicatorFilterChip(label = "EMA", selected = showEma, color = IndicatorEMA20, onClick = onToggleEma, tag = "chip_ema")
                IndicatorFilterChip(label = "Boll", selected = showBollinger, color = IndicatorBollinger, onClick = onToggleBollinger, tag = "chip_bollinger")
                IndicatorFilterChip(label = "RSI", selected = showRsi, color = IndicatorRSI, onClick = onToggleRsi, tag = "chip_rsi")
            }

            // Chart Sizing (چھوٹا / درمیانہ / بڑا) & Zoom Controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Zoom In Button (+)
                Surface(
                    onClick = { zoomCount = (zoomCount - 12).coerceAtLeast(18) },
                    shape = RoundedCornerShape(4.dp),
                    color = SlateDarkBackground,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SlateDarkBorder),
                    modifier = Modifier.size(26.dp).testTag("zoom_in_btn")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("+", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                }

                // Zoom Out Button (-)
                Surface(
                    onClick = { zoomCount = (zoomCount + 15).coerceAtMost(85) },
                    shape = RoundedCornerShape(4.dp),
                    color = SlateDarkBackground,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SlateDarkBorder),
                    modifier = Modifier.size(26.dp).testTag("zoom_out_btn")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("-", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                }

                // Chart Size Mode Switch (چھوٹا / درمیانہ / بڑا)
                Surface(
                    onClick = { chartHeightIndex = (chartHeightIndex + 1) % 3 },
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    modifier = Modifier.height(26.dp).testTag("chart_size_toggle_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.UnfoldMore, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "سائز: ${chartSizeNames[chartHeightIndex]}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Selected Crosshair Info Bar & Visible Candles adapted to zoom
        val visibleCandles = candles.takeLast(zoomCount)
        val selectedCandleIndex = remember(touchX, visibleCandles.size) {
            touchX?.let { x ->
                val count = visibleCandles.size
                if (count > 0) {
                    val idx = ((x / 340f) * count).toInt() // approximation, dynamically refined in canvas
                    idx.coerceIn(0, count - 1)
                } else null
            }
        }

        val activeInspectCandle = if (selectedCandleIndex != null && selectedCandleIndex in visibleCandles.indices) {
            visibleCandles[selectedCandleIndex]
        } else {
            visibleCandles.lastOrNull()
        }

        activeInspectCandle?.let { c ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SlateDarkBackground, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("O: ${formatPrice(c.open, pair.decimals)}", fontSize = 10.sp, color = TextSecondary)
                Text("H: ${formatPrice(c.high, pair.decimals)}", fontSize = 10.sp, color = BullishGreen)
                Text("L: ${formatPrice(c.low, pair.decimals)}", fontSize = 10.sp, color = BearishRed)
                Text("C: ${formatPrice(c.close, pair.decimals)}", fontSize = 10.sp, color = if (c.close >= c.open) BullishGreen else BearishRed, fontWeight = FontWeight.Bold)
                if (showRsi) {
                    Text("RSI: ${c.rsi.toInt()}", fontSize = 10.sp, color = IndicatorRSI)
                }
            }
        }

        // Compute live Bid, Ask, Min, Max for side axis price tags and MT5 Buy/Sell options
        val latestCandle = visibleCandles.lastOrNull()
        val askSpread = if (pair.isGold) 0.25 else 0.00012
        val liveBidPrice = latestCandle?.close ?: pair.initialPrice
        val liveAskPrice = liveBidPrice + askSpread

        val minPrice = visibleCandles.minOfOrNull { it.low } ?: (liveBidPrice * 0.99)
        val maxPrice = visibleCandles.maxOfOrNull { it.high } ?: (liveBidPrice * 1.01)
        val midPrice = (minPrice + maxPrice) / 2.0

        // MT5 One-Click Quick Buy & Sell Trading Panel (Hr chart K sath buy or sell ka option)
        var quickLotSize by remember { mutableDoubleStateOf(0.10) }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Quick SELL Button (Short)
            Surface(
                onClick = { onQuickSell(quickLotSize) },
                color = BearishRed,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1.1f)
                    .height(42.dp)
                    .testTag("chart_quick_sell_btn")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.ArrowDownward, null, tint = Color.White, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("SELL", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                        Text(formatPrice(liveBidPrice, pair.decimals), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.9f))
                    }
                }
            }

            // Lot Size Selector Box
            Surface(
                color = SlateDarkBackground,
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SlateDarkBorder),
                modifier = Modifier
                    .weight(0.9f)
                    .height(42.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = { quickLotSize = (quickLotSize - 0.05).coerceAtLeast(0.01) },
                        modifier = Modifier.size(28.dp).testTag("chart_lot_minus")
                    ) {
                        Text("-", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    Text(
                        text = "${String.format("%.2f", quickLotSize)} Lot",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(
                        onClick = { quickLotSize = (quickLotSize + 0.05).coerceAtMost(5.0) },
                        modifier = Modifier.size(28.dp).testTag("chart_lot_plus")
                    ) {
                        Text("+", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                }
            }

            // Quick BUY Button (Long)
            Surface(
                onClick = { onQuickBuy(quickLotSize) },
                color = BullishGreen,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1.1f)
                    .height(42.dp)
                    .testTag("chart_quick_buy_btn")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.ArrowUpward, null, tint = Color.Black, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("BUY", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                        Text(formatPrice(liveAskPrice, pair.decimals), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black.copy(alpha = 0.9f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Main Candlestick / Area Chart Canvas with Side Dynamic Price Numbering Scale
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeights[chartHeightIndex])
                .background(SlateDarkBackground, RoundedCornerShape(8.dp))
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                Canvas(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .pointerInput(visibleCandles) {
                            detectDragGestures(
                                onDragStart = { touchX = it.x },
                                onDragEnd = { touchX = null },
                                onDragCancel = { touchX = null },
                                onDrag = { change, _ -> touchX = change.position.x }
                            )
                        }
                ) {
                    if (visibleCandles.isEmpty()) return@Canvas

                val width = size.width
                val height = size.height

                // Compute min and max across visible candles including indicators
                var minP = visibleCandles.minOf { it.low }
                var maxP = visibleCandles.maxOf { it.high }

                if (showBollinger) {
                    val maxBoll = visibleCandles.mapNotNull { if (it.bollUpper > 0) it.bollUpper else null }.maxOrNull()
                    val minBoll = visibleCandles.mapNotNull { if (it.bollLower > 0) it.bollLower else null }.minOrNull()
                    if (maxBoll != null) maxP = max(maxP, maxBoll)
                    if (minBoll != null) minP = min(minP, minBoll)
                }

                val priceRange = if (maxP - minP > 0) maxP - minP else 1.0
                val paddingVertical = height * 0.08f
                val chartHeight = height - (paddingVertical * 2)

                fun priceToY(price: Double): Float {
                    val normalized = (maxP - price) / priceRange
                    return (paddingVertical + (normalized * chartHeight)).toFloat()
                }

                // Draw Grid Lines (4 horizontal price lines)
                val gridColor = SlateDarkBorder.copy(alpha = 0.5f)
                val lineStroke = Stroke(width = 1f)
                for (step in 0..4) {
                    val y = paddingVertical + (step * (chartHeight / 4))
                    drawLine(gridColor, Offset(0f, y), Offset(width, y), strokeWidth = 1f)
                }

                val candleCount = visibleCandles.size
                val stepX = width / candleCount
                val candleBarWidth = max(2f, stepX * 0.65f)

                // Bollinger Bands Ribbon
                if (showBollinger) {
                    val upperPath = Path()
                    val lowerPath = Path()
                    var started = false

                    visibleCandles.forEachIndexed { i, c ->
                        if (c.bollUpper > 0 && c.bollLower > 0) {
                            val x = (i * stepX) + (stepX / 2)
                            val yUpper = priceToY(c.bollUpper)
                            val yLower = priceToY(c.bollLower)

                            if (!started) {
                                upperPath.moveTo(x, yUpper)
                                lowerPath.moveTo(x, yLower)
                                started = true
                            } else {
                                upperPath.lineTo(x, yUpper)
                                lowerPath.lineTo(x, yLower)
                            }
                        }
                    }

                    if (started) {
                        drawPath(upperPath, IndicatorBollinger.copy(alpha = 0.8f), style = Stroke(width = 1.5f))
                        drawPath(lowerPath, IndicatorBollinger.copy(alpha = 0.8f), style = Stroke(width = 1.5f))
                    }
                }

                // EMA 20 & SMA 50
                if (showEma) {
                    val ema20Path = Path()
                    val ema50Path = Path()
                    var ema20Started = false
                    var ema50Started = false

                    visibleCandles.forEachIndexed { i, c ->
                        val x = (i * stepX) + (stepX / 2)
                        if (c.ema20 > 0) {
                            val y = priceToY(c.ema20)
                            if (!ema20Started) { ema20Path.moveTo(x, y); ema20Started = true }
                            else ema20Path.lineTo(x, y)
                        }
                        if (c.ema50 > 0) {
                            val y = priceToY(c.ema50)
                            if (!ema50Started) { ema50Path.moveTo(x, y); ema50Started = true }
                            else ema50Path.lineTo(x, y)
                        }
                    }

                    if (ema20Started) drawPath(ema20Path, IndicatorEMA20, style = Stroke(width = 2f))
                    if (ema50Started) drawPath(ema50Path, IndicatorSMA50, style = Stroke(width = 1.8f))
                }

                // Candlesticks or Area Line
                if (isCandlestickMode) {
                    visibleCandles.forEachIndexed { i, c ->
                        val centerX = (i * stepX) + (stepX / 2)
                        val isBullish = c.close >= c.open
                        val color = if (isBullish) BullishGreen else BearishRed

                        // Wick
                        val highY = priceToY(c.high)
                        val lowY = priceToY(c.low)
                        drawLine(color, Offset(centerX, highY), Offset(centerX, lowY), strokeWidth = 1.5f)

                        // Body
                        val openY = priceToY(c.open)
                        val closeY = priceToY(c.close)
                        val topY = min(openY, closeY)
                        val bodyH = max(2f, kotlin.math.abs(closeY - openY))

                        drawRect(
                            color = color,
                            topLeft = Offset(centerX - (candleBarWidth / 2), topY),
                            size = Size(candleBarWidth, bodyH)
                        )
                    }
                } else {
                    // Area/Line Chart
                    val areaPath = Path()
                    val linePath = Path()
                    visibleCandles.forEachIndexed { i, c ->
                        val x = (i * stepX) + (stepX / 2)
                        val y = priceToY(c.close)
                        if (i == 0) {
                            linePath.moveTo(x, y)
                            areaPath.moveTo(x, height)
                            areaPath.lineTo(x, y)
                        } else {
                            linePath.lineTo(x, y)
                            areaPath.lineTo(x, y)
                        }
                    }
                    val lastX = (visibleCandles.size - 1) * stepX + (stepX / 2)
                    areaPath.lineTo(lastX, height)
                    areaPath.close()

                    drawPath(areaPath, BullishGreen.copy(alpha = 0.15f))
                    drawPath(linePath, BullishGreen, style = Stroke(width = 2.5f))
                }

                // MT5 Live Bid and Ask Horizontal Price Lines & Pulse Beacon
                val latestCandle = visibleCandles.lastOrNull()
                if (latestCandle != null) {
                    val liveBidY = priceToY(latestCandle.close)
                    val askSpread = if (pair.isGold) 0.25 else 0.00012
                    val liveAskY = priceToY(latestCandle.close + askSpread)

                    // MT5 Bid (Selling) Price Line (Red Dashed)
                    drawLine(
                        color = BearishRed.copy(alpha = 0.9f),
                        start = Offset(0f, liveBidY),
                        end = Offset(width, liveBidY),
                        strokeWidth = 1.5f,
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f)
                    )

                    // MT5 Ask (Buying) Price Line (Cyan Accent Dashed)
                    drawLine(
                        color = CyanAccent.copy(alpha = 0.9f),
                        start = Offset(0f, liveAskY),
                        end = Offset(width, liveAskY),
                        strokeWidth = 1.5f,
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f)
                    )

                    // MT5 Live Candle Pulse Beacon Dot at current selling/market price
                    val lastX = (visibleCandles.size - 1) * stepX + (stepX / 2)
                    drawCircle(
                        color = if (latestCandle.close >= latestCandle.open) BullishGreen else BearishRed,
                        radius = 5f,
                        center = Offset(lastX, liveBidY)
                    )
                    drawCircle(
                        color = (if (latestCandle.close >= latestCandle.open) BullishGreen else BearishRed).copy(alpha = 0.35f),
                        radius = 9f,
                        center = Offset(lastX, liveBidY)
                    )
                }

                    // Crosshair touch tracking
                    touchX?.let { tx ->
                        if (tx in 0f..width) {
                            drawLine(
                                color = Color.White.copy(alpha = 0.5f),
                                start = Offset(tx, 0f),
                                end = Offset(tx, height),
                                strokeWidth = 1f
                            )
                        }
                    }
                }

                // Dynamic Multi-Level Price Numbering Scale adapted to timeframe & Live Buy/Sell Tags
                val scaleRange = (maxPrice - minPrice).coerceAtLeast(0.0001)
                val stepPrice = scaleRange / 4.0

                Column(
                    modifier = Modifier
                        .width(78.dp)
                        .fillMaxHeight()
                        .background(SlateDarkSurface)
                        .border(androidx.compose.foundation.BorderStroke(1.dp, SlateDarkBorder))
                        .padding(horizontal = 4.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.End
                ) {
                    // Level 1: Highest Price for this timeframe
                    Text(
                        text = formatPrice(maxPrice, pair.decimals),
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        maxLines = 1
                    )

                    // Level 2: Upper Quarter Price
                    Text(
                        text = formatPrice(maxPrice - (stepPrice * 1.0), pair.decimals),
                        fontSize = 8.sp,
                        color = TextSecondary.copy(alpha = 0.8f),
                        maxLines = 1
                    )

                    // LIVE BUYING & SELLING (BID/ASK) PRICE BADGES (where orders are executing)
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // BUY (ASK) Live Price Pill
                        Surface(
                            color = CyanAccent,
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 2.dp, horizontal = 2.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "BUY (خرید)",
                                    fontSize = 7.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.Black
                                )
                                Text(
                                    text = formatPrice(liveAskPrice, pair.decimals),
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    maxLines = 1
                                )
                            }
                        }

                        // SELL (BID) Live Price Pill
                        Surface(
                            color = BearishRed,
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 2.dp, horizontal = 2.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "SELL (فروخت)",
                                    fontSize = 7.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Text(
                                    text = formatPrice(liveBidPrice, pair.decimals),
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1
                                )
                            }
                        }

                        // Live Spread Indicator Tag
                        Text(
                            text = "Sprd: ${if (pair.isGold) "25¢" else "1.2p"}",
                            fontSize = 7.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GoldPrimary,
                            maxLines = 1
                        )
                    }

                    // Level 3: Lower Quarter Price
                    Text(
                        text = formatPrice(minPrice + (stepPrice * 1.0), pair.decimals),
                        fontSize = 8.sp,
                        color = TextSecondary.copy(alpha = 0.8f),
                        maxLines = 1
                    )

                    // Level 4: Lowest Price for this timeframe
                    Text(
                        text = formatPrice(minPrice, pair.decimals),
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        maxLines = 1
                    )
                }
            }
        }

        // Sub-chart: RSI (14) Indicator
        if (showRsi) {
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp)
                    .background(SlateDarkBackground, RoundedCornerShape(6.dp))
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // 70 and 30 guide lines
                    val y70 = h * 0.3f
                    val y30 = h * 0.7f
                    drawLine(Color(0xFFE2E8F0).copy(alpha = 0.25f), Offset(0f, y70), Offset(w, y70), strokeWidth = 1f)
                    drawLine(Color(0xFFE2E8F0).copy(alpha = 0.25f), Offset(0f, y30), Offset(w, y30), strokeWidth = 1f)

                    if (visibleCandles.isNotEmpty()) {
                        val stepX = w / visibleCandles.size
                        val rsiPath = Path()
                        var started = false

                        visibleCandles.forEachIndexed { i, c ->
                            val x = (i * stepX) + (stepX / 2)
                            val normalized = (100.0 - c.rsi) / 100.0
                            val y = (normalized * h).toFloat()
                            if (!started) {
                                rsiPath.moveTo(x, y)
                                started = true
                            } else {
                                rsiPath.lineTo(x, y)
                            }
                        }
                        if (started) {
                            drawPath(rsiPath, IndicatorRSI, style = Stroke(width = 1.8f))
                        }
                    }
                }

                // RSI label
                Text(
                    text = "RSI (14) Overbought 70 / Oversold 30",
                    fontSize = 9.sp,
                    color = TextMuted,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 6.dp, top = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun IndicatorFilterChip(
    label: String,
    selected: Boolean,
    color: Color,
    onClick: () -> Unit,
    tag: String
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(color, RoundedCornerShape(3.dp))
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(label, fontSize = 11.sp, color = if (selected) Color.White else TextMuted)
            }
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = SlateDarkBorder,
            containerColor = SlateDarkBackground
        ),
        modifier = Modifier
            .height(28.dp)
            .testTag(tag)
    )
}

private fun formatPrice(price: Double, decimals: Int): String {
    return String.format("%.${decimals}f", price)
}
