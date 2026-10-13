package com.example

import com.example.engine.TechnicalAnalysisEngine
import com.example.model.BacktestParams
import com.example.model.BotStrategyType
import com.example.model.Candle
import com.example.model.OrderSide
import com.example.model.TradingPair
import com.example.urdu.UrduForexExpertRepository
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testTradingPairsConfigured() {
        val gold = TradingPair.find("AUX/USD")
        assertNotNull(gold)
        assertTrue(gold.isGold)
        assertEquals(2, gold.decimals)
        assertEquals(4168.40, gold.initialPrice, 0.01)

        val eur = TradingPair.find("EUR/USD")
        assertNotNull(eur)
        assertEquals(4, eur.decimals)
    }

    @Test
    fun testTechnicalIndicatorsCalculation() {
        val candles = mutableListOf<Candle>()
        val baseTime = 1700000000000L
        for (i in 0 until 60) {
            val price = 2650.0 + (i * 0.5)
            candles.add(
                Candle(
                    timestamp = baseTime + (i * 60000L),
                    open = price - 0.2,
                    high = price + 0.8,
                    low = price - 0.5,
                    close = price,
                    volume = 100.0
                )
            )
        }

        TechnicalAnalysisEngine.calculateIndicators(candles)

        val last = candles.last()
        assertTrue("EMA20 should be calculated", last.ema20 > 0)
        assertTrue("EMA50 should be calculated", last.ema50 > 0)
        assertTrue("RSI should be between 0 and 100", last.rsi in 0.0..100.0)
        assertTrue("Bollinger Upper should be >= Bollinger Lower", last.bollUpper >= last.bollLower)
    }

    @Test
    fun testBacktestEngineProducesValidResult() {
        val pair = TradingPair.find("AUX/USD")
        val candles = mutableListOf<Candle>()
        val baseTime = 1700000000000L
        for (i in 0 until 100) {
            val price = 2670.0 + kotlin.math.sin(i.toDouble() / 5.0) * 15.0
            candles.add(
                Candle(
                    timestamp = baseTime + (i * 300000L),
                    open = price - 0.5,
                    high = price + 2.0,
                    low = price - 2.0,
                    close = price,
                    volume = 200.0
                )
            )
        }

        val params = BacktestParams(
            symbol = "AUX/USD",
            strategy = BotStrategyType.EMA_CROSSOVER,
            periodDays = 30,
            initialBalance = 10000.0,
            lotSize = 0.10
        )

        val result = TechnicalAnalysisEngine.runBacktest(params, candles, pair)
        assertNotNull(result)
        assertTrue("Equity curve points should be generated", result.equityCurve.isNotEmpty())
        assertTrue("Final balance should be non-zero", result.finalBalance > 0)
    }

    @Test
    fun testUrduExpertRepositoryContent() {
        assertTrue("Should have Urdu expert posts", UrduForexExpertRepository.EXPERT_POSTS.isNotEmpty())
        val goldPost = UrduForexExpertRepository.EXPERT_POSTS.first()
        assertEquals("AUX/USD", goldPost.pair)
        assertTrue(goldPost.actionUrdu.contains("خریداری") || goldPost.actionUrdu.contains("فروخت"))

        val commentary = UrduForexExpertRepository.generateDynamicUrduCommentary(
            pair = TradingPair.find("AUX/USD"),
            currentPrice = 2690.0,
            rsi = 55.0,
            ema20 = 2685.0,
            ema50 = 2670.0
        )
        assertTrue(commentary.contains("تجزیہ"))
        assertTrue(commentary.contains("2690"))
    }
}
