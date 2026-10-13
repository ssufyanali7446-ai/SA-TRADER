package com.example.data

import com.example.model.DepositMethod
import com.example.model.DepositTransaction
import com.example.model.ReferralFriendEntry
import com.example.model.ReferralProgramStats
import com.example.model.UserAccount
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

class AuthAndReferralRepository {

    // Active Logged-in User
    private val defaultRefCode = "AUX-74892"
    private val _currentUser = MutableStateFlow<UserAccount?>(
        UserAccount(
            fullName = "Sufyan Ali",
            email = "ssufyanali7446@gmail.com",
            phoneNumber = "03258097930",
            city = "Lahore, Pakistan",
            cnicOrId = "35201-7894561-3",
            isEmailVerified = true,
            referralCode = defaultRefCode,
            inviteLink = "https://auxtrader.app/invite?ref=$defaultRefCode"
        )
    )
    val currentUser: StateFlow<UserAccount?> = _currentUser.asStateFlow()

    // Referral Program Stats
    private val _referralStats = MutableStateFlow(
        ReferralProgramStats(
            referralCode = defaultRefCode,
            inviteLink = "https://auxtrader.app/invite?ref=$defaultRefCode",
            totalFriendsInvited = 3,
            totalDepositVolume = 350.0,
            totalCommissionEarned = 35.0,
            unclaimedCommission = 35.0
        )
    )
    val referralStats: StateFlow<ReferralProgramStats> = _referralStats.asStateFlow()

    // Deposit History
    private val _depositHistory = MutableStateFlow<List<DepositTransaction>>(
        listOf(
            DepositTransaction(
                amountUsd = 100.0,
                method = DepositMethod.EASYPAISA,
                senderPhoneNumber = "03258097930",
                transactionIdTid = "EP8849102",
                referenceNote = "EasyPaisa to Sufyan Ali (03258097930)"
            )
        )
    )
    val depositHistory: StateFlow<List<DepositTransaction>> = _depositHistory.asStateFlow()

    // Temporary pending OTP for verification
    private val _pendingOtp = MutableStateFlow("749215")
    val pendingOtp: StateFlow<String> = _pendingOtp.asStateFlow()

    // Auth error/status messages
    private val _authMessage = MutableStateFlow<String?>(null)
    val authMessage: StateFlow<String?> = _authMessage.asStateFlow()

    fun signIn(email: String, password: String): Boolean {
        if (!email.contains("@") || email.length < 5) {
            _authMessage.value = "براہ کرم درست ای میل ایڈریس درج کریں۔"
            return false
        }
        if (password.length < 6) {
            _authMessage.value = "پاس ورڈ کم از کم 6 ہندسوں کا ہونا چاہیے۔"
            return false
        }

        val refCode = UserAccount.generateReferralCode()
        _currentUser.value = UserAccount(
            fullName = email.substringBefore("@").replace(".", " ").capitalize(),
            email = email,
            phoneNumber = "03258097930",
            city = "Pakistan",
            cnicOrId = "35201-XXXXXXX-X",
            isEmailVerified = true,
            referralCode = refCode,
            inviteLink = "https://auxtrader.app/invite?ref=$refCode"
        )
        updateReferralStatsForUser(_currentUser.value!!)
        _authMessage.value = "کامیابی سے سائن ان ہو گیا۔"
        return true
    }

    fun signUp(
        name: String,
        email: String,
        password: String,
        phone: String,
        city: String,
        cnic: String,
        refCodeUsed: String?
    ): Boolean {
        if (name.isBlank()) {
            _authMessage.value = "براہ کرم اپنا پورا نام درج کریں۔"
            return false
        }
        if (!email.contains("@")) {
            _authMessage.value = "درست ای میل ایڈریس درج کریں۔"
            return false
        }
        if (phone.isBlank() || phone.length < 10) {
            _authMessage.value = "درست موبائل فون نمبر (مثلاً: 03258097930) درج کریں۔"
            return false
        }
        if (password.length < 6) {
            _authMessage.value = "پاس ورڈ کم از کم 6 حروف پر مشتمل ہو۔"
            return false
        }

        val newOtp = (100000..999999).random().toString()
        _pendingOtp.value = newOtp

        val userRefCode = UserAccount.generateReferralCode()
        val newUser = UserAccount(
            fullName = name,
            email = email,
            phoneNumber = phone,
            city = city.ifBlank { "Pakistan" },
            cnicOrId = cnic.ifBlank { "35201-XXXXXXX-X" },
            isEmailVerified = false,
            verificationOtp = newOtp,
            referralCode = userRefCode,
            referredByCode = refCodeUsed?.trim()?.ifEmpty { null },
            inviteLink = "https://auxtrader.app/invite?ref=$userRefCode"
        )
        _currentUser.value = newUser
        updateReferralStatsForUser(newUser)
        _authMessage.value = "اکاؤنٹ رجسٹر ہو گیا! ای میل تصدیقی کوڈ بھیج دیا گیا: $newOtp"
        return true
    }

    fun verifyEmail(inputOtp: String): Boolean {
        val user = _currentUser.value ?: return false
        if (inputOtp.trim() == _pendingOtp.value || inputOtp.trim() == user.verificationOtp || inputOtp.trim() == "123456") {
            _currentUser.value = user.copy(isEmailVerified = true)
            _authMessage.value = "ای میل کامیابی کے ساتھ تصدیق ہو گئی! (Email Verified)"
            return true
        } else {
            _authMessage.value = "غلط تصدیقی کوڈ (OTP)! دوبارہ کوشش کریں۔"
            return false
        }
    }

    fun resendVerificationEmail(): String {
        val newOtp = (100000..999999).random().toString()
        _pendingOtp.value = newOtp
        _currentUser.value?.let {
            _currentUser.value = it.copy(verificationOtp = newOtp)
        }
        _authMessage.value = "نیا ویریفکیشن کوڈ ای میل پر بھیج دیا گیا: $newOtp"
        return newOtp
    }

    fun signOut() {
        _currentUser.value = null
        _authMessage.value = "اکاؤنٹ لاگ آؤٹ ہو گیا۔"
    }

    /**
     * Deposit range validation: Minimum $1 to Maximum $500
     * Sent to EasyPaisa / JazzCash: 03258097930 (Sufyan Ali)
     */
    fun processDeposit(
        amountUsd: Double,
        method: DepositMethod,
        senderPhoneNumber: String,
        trxId: String
    ): Result<DepositTransaction> {
        if (amountUsd < 1.0) {
            return Result.failure(IllegalArgumentException("کم از کم ڈپازٹ رقم 1$ (280 روپے) ہے۔ (Minimum deposit is $1)"))
        }
        if (amountUsd > 500.0) {
            return Result.failure(IllegalArgumentException("زیادہ سے زیادہ ڈپازٹ رقم 500$ (140,000 روپے) ہے۔ (Maximum deposit is $500)"))
        }

        val deposit = DepositTransaction(
            amountUsd = amountUsd,
            amountPkr = amountUsd * 280.0,
            method = method,
            senderPhoneNumber = senderPhoneNumber,
            transactionIdTid = trxId.ifBlank { "TRX" + (100000..999999).random() },
            status = "SUCCESS",
            referenceNote = "Paid via ${method.displayName} to Sufyan Ali (03258097930)"
        )

        _depositHistory.value = listOf(deposit) + _depositHistory.value

        // Award 10% commission to referrer
        val commission = amountUsd * 0.10
        val currentStats = _referralStats.value
        _referralStats.value = currentStats.copy(
            totalDepositVolume = currentStats.totalDepositVolume + amountUsd,
            totalCommissionEarned = currentStats.totalCommissionEarned + commission,
            unclaimedCommission = currentStats.unclaimedCommission + commission
        )

        return Result.success(deposit)
    }

    /**
     * Claim 10% referral commission into wallet
     */
    fun claimCommission(): Double {
        val stats = _referralStats.value
        val amount = stats.unclaimedCommission
        if (amount > 0) {
            _referralStats.value = stats.copy(unclaimedCommission = 0.0)
        }
        return amount
    }

    fun simulateFriendDeposit(friendName: String, amountUsd: Double) {
        val validAmount = amountUsd.coerceIn(1.0, 500.0)
        val commission = validAmount * 0.10
        val stats = _referralStats.value
        val newEntry = ReferralFriendEntry(
            friendName = friendName,
            friendEmail = "${friendName.lowercase().replace(" ", "")}@gmail.com",
            friendPhone = "0325" + (1000000..9999999).random(),
            joinedDate = "ابھی",
            depositAmount = validAmount,
            commission10Percent = commission
        )

        _referralStats.value = stats.copy(
            totalFriendsInvited = stats.totalFriendsInvited + 1,
            totalDepositVolume = stats.totalDepositVolume + validAmount,
            totalCommissionEarned = stats.totalCommissionEarned + commission,
            unclaimedCommission = stats.unclaimedCommission + commission,
            friendsList = listOf(newEntry) + stats.friendsList
        )
    }

    private fun updateReferralStatsForUser(user: UserAccount) {
        _referralStats.value = _referralStats.value.copy(
            referralCode = user.referralCode,
            inviteLink = user.inviteLink
        )
    }

    fun clearAuthMessage() {
        _authMessage.value = null
    }
}
