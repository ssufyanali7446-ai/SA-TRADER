package com.example.model

import java.util.UUID

data class UserAccount(
    val id: String = UUID.randomUUID().toString().take(8),
    val fullName: String,
    val email: String,
    val phoneNumber: String = "",
    val city: String = "",
    val cnicOrId: String = "",
    val isEmailVerified: Boolean = false,
    val verificationOtp: String = "749215",
    val referralCode: String = generateReferralCode(),
    val referredByCode: String? = null,
    val inviteLink: String = "https://auxtrader.app/invite?ref=" + generateReferralCode(),
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        fun generateReferralCode(): String {
            val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
            val randomStr = (1..5).map { chars.random() }.joinToString("")
            return "AUX-$randomStr"
        }
    }
}

enum class DepositMethod(
    val displayName: String,
    val accountTitle: String,
    val accountNumber: String,
    val instructionsUrdu: String
) {
    EASYPAISA(
        displayName = "EasyPaisa",
        accountTitle = "Sufyan Ali",
        accountNumber = "03258097930",
        instructionsUrdu = "ایزی پیسہ ایپ سے 03258097930 پر رقم بھیجیں (نام: Sufyan Ali)"
    ),
    JAZZCASH(
        displayName = "JazzCash",
        accountTitle = "Sufyan Ali",
        accountNumber = "03258097930",
        instructionsUrdu = "جاز کیش ایپ سے 03258097930 پر رقم بھیجیں (نام: Sufyan Ali)"
    )
}

data class DepositTransaction(
    val id: String = UUID.randomUUID().toString().take(8),
    val amountUsd: Double, // Min $1, Max $500
    val amountPkr: Double = amountUsd * 280.0,
    val method: DepositMethod,
    val senderPhoneNumber: String = "",
    val transactionIdTid: String = "",
    val status: String = "COMPLETED",
    val timestamp: Long = System.currentTimeMillis(),
    val referenceNote: String = ""
)

data class ReferralFriendEntry(
    val id: String = UUID.randomUUID().toString().take(6),
    val friendName: String,
    val friendEmail: String,
    val friendPhone: String = "03XX-XXXXXXX",
    val joinedDate: String,
    val depositAmount: Double,
    val commission10Percent: Double = depositAmount * 0.10,
    val isVerified: Boolean = true
)

data class ReferralProgramStats(
    val referralCode: String,
    val inviteLink: String,
    val totalFriendsInvited: Int = 3,
    val totalDepositVolume: Double = 350.0,
    val totalCommissionEarned: Double = 35.0, // 10% of $350
    val unclaimedCommission: Double = 35.0,
    val friendsList: List<ReferralFriendEntry> = listOf(
        ReferralFriendEntry("1", "Muhammad Hamza", "hamza.trader@gmail.com", "0300-1234567", "آج", 150.0, 15.0),
        ReferralFriendEntry("2", "Zeeshan Ali", "zeeshan.fx@gmail.com", "0321-9876543", "کل", 100.0, 10.0),
        ReferralFriendEntry("3", "Usman Tariq", "usman.crypto@gmail.com", "0345-5556677", "3 دن پہلے", 100.0, 10.0)
    )
)
