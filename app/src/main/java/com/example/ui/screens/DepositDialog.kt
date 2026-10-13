package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.DepositMethod
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
fun DepositDialog(
    viewModel: TradingViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedMethod by remember { mutableStateOf(DepositMethod.EASYPAISA) }
    var amountText by remember { mutableStateOf("50") }
    var senderPhone by remember { mutableStateOf("") }
    var trxId by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    val depositHistory by viewModel.authRepository.depositHistory.collectAsStateWithLifecycle()

    val amountDouble = amountText.toDoubleOrNull() ?: 0.0
    val amountPkr = (amountDouble * 280.0)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(BullishGreen.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AccountBalanceWallet, null, tint = BullishGreen, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("ڈپازٹ فنڈز (EasyPaisa / JazzCash)", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Min: $1 | Max: $500", fontSize = 11.sp, color = GoldPrimary)
                    }
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, "Close", tint = TextMuted, modifier = Modifier.size(18.dp))
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Official Sufyan Ali Account Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SlateDarkBackground, RoundedCornerShape(10.dp))
                        .border(1.5.dp, GoldPrimary, RoundedCornerShape(10.dp))
                        .padding(12.dp)
                        .testTag("deposit_account_info_box")
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "رقم بھیجنے کا تصدیق شدہ اکاؤنٹ:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldPrimary
                            )
                            Surface(
                                color = BullishGreen.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text("OFFICIAL", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = BullishGreen, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "اکاؤنٹ ٹائٹل (Name): Sufyan Ali",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "نمبر: 03258097930",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = CyanAccent
                            )

                            Button(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Deposit Account", "03258097930"))
                                    Toast.makeText(context, "03258097930 کاپی ہو گیا!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SlateDarkCard),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.height(28.dp).testTag("copy_acc_num_btn")
                            ) {
                                Icon(Icons.Default.ContentCopy, null, tint = GoldPrimary, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("Copy", fontSize = 10.sp, color = GoldPrimary)
                            }
                        }
                    }
                }

                // Payment Method Selector (EasyPaisa vs JazzCash)
                Text("ادائیگی کا طریقہ منتخب کریں:", fontSize = 12.sp, color = TextSecondary)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DepositMethod.values().forEach { method ->
                        val isSelected = selectedMethod == method
                        Surface(
                            onClick = { selectedMethod = method },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else SlateDarkBackground,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else SlateDarkBorder),
                            modifier = Modifier.weight(1f).height(38.dp).testTag("method_${method.name.lowercase()}")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.PhoneAndroid, null, tint = if (isSelected) Color.Black else TextSecondary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = method.displayName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.Black else TextPrimary
                                )
                            }
                        }
                    }
                }

                // Quick Amount Presets ($1, $10, $50, $100, $250, $500)
                Text("فوری رقم کے بٹن ($1 سے $500):", fontSize = 12.sp, color = TextSecondary)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("1", "10", "50", "100", "250", "500").forEach { preset ->
                        val isSel = amountText == preset
                        Surface(
                            onClick = {
                                amountText = preset
                                errorMessage = null
                            },
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSel) MaterialTheme.colorScheme.primary else SlateDarkBackground,
                            modifier = Modifier.weight(1f).height(28.dp).testTag("preset_$preset")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "$$preset",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) Color.Black else TextPrimary
                                )
                            }
                        }
                    }
                }

                // Amount Input Field (USD) with PKR Live Preview
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it.filter { char -> char.isDigit() || char == '.' }
                        errorMessage = null
                    },
                    label = { Text("ڈپازٹ رقم (USD $)") },
                    supportingText = {
                        Text(
                            text = "= تقریباً Rs ${String.format("%,.0f", amountPkr)} روپے (1$ = 280 Rs)",
                            fontSize = 11.sp,
                            color = BullishGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    isError = amountDouble !in 1.0..500.0 && amountText.isNotEmpty(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = SlateDarkBorder
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("input_deposit_amount")
                )

                // Sender Phone Number Field
                OutlinedTextField(
                    value = senderPhone,
                    onValueChange = { senderPhone = it },
                    label = { Text("بھیجنے والے کا فون نمبر (Sender Number)") },
                    placeholder = { Text("03XX-XXXXXXX", color = TextMuted) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = SlateDarkBorder
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("input_sender_phone")
                )

                // Transaction ID (TID)
                OutlinedTextField(
                    value = trxId,
                    onValueChange = { trxId = it },
                    label = { Text("ٹرانزیکشن آئی ڈی (TID / Trx ID)") },
                    placeholder = { Text("ایزی پیسہ / جاز کیش کا SMS کوڈ", color = TextMuted) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = SlateDarkBorder
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("input_trx_id")
                )

                // Error Message if any
                errorMessage?.let { err ->
                    Text(err, color = BearishRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Success Message
                successMessage?.let { msg ->
                    Text(msg, color = BullishGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    if (amount == null || amount < 1.0 || amount > 500.0) {
                        errorMessage = "ڈپازٹ رقم 1$ سے 500$ کے درمیان ہونی چاہیے۔"
                        return@Button
                    }
                    if (senderPhone.isBlank()) {
                        errorMessage = "براہ کرم اپنا ایزی پیسہ / جاز کیش نمبر درج کریں۔"
                        return@Button
                    }

                    val result = viewModel.deposit(
                        amountUsd = amount,
                        method = selectedMethod,
                        senderPhone = senderPhone,
                        trxId = trxId
                    )

                    result.onSuccess {
                        successMessage = "کامیابی! $${amount.toInt()} اکاؤنٹ میں کریڈٹ ہو گئے اور 10% ریفرل کمیشن ریکارڈ ہو گیا!"
                        Toast.makeText(context, "$${amount.toInt()} کامیابی سے جمع ہو گئے!", Toast.LENGTH_LONG).show()
                        onDismiss()
                    }.onFailure { ex ->
                        errorMessage = ex.message
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("submit_deposit_btn")
            ) {
                Text("ڈپازٹ تصدیق کریں (Confirm)", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("بند کریں", color = TextSecondary)
            }
        },
        containerColor = SlateDarkCard,
        shape = RoundedCornerShape(14.dp)
    )
}
