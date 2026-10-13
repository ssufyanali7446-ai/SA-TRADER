package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MarketTick
import com.example.model.TradingPair
import com.example.ui.theme.BearishRed
import com.example.ui.theme.BullishGreen
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.SlateDarkBorder
import com.example.ui.theme.SlateDarkCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun PairSelectorBar(
    pairs: List<TradingPair>,
    selectedPair: TradingPair,
    liveTicks: Map<String, MarketTick>,
    onPairSelected: (TradingPair) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .testTag("pair_selector_row"),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(pairs, key = { it.symbol }) { pair ->
            val tick = liveTicks[pair.symbol]
            val isSelected = pair.symbol == selectedPair.symbol
            val isUp = (tick?.change24h ?: 0.0) >= 0.0

            val borderColor by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.primary else SlateDarkBorder,
                label = "border_color"
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(SlateDarkCard)
                    .border(if (isSelected) 1.5.dp else 1.dp, borderColor, RoundedCornerShape(10.dp))
                    .clickable { onPairSelected(pair) }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .testTag("pair_item_${pair.symbol.replace('/', '_')}")
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = pair.symbol,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        if (pair.isGold) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Gold Asset",
                                tint = GoldPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    val priceStr = if (tick != null) {
                        String.format("%.${pair.decimals}f", tick.price)
                    } else {
                        String.format("%.${pair.decimals}f", pair.initialPrice)
                    }

                    Text(
                        text = priceStr,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else TextSecondary,
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    val changePercent = tick?.changePercent24h ?: 0.0
                    val changeSign = if (changePercent >= 0) "+" else ""
                    Text(
                        text = "$changeSign${String.format("%.2f", changePercent)}%",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isUp) BullishGreen else BearishRed,
                        modifier = Modifier.padding(top = 1.dp)
                    )
                }
            }
        }
    }
}
