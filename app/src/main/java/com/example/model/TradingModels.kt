package com.example.model

import java.util.UUID

enum class OrderSide { BUY, SELL }
enum class OrderType { MARKET, LIMIT, BOT_TRIGGER }
enum class OrderStatus { OPEN, CLOSED, CANCELLED }

enum class BotStrategyType(val displayName: String, val description: String) {
    RSI_REVERSAL("RSI Reversal", "Trades oversold (<30) and overbought (>70) market extremes"),
    EMA_CROSSOVER("EMA 20/50 Cross", "Golden Cross triggers BUY, Death Cross triggers SELL"),
    BOLLINGER_BREAKOUT("Bollinger Breakout", "Rides strong volatility breakouts outside bands"),
    GRID_SCALPER("Grid Scalper", "Places tiered buy-low/sell-high orders across market range")
}

data class GoldSpotData(
    val ouncePrice: Double = 4168.40,
    val gram24kPrice: Double = 4168.40 / 31.1034768,
    val tolaPriceUsd: Double = (4168.40 / 31.1034768) * 11.6638,
    val tolaPricePkr: Double = ((4168.40 / 31.1034768) * 11.6638) * 278.0,
    val source: String = "XAU/USD Live Spot Feed",
    val lastUpdated: Long = System.currentTimeMillis()
)

data class TradingPair(
    val symbol: String,
    val base: String,
    val quote: String,
    val name: String,
    val decimals: Int,
    val pipSize: Double,
    val initialPrice: Double,
    val isGold: Boolean = false,
    val urduName: String = ""
) {
    companion object {
        val ALL_PAIRS = listOf(
            TradingPair("AUX/USD", "AUX", "USD", "Gold Bullion / US Dollar (XAU/USD)", 2, 0.01, 4169.10, isGold = true, urduName = "سونے کی لائیو ٹریڈنگ (AUX/USD)"),
            TradingPair("EUR/USD", "EUR", "USD", "Euro / US Dollar", 4, 0.0001, 1.1171, urduName = "یورو بنام امریکی ڈالر"),
            TradingPair("GBP/USD", "GBP", "USD", "British Pound / US Dollar", 4, 0.0001, 1.3217, urduName = "برطانوی پاؤنڈ بنام امریکی ڈالر"),
            TradingPair("USD/JPY", "USD", "JPY", "US Dollar / Japanese Yen", 2, 0.01, 158.54, urduName = "امریکی ڈالر بنام جاپانی ین"),
            TradingPair("USD/CHF", "USD", "CHF", "US Dollar / Swiss Franc", 4, 0.0001, 0.8340, urduName = "امریکی ڈالر بنام سوئس فرانک"),
            TradingPair("AUD/USD", "AUD", "USD", "Australian Dollar / US Dollar", 4, 0.0001, 0.6961, urduName = "آسٹریلوی ڈالر بنام امریکی ڈالر"),
            TradingPair("USD/CAD", "USD", "CAD", "US Dollar / Canadian Dollar", 4, 0.0001, 1.4274, urduName = "امریکی ڈالر بنام کینیڈین ڈالر")
        )

        fun find(symbol: String): TradingPair {
            return ALL_PAIRS.find { it.symbol == symbol } ?: ALL_PAIRS.first()
        }
    }
}

data class MarketTick(
    val symbol: String,
    val price: Double,
    val bid: Double,
    val ask: Double,
    val high24h: Double,
    val low24h: Double,
    val change24h: Double,
    val changePercent24h: Double,
    val volume24h: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val isUpTick: Boolean = true
)

data class Candle(
    val timestamp: Long,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Double,
    var ema20: Double = 0.0,
    var ema50: Double = 0.0,
    var rsi: Double = 50.0,
    var bollUpper: Double = 0.0,
    var bollLower: Double = 0.0
)

data class TradeOrder(
    val id: String = UUID.randomUUID().toString().take(8),
    val symbol: String,
    val side: OrderSide,
    val type: OrderType = OrderType.BOT_TRIGGER,
    val status: OrderStatus = OrderStatus.OPEN,
    val lots: Double,
    val openPrice: Double,
    val currentPrice: Double = openPrice,
    val closePrice: Double? = null,
    val takeProfit: Double? = null,
    val stopLoss: Double? = null,
    val trailingStopPips: Double? = null,
    val highestPrice: Double = openPrice,
    val lowestPrice: Double = openPrice,
    val pnl: Double = 0.0,
    val openTime: Long = System.currentTimeMillis(),
    val closeTime: Long? = null,
    val strategyTag: String = "Manual",
    val closeReason: String? = null
)

data class BotConfig(
    val enabled: Boolean = false,
    val symbol: String = "AUX/USD",
    val strategy: BotStrategyType = BotStrategyType.RSI_REVERSAL,
    val lotSize: Double = 0.10,
    val takeProfitPips: Double = 60.0,
    val stopLossPips: Double = 35.0,
    val trailingStop: Boolean = true,
    val trailingStopPips: Double = 20.0,
    val maxOpenPositions: Int = 3,
    val scanIntervalSeconds: Int = 2,
    val isPaperTrading: Boolean = false,
    val mt5Connected: Boolean = true,
    val mt5Server: String = "MetaQuotes-Demo / Exness-Real",
    val mt5AccountNumber: String = "50982410",
    val mt5PingMs: Long = 14
)

data class BacktestParams(
    val symbol: String = "AUX/USD",
    val strategy: BotStrategyType = BotStrategyType.EMA_CROSSOVER,
    val periodDays: Int = 30,
    val initialBalance: Double = 10000.0,
    val lotSize: Double = 0.10,
    val takeProfitPips: Double = 60.0,
    val stopLossPips: Double = 35.0,
    val rsiThreshold: Double = 30.0
)

data class BacktestResult(
    val totalTrades: Int,
    val wins: Int,
    val losses: Int,
    val winRate: Double,
    val totalPnl: Double,
    val profitFactor: Double,
    val maxDrawdownPercent: Double,
    val finalBalance: Double,
    val equityCurve: List<Pair<Long, Double>>,
    val trades: List<TradeOrder>
)

enum class AlertCondition { PRICE_ABOVE, PRICE_BELOW, RSI_OVERBOUGHT, RSI_OVERSOLD, GOLDEN_CROSS }

data class MarketAlert(
    val id: String = UUID.randomUUID().toString().take(8),
    val symbol: String,
    val condition: AlertCondition,
    val targetValue: Double,
    val isTriggered: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val note: String = ""
)

data class RiskSettings(
    val dailyMaxLoss: Double = 500.0,
    val currentDailyLoss: Double = 0.0,
    val emergencyCircuitBreakerEnabled: Boolean = true,
    val maxConcurrentTrades: Int = 5,
    val maxLotPerTrade: Double = 1.0,
    val autoTrailingStop: Boolean = true
)

data class ApiConnection(
    val exchangeName: String,
    val apiKey: String,
    val apiSecret: String,
    val accountIdOrServer: String,
    val isConnected: Boolean,
    val isDemoMode: Boolean = true,
    val pingMs: Long = 24,
    val lastUpdated: Long = System.currentTimeMillis()
)

data class UrduExpertPost(
    val id: String,
    val expertNameUrdu: String,
    val expertRankUrdu: String,
    val pair: String,
    val actionUrdu: String, // مثلاً "مضبوط خریداری (BUY)" یا "فروخت (SELL)"
    val isBullish: Boolean,
    val entryPrice: Double,
    val takeProfit: Double,
    val stopLoss: Double,
    val timeframe: String,
    val titleUrdu: String,
    val detailedAnalysisUrdu: String,
    val keySupportResistanceUrdu: String,
    val winRatePercent: Int,
    val timestampAgo: String
)
