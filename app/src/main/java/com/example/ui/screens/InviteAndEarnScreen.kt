package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.ReferralFriendEntry
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

@Composable
fun InviteAndEarnScreen(
    viewModel: TradingViewModel,
    onOpenDeposit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val referralStats by viewModel.authRepository.referralStats.collectAsStateWithLifecycle()
    val currentUser by viewModel.authRepository.currentUser.collectAsStateWithLifecycle()

    var showSimulateFriendDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SlateDarkBackground)
            .padding(horizontal = 14.dp)
            .testTag("invite_and_earn_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Mobile-friendly Top Banner: 10% Commission Callout
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateDarkCard),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, GoldPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
                    .testTag("invite_banner_card")
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
                                    .size(38.dp)
                                    .background(GoldPrimary.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CardGiftcard,
                                    contentDescription = "Invite Program",
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "انوائٹ پروگرام: 10% کمیشن",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Earn 10% Lifetime Commission",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                        }

                        Surface(
                            color = GoldPrimary,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "10% CASH",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.Black,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "اپنے دوستوں کو انوائٹ کریں! جب بھی آپ کا ریفرل 1$ سے 500$ کے درمیان ڈپازٹ کرے گا، آپ کو فوری طور پر اس کے ڈپازٹ کا 10% نقد کمیشن ملے گا!",
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        // 2. Unique Personalized Invite Link & Code Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateDarkCard),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SlateDarkBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
                    .testTag("unique_invite_link_card")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "آپ کا منفرد انوائٹ لنک (Unique Link)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        currentUser?.let { user ->
                            Surface(
                                color = if (user.isEmailVerified) BullishGreen.copy(alpha = 0.15f) else BearishRed.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = if (user.isEmailVerified) Icons.Default.Verified else Icons.Default.PersonAdd,
                                        contentDescription = null,
                                        tint = if (user.isEmailVerified) BullishGreen else BearishRed,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = if (user.isEmailVerified) "Verified" else "Unverified",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (user.isEmailVerified) BullishGreen else BearishRed
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Link Display with 1-Tap Copy Box
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SlateDarkBackground, RoundedCornerShape(8.dp))
                            .border(1.dp, SlateDarkBorder, RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = referralStats.inviteLink,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = CyanAccent,
                                maxLines = 1
                            )
                            Text(
                                text = "ریفرل کوڈ: ${referralStats.referralCode}",
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                        }

                        Button(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("AUX Trader Invite Link", referralStats.inviteLink)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "انوائٹ لنک کاپی ہو گیا! (Link Copied)", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("copy_invite_link_btn")
                        ) {
                            Icon(Icons.Default.ContentCopy, null, tint = Color.Black, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("کاپی", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Share Buttons: WhatsApp and Native Share
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // WhatsApp Share
                        Button(
                            onClick = {
                                val shareText = "🔥 سونے اور فاریکس کی آٹومیٹک ٹریڈنگ بوٹ ایپ ڈاؤن لوڈ کریں اور منافع کمائیں!\nمیرے لنک سے جوائن کریں:\n${referralStats.inviteLink}\nریفرل کوڈ: ${referralStats.referralCode}"
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                    setPackage("com.whatsapp")
                                }
                                try {
                                    context.startActivity(intent)
                                } catch (_: Exception) {
                                    // Fallback to chooser
                                    val chooser = Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, shareText)
                                    }, "Share Invite Link")
                                    context.startActivity(chooser)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .testTag("share_whatsapp_btn")
                        ) {
                            Icon(Icons.Default.Share, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("WhatsApp پر شیئر", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        // System Share
                        OutlinedButton(
                            onClick = {
                                val shareText = "Join AUX Trader Gold & Forex Bot:\n${referralStats.inviteLink}\nReferral Code: ${referralStats.referralCode}"
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                }
                                context.startActivity(Intent.createChooser(intent, "Share Invite Link"))
                            },
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SlateDarkBorder),
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .testTag("share_other_btn")
                        ) {
                            Text("دیگر ایپس (Share)", fontSize = 12.sp, color = TextPrimary)
                        }
                    }
                }
            }
        }

        // 3. Referral Earnings Summary Card & 10% Claim Action
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateDarkCard),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SlateDarkBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
                    .testTag("referral_earnings_card")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "آپ کی کمائی کا خلاصہ (Referral Earnings)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Total Invited
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(SlateDarkBackground, RoundedCornerShape(8.dp))
                                .border(1.dp, SlateDarkBorder, RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Column {
                                Text("کل دوست", fontSize = 10.sp, color = TextMuted)
                                Text(
                                    text = "${referralStats.totalFriendsInvited}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TextPrimary,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }

                        // Total Deposits
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(SlateDarkBackground, RoundedCornerShape(8.dp))
                                .border(1.dp, SlateDarkBorder, RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Column {
                                Text("دوستوں کے ڈپازٹس", fontSize = 10.sp, color = TextMuted)
                                Text(
                                    text = "$${String.format("%,.0f", referralStats.totalDepositVolume)}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = CyanAccent,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }

                        // 10% Commission Earned
                        Box(
                            modifier = Modifier
                                .weight(1.2f)
                                .background(SlateDarkBackground, RoundedCornerShape(8.dp))
                                .border(1.dp, GoldPrimary, RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Column {
                                Text("10% کل کمیشن", fontSize = 10.sp, color = GoldPrimary)
                                Text(
                                    text = "+$${String.format("%.2f", referralStats.totalCommissionEarned)}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = BullishGreen,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Claim Commission Button
                    val unclaimed = referralStats.unclaimedCommission
                    Button(
                        onClick = {
                            val claimed = viewModel.claimReferralCommission()
                            if (claimed > 0) {
                                Toast.makeText(context, "$${String.format("%.2f", claimed)} ٹریڈنگ بیلنس میں ٹرانسفر ہو گئے!", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "فی الوقت کوئی کلیم کے لیے کمیشن موجود نہیں۔", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (unclaimed > 0) BullishGreen else SlateDarkBorder
                        ),
                        shape = RoundedCornerShape(8.dp),
                        enabled = unclaimed > 0,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("claim_commission_btn")
                    ) {
                        Icon(Icons.Default.MonetizationOn, null, tint = if (unclaimed > 0) Color.White else TextMuted, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (unclaimed > 0) "کلیم کریں: $${String.format("%.2f", unclaimed)} ٹریڈنگ بیلنس میں شامل کریں" else "تمام کمیشن کلیم ہو چکا ہے",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (unclaimed > 0) Color.White else TextMuted
                        )
                    }
                }
            }
        }

        // 4. Deposit Policy Information Box ($1 to $500)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateDarkCard),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SlateDarkBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ڈپازٹ پالیسی (Deposit Rules)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Button(
                            onClick = onOpenDeposit,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(30.dp).testTag("quick_deposit_btn")
                        ) {
                            Text("خود ڈپازٹ کریں", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text("• کم از کم ڈپازٹ رقم: 1$ (Minimum $1)", fontSize = 11.sp, color = TextSecondary)
                    Text("• زیادہ سے زیادہ ڈپازٹ رقم: 500$ (Maximum $500)", fontSize = 11.sp, color = TextSecondary)
                    Text("• ہر ڈپازٹ پر انوائٹ کرنے والے کو فوری 10% کمیشن کریڈٹ ہو گا۔", fontSize = 11.sp, color = BullishGreen)
                }
            }
        }

        // 5. Friends Joined History List
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateDarkCard),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SlateDarkBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
                    .testTag("referrals_list_card")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "آپ کے ریفرلز کی فہرست (${referralStats.friendsList.size})",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        // Quick Test Button
                        Text(
                            text = "+ فرضی دوست ٹیسٹ",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable {
                                    val randomDeposit = listOf(10.0, 50.0, 100.0, 200.0, 500.0).random()
                                    viewModel.authRepository.simulateFriendDeposit("Noman Trader", randomDeposit)
                                    Toast.makeText(context, "نیا دوست جوائن ہوا اور $randomDeposit$ ڈپازٹ کیا! (10% کمیشن مل گیا)", Toast.LENGTH_SHORT).show()
                                }
                                .padding(4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    referralStats.friendsList.forEach { friend ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .background(SlateDarkBackground, RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(friend.friendName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Default.CheckCircle, null, tint = BullishGreen, modifier = Modifier.size(12.dp))
                                }
                                Text("ڈپازٹ رقم: $${friend.depositAmount.toInt()} • ${friend.joinedDate}", fontSize = 10.sp, color = TextMuted)
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "+$${String.format("%.2f", friend.commission10Percent)}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = BullishGreen
                                )
                                Text("10% کمیشن", fontSize = 9.sp, color = TextSecondary)
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
