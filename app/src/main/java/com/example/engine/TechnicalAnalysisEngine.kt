package com.example.engine

import com.example.model.BacktestParams
import com.example.model.BacktestResult
import com.example.model.BotStrategyType
import com.example.model.Candle
import com.example.model.OrderSide
import com.example.model.OrderStatus
import com.example.model.OrderType
import com.example.model.TradeOrder
import com.example.model.TradingPair
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

object TechnicalAnalysisEngine {

    /**
     * Computes technical indicators for a given series of candles:
     * - EMA 20, EMA 50
     * - RSI 14
     * - Bollinger Bands (20, 2)
     */
    fun calculateIndicators(candles: List<Candle>) {
        if (candles.isEmpty()) return

        // 1. Calculate EMA 20
        calculateEma(candles, 20) { candle, value -> candle.ema20 = value }

        // 2. Calculate EMA 50
        calculateEma(candles, 50) { candle, value -> candle.ema50 = value }

        // 3. Calculate RSI 14
        calculateRsi(candles, 14)

        // 4. Calculate Bollinger Bands 20, 2
        calculateBollingerBands(candles, 20, 2.0)
    }

    private fun calculateEma(candles: List<Candle>, period: Int, applyToCandle: (Candle, Double) -> Unit) {
        if (candles.size < period) {
            candles.forEach { applyToCandle(it, it.close) }
            return
        }
        val k = 2.0 / (period + 1)
        var ema = candles.take(period).map { it.close }.average()
        for (i in period until candles.size) {
            val close = candles[i].close
            ema = (close * k) + (ema * (1 - k))
            applyToCandle(candles[i], ema)
        }
        for (i in 0 until min(period, candles.size)) {
            applyToCandle(candles[i], candles[i].close)
        }
    }

    private fun calculateRsi(candles: List<Candle>, period: Int = 14) {
        if (candles.size <= period) return
        var gains = 0.0
        var losses = 0.0

        for (i in 1..period) {
            val diff = candles[i].close - candles[i - 1].close
            if (diff >= 0) gains += diff else losses += abs(diff)
        }

        var avgGain = gains / period
        var avgLoss = losses / period

        candles[period].rsi = if (avgLoss == 0.0) 100.0 else {
            val rs = avgGain / avgLoss
            100.0 - (100.0 / (1.0 + rs))
        }

        for (i in (period + 1) until candles.size) {
            val diff = candles[i].close - candles[i - 1].close
            val currentGain = if (diff > 0) diff else 0.0
            val currentLoss = if (diff < 0) abs(diff) else 0.0

            avgGain = ((avgGain * (period - 1)) + currentGain) / period
            avgLoss = ((avgLoss * (period - 1)) + currentLoss) / period

            candles[i].rsi = if (avgLoss == 0.0) 100.0 else {
                val rs = avgGain / avgLoss
                100.0 - (100.0 / (1.0 + rs))
            }
        }
    }

    private fun calculateBollingerBands(candles: List<Candle>, period: Int = 20, numStd: Double = 2.0) {
        if (candles.size < period) return
        for (i in period - 1 until candles.size) {
            val window = candles.subList(i - period + 1, i + 1)
            val mean = window.map { it.close }.average()
            val variance = window.map { (it.close - mean) * (it.close - mean) }.average()
            val stdDev = sqrt(variance)

            candles[i].bollUpper = mean + (numStd * stdDev)
            candles[i].bollLower = mean - (numStd * stdDev)
        }
    }

    /**
     * Checks strategy signals on the latest candles:
     * Returns OrderSide.BUY, OrderSide.SELL, or null
     */
    fun evaluateSignal(
        strategy: BotStrategyType,
        candles: List<Candle>
    ): OrderSide? {
        if (candles.size < 5) return null
        val latest = candles.last()
        val prev = candles[candles.size - 2]

        return when (strategy) {
            BotStrategyType.RSI_REVERSAL -> {
                when {
                    prev.rsi < 32 && latest.rsi >= 32 -> OrderSide.BUY // Oversold bounce
                    prev.rsi > 68 && latest.rsi <= 68 -> OrderSide.SELL // Overbought roll
                    else -> null
                }
            }
            BotStrategyType.EMA_CROSSOVER -> {
                when {
                    prev.ema20 <= prev.ema50 && latest.ema20 > latest.ema50 -> OrderSide.BUY // Golden Cross
                    prev.ema20 >= prev.ema50 && latest.ema20 < latest.ema50 -> OrderSide.SELL // Death Cross
                    else -> null
                }
            }
            BotStrategyType.BOLLINGER_BREAKOUT -> {
                when {
                    latest.close > latest.bollUpper && prev.close <= prev.bollUpper -> OrderSide.BUY
                    latest.close < latest.bollLower && prev.close >= prev.bollLower -> OrderSide.SELL
                    else -> null
                }
            }
            BotStrategyType.GRID_SCALPER -> {
                // Scalp dips below EMA20 and rallies above EMA20
                if (latest.close < latest.ema20 * 0.998) OrderSide.BUY
                else if (latest.close > latest.ema20 * 1.002) OrderSide.SELL
                else null
            }
        }
    }

    /**
     * Runs full historical backtest simulation
     */
    fun runBacktest(
        params: BacktestParams,
        candles: List<Candle>,
        pair: TradingPair
    ): BacktestResult {
        var balance = params.initialBalance
        val equityCurve = mutableListOf<Pair<Long, Double>>()
        val executedTrades = mutableListOf<TradeOrder>()
        val openOrders = mutableListOf<TradeOrder>()

        var peakBalance = balance
        var maxDrawdown = 0.0
        var totalWins = 0
        var totalLosses = 0
        var grossProfit = 0.0
        var grossLoss = 0.0

        val pipMultiplier = if (pair.isGold) 1.0 else 10000.0 // Gold pip = $0.10 or $1.0; FX pip = 0.0001
        val lotScale = if (pair.isGold) 100.0 else 100000.0

        calculateIndicators(candles)

        for (i in 50 until candles.size) {
            val candle = candles[i]
            val subList = candles.subList(0, i + 1)

            // Check if open trades hit TP or SL
            val iterator = openOrders.iterator()
            while (iterator.hasNext()) {
                val order = iterator.next()
                var closed = false
                var exitPrice = order.openPrice
                var reason = ""

                if (order.side == OrderSide.BUY) {
                    if (order.takeProfit != null && candle.high >= order.takeProfit) {
                        exitPrice = order.takeProfit
                        reason = "TP Hit"
                        closed = true
                    } else if (order.stopLoss != null && candle.low <= order.stopLoss) {
                        exitPrice = order.stopLoss
                        reason = "SL Hit"
                        closed = true
                    }
                } else {
                    if (order.takeProfit != null && candle.low <= order.takeProfit) {
                        exitPrice = order.takeProfit
                        reason = "TP Hit"
                        closed = true
                    } else if (order.stopLoss != null && candle.high >= order.stopLoss) {
                        exitPrice = order.stopLoss
                        reason = "SL Hit"
                        closed = true
                    }
                }

                if (closed) {
                    val pips = if (order.side == OrderSide.BUY) {
                        (exitPrice - order.openPrice) * pipMultiplier
                    } else {
                        (order.openPrice - exitPrice) * pipMultiplier
                    }
                    val tradePnl = (pips / pipMultiplier) * (order.lots * lotScale)
                    balance += tradePnl

                    if (tradePnl > 0) {
                        totalWins++
                        grossProfit += tradePnl
                    } else {
                        totalLosses++
                        grossLoss += abs(tradePnl)
                    }

                    peakBalance = max(peakBalance, balance)
                    val currentDd = if (peakBalance > 0) ((peakBalance - balance) / peakBalance) * 100.0 else 0.0
                    maxDrawdown = max(maxDrawdown, currentDd)

                    executedTrades.add(
                        order.copy(
                            status = OrderStatus.CLOSED,
                            closePrice = exitPrice,
                            closeTime = candle.timestamp,
                            pnl = tradePnl,
                            closeReason = reason
                        )
                    )
                    iterator.remove()
                }
            }

            // Signal evaluation for new trades
            if (openOrders.size < 2) {
                val signal = evaluateSignal(params.strategy, subList)
                if (signal != null) {
                    val entryPrice = candle.close
                    val tpDelta = (params.takeProfitPips / pipMultiplier)
                    val slDelta = (params.stopLossPips / pipMultiplier)

                    val tp = if (signal == OrderSide.BUY) entryPrice + tpDelta else entryPrice - tpDelta
                    val sl = if (signal == OrderSide.BUY) entryPrice - slDelta else entryPrice + slDelta

                    openOrders.add(
                        TradeOrder(
                            symbol = pair.symbol,
                            side = signal,
                            type = OrderType.BOT_TRIGGER,
                            lots = params.lotSize,
                            openPrice = entryPrice,
                            takeProfit = tp,
                            stopLoss = sl,
                            openTime = candle.timestamp,
                            strategyTag = params.strategy.displayName
                        )
                    )
                }
            }

            // Record equity point every 10 candles
            if (i % 10 == 0 || i == candles.size - 1) {
                equityCurve.add(Pair(candle.timestamp, balance))
            }
        }

        val totalTrades = totalWins + totalLosses
        val winRate = if (totalTrades > 0) (totalWins.toDouble() / totalTrades.toDouble()) * 100.0 else 0.0
        val profitFactor = if (grossLoss > 0) grossProfit / grossLoss else if (grossProfit > 0) 9.99 else 1.0

        return BacktestResult(
            totalTrades = totalTrades,
            wins = totalWins,
            losses = totalLosses,
            winRate = winRate,
            totalPnl = balance - params.initialBalance,
            profitFactor = profitFactor,
            maxDrawdownPercent = maxDrawdown,
            finalBalance = balance,
            equityCurve = equityCurve,
            trades = executedTrades
        )
    }
}
