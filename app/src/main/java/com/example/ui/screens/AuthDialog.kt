package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.VerifiedUser
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
fun AuthDialog(
    viewModel: TradingViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var isSignUpMode by remember { mutableStateOf(false) }
    var isVerifyingOtp by remember { mutableStateOf(false) }

    val currentUser by viewModel.authRepository.currentUser.collectAsStateWithLifecycle()
    val pendingOtp by viewModel.authRepository.pendingOtp.collectAsStateWithLifecycle()
    val authMessage by viewModel.authRepository.authMessage.collectAsStateWithLifecycle()

    // Complete Sign Up Form Fields
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var cnic by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var referralCodeInput by remember { mutableStateOf("") }

    // OTP field
    var otpInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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
                            .background(GoldPrimary.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isVerifyingOtp) Icons.Default.MarkEmailRead else if (isSignUpMode) Icons.Default.Person else Icons.Default.Lock,
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (isVerifyingOtp) "ای میل تصدیق (Email OTP)" else if (isSignUpMode) "نیا اکاؤنٹ بنائیں (Complete Sign Up)" else "سائن ان کریں (Sign In)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = if (isVerifyingOtp) "Verify 6-digit Code" else if (isSignUpMode) "مکمل ذاتی ڈیٹا درج کریں" else "Welcome Back Trader",
                            fontSize = 10.sp,
                            color = TextMuted
                        )
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
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Switch Tabs between Sign In and Sign Up
                if (!isVerifyingOtp) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SlateDarkBackground, RoundedCornerShape(8.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            onClick = { isSignUpMode = false; errorMessage = null },
                            shape = RoundedCornerShape(6.dp),
                            color = if (!isSignUpMode) MaterialTheme.colorScheme.primary else Color.Transparent,
                            modifier = Modifier.weight(1f).height(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("سائن ان (Sign In)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (!isSignUpMode) Color.Black else TextSecondary)
                            }
                        }

                        Surface(
                            onClick = { isSignUpMode = true; errorMessage = null },
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSignUpMode) MaterialTheme.colorScheme.primary else Color.Transparent,
                            modifier = Modifier.weight(1f).height(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("رجسٹریشن (Sign Up)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isSignUpMode) Color.Black else TextSecondary)
                            }
                        }
                    }
                }

                // If in Email Verification Mode
                if (isVerifyingOtp) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SlateDarkBackground, RoundedCornerShape(8.dp))
                            .border(1.dp, CyanAccent, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Text("ای میل تصدیق (Email Verification):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyanAccent)
                            Text("آپ کی ای میل ($email) پر 6 ہندسوں کا ویریفکیشن کوڈ بھیجا گیا ہے۔", fontSize = 11.sp, color = TextSecondary, modifier = Modifier.padding(top = 2.dp))
                            Text("ٹیسٹنگ کوڈ: $pendingOtp", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = GoldPrimary, modifier = Modifier.padding(top = 4.dp))
                        }
                    }

                    OutlinedTextField(
                        value = otpInput,
                        onValueChange = { if (it.length <= 6) otpInput = it },
                        label = { Text("6-Digit OTP Code") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary),
                        modifier = Modifier.fillMaxWidth().testTag("input_otp_code")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = {
                            val code = viewModel.authRepository.resendVerificationEmail()
                            Toast.makeText(context, "نیا کوڈ بھیج دیا گیا: $code", Toast.LENGTH_SHORT).show()
                        }) {
                            Text("کوڈ دوبارہ بھیجیں (Resend)", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                        }

                        Button(
                            onClick = {
                                val success = viewModel.authRepository.verifyEmail(otpInput)
                                if (success) {
                                    Toast.makeText(context, "ای میل کی تصدیق کامیاب ہو گئی!", Toast.LENGTH_LONG).show()
                                    isVerifyingOtp = false
                                    onDismiss()
                                } else {
                                    errorMessage = "غلط تصدیقی کوڈ درج کیا گیا ہے۔"
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BullishGreen),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.testTag("btn_verify_otp")
                        ) {
                            Text("تصدیق کریں", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                } else if (isSignUpMode) {
                    // Complete Sign Up Form
                    Text("مکمل ذاتی ڈیٹا (Complete Trader Data):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GoldPrimary)

                    // 1. Full Name
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("پورا نام (Full Name)") },
                        leadingIcon = { Icon(Icons.Default.Person, null, tint = TextMuted, modifier = Modifier.size(16.dp)) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary),
                        modifier = Modifier.fillMaxWidth().testTag("input_signup_name")
                    )

                    // 2. Email Address
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("ای میل ایڈریس (Email Address)") },
                        leadingIcon = { Icon(Icons.Default.Email, null, tint = TextMuted, modifier = Modifier.size(16.dp)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary),
                        modifier = Modifier.fillMaxWidth().testTag("input_signup_email")
                    )

                    // 3. Mobile Phone Number
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("موبائل فون نمبر (Mobile / WhatsApp)") },
                        placeholder = { Text("03258097930", color = TextMuted) },
                        leadingIcon = { Icon(Icons.Default.Phone, null, tint = TextMuted, modifier = Modifier.size(16.dp)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary),
                        modifier = Modifier.fillMaxWidth().testTag("input_signup_phone")
                    )

                    // 4. City
                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text("شہر / ضلع (City)") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, null, tint = TextMuted, modifier = Modifier.size(16.dp)) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary),
                        modifier = Modifier.fillMaxWidth().testTag("input_signup_city")
                    )

                    // 5. CNIC / ID
                    OutlinedTextField(
                        value = cnic,
                        onValueChange = { cnic = it },
                        label = { Text("شناختی کارڈ نمبر (CNIC Number)") },
                        placeholder = { Text("35201-XXXXXXX-X", color = TextMuted) },
                        leadingIcon = { Icon(Icons.Default.Badge, null, tint = TextMuted, modifier = Modifier.size(16.dp)) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary),
                        modifier = Modifier.fillMaxWidth().testTag("input_signup_cnic")
                    )

                    // 6. Password
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("پاس ورڈ (Password)") },
                        leadingIcon = { Icon(Icons.Default.Lock, null, tint = TextMuted, modifier = Modifier.size(16.dp)) },
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary),
                        modifier = Modifier.fillMaxWidth().testTag("input_signup_password")
                    )

                    // 7. Referral Code
                    OutlinedTextField(
                        value = referralCodeInput,
                        onValueChange = { referralCodeInput = it },
                        label = { Text("ریفرل کوڈ (اختیاری Referral Code)") },
                        placeholder = { Text("AUX-74892", color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary),
                        modifier = Modifier.fillMaxWidth().testTag("input_signup_refcode")
                    )
                } else {
                    // Sign In Form
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("ای میل ایڈریس (Email Address)") },
                        leadingIcon = { Icon(Icons.Default.Email, null, tint = TextMuted, modifier = Modifier.size(16.dp)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary),
                        modifier = Modifier.fillMaxWidth().testTag("input_signin_email")
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("پاس ورڈ (Password)") },
                        leadingIcon = { Icon(Icons.Default.Lock, null, tint = TextMuted, modifier = Modifier.size(16.dp)) },
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary),
                        modifier = Modifier.fillMaxWidth().testTag("input_signin_password")
                    )
                }

                // Error message
                errorMessage?.let { err ->
                    Text(err, color = BearishRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            if (!isVerifyingOtp) {
                Button(
                    onClick = {
                        if (isSignUpMode) {
                            val success = viewModel.authRepository.signUp(
                                name = fullName,
                                email = email,
                                password = password,
                                phone = phone,
                                city = city,
                                cnic = cnic,
                                refCodeUsed = referralCodeInput
                            )
                            if (success) {
                                isVerifyingOtp = true
                                errorMessage = null
                            } else {
                                errorMessage = "براہ کرم تمام ضروری فیلڈز درست درج کریں۔"
                            }
                        } else {
                            val success = viewModel.authRepository.signIn(email, password)
                            if (success) {
                                Toast.makeText(context, "سائن ان کامیاب ہو گیا!", Toast.LENGTH_SHORT).show()
                                onDismiss()
                            } else {
                                errorMessage = "ای میل یا پاس ورڈ غلط ہے۔"
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("auth_action_submit_btn")
                ) {
                    Text(
                        text = if (isSignUpMode) "رجسٹر کریں اور ای میل تصدیق کریں" else "سائن ان کریں",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("منسوخ", color = TextSecondary)
            }
        },
        containerColor = SlateDarkCard,
        shape = RoundedCornerShape(14.dp)
    )
}
