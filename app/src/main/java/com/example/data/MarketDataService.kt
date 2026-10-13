package com.example.data

import android.util.Log
import com.example.engine.TechnicalAnalysisEngine
import com.example.model.Candle
import com.example.model.GoldSpotData
import com.example.model.MarketTick
import com.example.model.TradingPair
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

object MarketDataService {
    private const val TAG = "MarketDataService"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    // Real-time Live Gold Spot Data Flow
    private val _goldSpotData = MutableStateFlow(GoldSpotData())
    val goldSpotDataFlow: StateFlow<GoldSpotData> = _goldSpotData.asStateFlow()

    // Store in-memory cache of candle history per pair
    private val candleCache = mutableMapOf<String, MutableList<Candle>>()
    private val lastPrices = mutableMapOf<String, Double>()

    init {
        // Initialize historical bars for all pairs
        TradingPair.ALL_PAIRS.forEach { pair ->
            candleCache[pair.symbol] = generateInitialCandles(pair, 120)
            lastPrices[pair.symbol] = candleCache[pair.symbol]?.lastOrNull()?.close ?: pair.initialPrice
        }
    }

    /**
     * Fetches real live rates from public open forex & gold feeds
     */
    suspend fun fetchLiveOnlinePrices(): Map<String, Double> = withContext(Dispatchers.IO) {
        val results = mutableMapOf<String, Double>()

        // 1. Fetch Real Gold Price via primary Gold API (XAU/USD spot)
        var liveGoldPrice: Double? = null
        try {
            val xauRequest = Request.Builder()
                .url("https://api.gold-api.com/price/XAU")
                .header("User-Agent", "AUXTrader-Android/1.0")
                .build()
            val response = httpClient.newCall(xauRequest).execute()
            if (response.isSuccessful) {
                val body = response.body?.string()
                if (body != null) {
                    val json = JSONObject(body)
                    val price = json.optDouble("price", 0.0)
                    if (price > 1000.0) {
                        liveGoldPrice = price
                        Log.d(TAG, "Online Gold-API XAU live price: $price")
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Gold-API fetch failed: ${e.message}")
        }

        // Secondary fallback: Binance PAXG/USDT (1:1 physical gold backing)
        if (liveGoldPrice == null) {
            try {
                val goldRequest = Request.Builder()
                    .url("https://api.binance.com/api/v3/ticker/price?symbol=PAXGUSDT")
                    .header("User-Agent", "AUXTrader-Android/1.0")
                    .build()
                val response = httpClient.newCall(goldRequest).execute()
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (body != null) {
                        val json = JSONObject(body)
                        val price = json.optDouble("price", 0.0)
                        if (price > 1000.0) {
                            liveGoldPrice = price
                            Log.d(TAG, "Online Binance Gold live price: $price")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Binance Gold fetch failed: ${e.message}")
            }
        }

        if (liveGoldPrice != null) {
            results["AUX/USD"] = liveGoldPrice
            _goldSpotData.value = GoldSpotData(
                ouncePrice = liveGoldPrice,
                gram24kPrice = liveGoldPrice / 31.1034768,
                tolaPriceUsd = (liveGoldPrice / 31.1034768) * 11.6638,
                tolaPricePkr = ((liveGoldPrice / 31.1034768) * 11.6638) * 278.0,
                source = "Live Spot Gold (XAU/USD)",
                lastUpdated = System.currentTimeMillis()
            )
        }

        // 2. Fetch Forex pairs via open.er-api.com (official real forex exchange rates, no key needed)
        try {
            val fxRequest = Request.Builder()
                .url("https://open.er-api.com/v6/latest/USD")
                .header("User-Agent", "AUXTrader-Android/1.0")
                .build()
            val response = httpClient.newCall(fxRequest).execute()
            if (response.isSuccessful) {
                val body = response.body?.string()
                if (body != null) {
                    val json = JSONObject(body)
                    val rates = json.getJSONObject("rates")

                    // EUR/USD = 1 / USD_EUR
                    val eurRate = rates.optDouble("EUR", 0.92)
                    if (eurRate > 0) results["EUR/USD"] = 1.0 / eurRate

                    // GBP/USD = 1 / USD_GBP
                    val gbpRate = rates.optDouble("GBP", 0.77)
                    if (gbpRate > 0) results["GBP/USD"] = 1.0 / gbpRate

                    // USD/JPY
                    val jpyRate = rates.optDouble("JPY", 152.0)
                    if (jpyRate > 0) results["USD/JPY"] = jpyRate

                    // USD/CHF
                    val chfRate = rates.optDouble("CHF", 0.865)
                    if (chfRate > 0) results["USD/CHF"] = chfRate

                    // AUD/USD = 1 / USD_AUD
                    val audRate = rates.optDouble("AUD", 1.50)
                    if (audRate > 0) results["AUD/USD"] = 1.0 / audRate

                    // USD/CAD
                    val cadRate = rates.optDouble("CAD", 1.385)
                    if (cadRate > 0) results["USD/CAD"] = cadRate

                    Log.d(TAG, "Online Forex pairs successfully synced")
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Online Forex fetch failed, will use active ticker: ${e.message}")
        }

        results
    }

    /**
     * Generates a realistic live tick for the requested pair,
     * incorporating online prices when fetched, plus live micro pip oscillations.
     */
    fun produceLiveTick(pair: TradingPair, onlineOverride: Double? = null): MarketTick {
        val currentBase = onlineOverride ?: lastPrices[pair.symbol] ?: pair.initialPrice

        // Micro market oscillation
        val deltaMax = if (pair.isGold) 0.35 else 0.00015
        val delta = (Random.nextDouble() * 2.0 - 1.0) * deltaMax
        val newPrice = (currentBase + delta).coerceAtLeast(0.0001)
        val isUp = newPrice >= currentBase

        lastPrices[pair.symbol] = newPrice

        // Spread in pips (Gold ~ 25 cents, FX ~ 1.2 pips)
        val spread = if (pair.isGold) 0.25 else 0.00012
        val bid = newPrice - (spread / 2.0)
        val ask = newPrice + (spread / 2.0)

        // Update the active candle in cache
        updateActiveCandle(pair.symbol, newPrice)

        val candles = candleCache[pair.symbol] ?: emptyList()
        val high24 = candles.maxOfOrNull { it.high } ?: (newPrice * 1.01)
        val low24 = candles.minOfOrNull { it.low } ?: (newPrice * 0.99)
        val open24 = candles.firstOrNull()?.open ?: newPrice
        val change24 = newPrice - open24
        val changePercent24 = if (open24 > 0) (change24 / open24) * 100.0 else 0.0
        val volume24 = (candles.sumOf { it.volume } + Random.nextDouble(50.0, 500.0))

        return MarketTick(
            symbol = pair.symbol,
            price = newPrice,
            bid = bid,
            ask = ask,
            high24h = high24,
            low24h = low24,
            change24h = change24,
            changePercent24h = changePercent24,
            volume24h = volume24,
            isUpTick = isUp
        )
    }

    private fun updateActiveCandle(symbol: String, price: Double) {
        val list = candleCache[symbol] ?: return
        if (list.isEmpty()) return

        val now = System.currentTimeMillis()
        val latest = list.last()
        // If candle is younger than 30 seconds in live simulation, update it with micro tick movements
        if (now - latest.timestamp < 30_000) {
            val updated = latest.copy(
                high = max(latest.high, price),
                low = min(latest.low, price),
                close = price,
                volume = latest.volume + Random.nextDouble(1.0, 5.0)
            )
            list[list.size - 1] = updated
        } else {
            // Close current bar and open new candle like MT5
            val newCandle = Candle(
                timestamp = now,
                open = latest.close,
                high = max(latest.close, price),
                low = min(latest.close, price),
                close = price,
                volume = Random.nextDouble(15.0, 60.0)
            )
            list.add(newCandle)
            if (list.size > 200) {
                list.removeAt(0)
            }
        }
        TechnicalAnalysisEngine.calculateIndicators(list)
    }

    fun getCandles(symbol: String, timeframe: String = "15M"): List<Candle> {
        val list = candleCache[symbol] ?: mutableListOf()
        if (list.isEmpty()) {
            val pair = TradingPair.find(symbol)
            val generated = generateInitialCandles(pair, 120)
            candleCache[symbol] = generated
            TechnicalAnalysisEngine.calculateIndicators(generated)
            return generated
        }
        return list.toList()
    }

    private fun generateInitialCandles(pair: TradingPair, count: Int): MutableList<Candle> {
        val list = mutableListOf<Candle>()
        var currentPrice = pair.initialPrice
        val intervalMillis = 15 * 60 * 1000L
        val startTime = System.currentTimeMillis() - (count * intervalMillis)

        val stepScale = if (pair.isGold) 1.5 else 0.0006

        for (i in 0 until count) {
            val open = currentPrice
            val walk = (Random.nextDouble() * 2.0 - 0.98) * stepScale
            val close = (open + walk).coerceAtLeast(0.0001)
            val high = max(open, close) + Random.nextDouble() * (stepScale * 0.8)
            val low = min(open, close) - Random.nextDouble() * (stepScale * 0.8)
            val volume = Random.nextDouble(50.0, 500.0)

            list.add(
                Candle(
                    timestamp = startTime + (i * intervalMillis),
                    open = open,
                    high = high,
                    low = low,
                    close = close,
                    volume = volume
                )
            )
            currentPrice = close
        }

        TechnicalAnalysisEngine.calculateIndicators(list)
        return list
    }
}
