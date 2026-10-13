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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.BotStrategyType
import com.example.model.TradingPair
import com.example.model.UrduExpertPost
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
import com.example.urdu.UrduForexExpertRepository

@Composable
fun UrduExpertsScreen(
    viewModel: TradingViewModel,
    modifier: Modifier = Modifier
) {
    val dynamicAnalysis by viewModel.dynamicUrduAnalysis.collectAsStateWithLifecycle()
    val selectedPair by viewModel.selectedPair.collectAsStateWithLifecycle()
    var appliedPostId by remember { mutableStateOf<String?>(null) }
    var showGlossary by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SlateDarkBackground)
            .padding(horizontal = 14.dp)
            .testTag("urdu_experts_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Header with Urdu Callout
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateDarkCard),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SlateDarkBorder),
                modifier = Modifier.fillMaxWidth().testTag("urdu_header_card")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Psychology, "Experts", tint = GoldPrimary, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "ماہرینِ فاریکس کے تجارتی تجزیات",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Forex & Gold Expert Views in Urdu",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                        }

                        Surface(
                            onClick = { showGlossary = !showGlossary },
                            shape = RoundedCornerShape(6.dp),
                            color = SlateDarkBackground,
                            border = androidx.compose.foundation.BorderStroke(1.dp, SlateDarkBorder)
                        ) {
                            Text(
                                text = if (showGlossary) "تجزیات دیکھیں" else "فاریکس گائیڈ",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }
        }

        // 2. Dynamic Live Urdu Analysis Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateDarkCard),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyanAccent.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth().testTag("dynamic_urdu_card")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, "Live AI", tint = CyanAccent, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("لائیو مارکیٹ اور تکنیکی جائزہ", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CyanAccent)
                        }
                        Surface(
                            color = CyanAccent.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(selectedPair.symbol, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CyanAccent, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = dynamicAnalysis.ifEmpty { "مارکیٹ کا ڈیٹا اور تجزیہ لوڈ ہو رہا ہے..." },
                        fontSize = 12.sp,
                        lineHeight = 19.sp,
                        color = TextPrimary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SlateDarkBackground, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    )
                }
            }
        }

        // 3. Glossary View or Feed of Expert Posts
        if (showGlossary) {
            item {
                Text("بنیادی فاریکس اصطلاحات اور رہنمائی:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
            items(UrduForexExpertRepository.URDU_GLOSSARY) { (title, desc) ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = SlateDarkCard),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SlateDarkBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = GoldPrimary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(desc, fontSize = 12.sp, lineHeight = 18.sp, color = TextSecondary)
                    }
                }
            }
        } else {
            item {
                Text("ماہرین کے تازہ ترین ٹریڈنگ سگنلز اور ویوز:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }

            items(UrduForexExpertRepository.EXPERT_POSTS, key = { it.id }) { post ->
                ExpertPostCard(
                    post = post,
                    isApplied = appliedPostId == post.id,
                    onApplyToBot = {
                        appliedPostId = post.id
                        val pair = TradingPair.find(post.pair)
                        val pipMultiplier = if (pair.isGold) 1.0 else 10000.0
                        val tpPips = kotlin.math.abs(post.takeProfit - post.entryPrice) * pipMultiplier
                        val slPips = kotlin.math.abs(post.entryPrice - post.stopLoss) * pipMultiplier

                        viewModel.updateBotConfig(
                            viewModel.repository.botConfig.value.copy(
                                enabled = true,
                                symbol = post.pair,
                                strategy = if (post.isBullish) BotStrategyType.EMA_CROSSOVER else BotStrategyType.RSI_REVERSAL,
                                takeProfitPips = tpPips.coerceIn(20.0, 150.0),
                                stopLossPips = slPips.coerceIn(15.0, 80.0)
                            )
                        )
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun ExpertPostCard(
    post: UrduExpertPost,
    isApplied: Boolean,
    onApplyToBot: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SlateDarkCard),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SlateDarkBorder),
        modifier = Modifier.fillMaxWidth().testTag("expert_post_${post.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Analyst Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(GoldPrimary.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(post.expertNameUrdu.take(1), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = GoldPrimary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(post.expertNameUrdu, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.Verified, "Verified", tint = CyanAccent, modifier = Modifier.size(14.dp))
                        }
                        Text(post.expertRankUrdu, fontSize = 10.sp, color = TextMuted)
                    }
                }

                Surface(
                    color = SlateDarkBackground,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("${post.winRatePercent}% درستگی", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BullishGreen, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Pair & Action Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${post.pair} (${post.timeframe})",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )

                Surface(
                    color = if (post.isBullish) BullishGreen.copy(alpha = 0.2f) else BearishRed.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (post.isBullish) BullishGreen else BearishRed)
                ) {
                    Text(
                        text = post.actionUrdu,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (post.isBullish) BullishGreen else BearishRed,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title
            Text(
                text = post.titleUrdu,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            // In-depth Analysis Body
            Text(
                text = post.detailedAnalysisUrdu,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Key Price Levels Grid
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SlateDarkBackground, RoundedCornerShape(8.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("انٹری پرائس", fontSize = 9.sp, color = TextMuted)
                    Text("${post.entryPrice}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("ٹیک پرافٹ (TP)", fontSize = 9.sp, color = BullishGreen)
                    Text("${post.takeProfit}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BullishGreen)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("اسٹاپ لاس (SL)", fontSize = 9.sp, color = BearishRed)
                    Text("${post.stopLoss}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BearishRed)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Apply to Bot Action Button
            Button(
                onClick = onApplyToBot,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isApplied) BullishGreen else MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().height(40.dp)
            ) {
                if (isApplied) {
                    Icon(Icons.Default.CheckCircle, null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("بوٹ پر سگنل فعال ہو گیا", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                } else {
                    Icon(Icons.Default.PlayArrow, null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("یہ تجزیہ اور سیٹنگز خودکار بوٹ پر لگائیں", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }
            }
        }
    }
}
