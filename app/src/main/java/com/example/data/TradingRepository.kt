package com.example.data

import com.example.engine.TechnicalAnalysisEngine
import com.example.model.AlertCondition
import com.example.model.ApiConnection
import com.example.model.BotConfig
import com.example.model.MarketAlert
import com.example.model.MarketTick
import com.example.model.OrderSide
import com.example.model.OrderStatus
import com.example.model.OrderType
import com.example.model.RiskSettings
import com.example.model.TradeOrder
import com.example.model.TradingPair
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class TradingRepository {

    // Balance & Portfolio
    private val _balance = MutableStateFlow(10000.0)
    val balance: StateFlow<Double> = _balance.asStateFlow()

    private val _realizedPnl = MutableStateFlow(0.0)
    val realizedPnl: StateFlow<Double> = _realizedPnl.asStateFlow()

    private val _unrealizedPnl = MutableStateFlow(0.0)
    val unrealizedPnl: StateFlow<Double> = _unrealizedPnl.asStateFlow()

    private val _openPositions = MutableStateFlow<List<TradeOrder>>(emptyList())
    val openPositions: StateFlow<List<TradeOrder>> = _openPositions.asStateFlow()

    private val _tradeHistory = MutableStateFlow<List<TradeOrder>>(emptyList())
    val tradeHistory: StateFlow<List<TradeOrder>> = _tradeHistory.asStateFlow()

    // Bot Config
    private val _botConfig = MutableStateFlow(BotConfig())
    val botConfig: StateFlow<BotConfig> = _botConfig.asStateFlow()

    // Bot Execution Logs
    private val _botLogs = MutableStateFlow<List<String>>(
        listOf(
            "System: AUX & Forex Trading Bot initialized.",
            "Feed: Real-time price listeners connected.",
            "Risk Engine: Circuit breaker enabled ($500 daily limit)."
        )
    )
    val botLogs: StateFlow<List<String>> = _botLogs.asStateFlow()

    // Risk Settings
    private val _riskSettings = MutableStateFlow(RiskSettings())
    val riskSettings: StateFlow<RiskSettings> = _riskSettings.asStateFlow()

    // Alerts
    private val _alerts = MutableStateFlow<List<MarketAlert>>(
        listOf(
            MarketAlert(symbol = "AUX/USD", condition = AlertCondition.PRICE_ABOVE, targetValue = 2700.0, note = "Major Bullish Resistance Breakout"),
            MarketAlert(symbol = "EUR/USD", condition = AlertCondition.RSI_OVERSOLD, targetValue = 30.0, note = "Oversold Reversal Dip")
        )
    )
    val alerts: StateFlow<List<MarketAlert>> = _alerts.asStateFlow()

    private val _recentTriggeredAlert = MutableStateFlow<MarketAlert?>(null)
    val recentTriggeredAlert: StateFlow<MarketAlert?> = _recentTriggeredAlert.asStateFlow()

    // Exchange API Credentials
    private val _apiConnections = MutableStateFlow<List<ApiConnection>>(
        listOf(
            ApiConnection("MetaTrader 5 (MT5)", "mt5_demo_pk_99418", "••••••••••••", "Server: MetaQuotes-Demo #50129", isConnected = true, pingMs = 18),
            ApiConnection("Binance", "bin_api_aux_98721", "••••••••••••", "Account: Pro-Gold-Margin", isConnected = true, pingMs = 28),
            ApiConnection("Exness", "", "", "Account: Real-14812", isConnected = false, pingMs = 45),
            ApiConnection("OctaFX", "", "", "Server: OctaFX-Real-2", isConnected = false, pingMs = 52)
        )
    )
    val apiConnections: StateFlow<List<ApiConnection>> = _apiConnections.asStateFlow()

    // Tickers Cache
    private val _liveTicks = MutableStateFlow<Map<String, MarketTick>>(emptyMap())
    val liveTicks: StateFlow<Map<String, MarketTick>> = _liveTicks.asStateFlow()

    init {
        // Seed 3 realistic past closed trades so dashboard performance shows history immediately
        val seed1 = TradeOrder(
            symbol = "AUX/USD",
            side = OrderSide.BUY,
            type = OrderType.BOT_TRIGGER,
            status = OrderStatus.CLOSED,
            lots = 0.20,
            openPrice = 4148.50,
            closePrice = 4172.00,
            pnl = 470.00,
            openTime = System.currentTimeMillis() - 7200000,
            closeTime = System.currentTimeMillis() - 3600000,
            strategyTag = "EMA Cross",
            closeReason = "Take Profit Hit"
        )
        val seed2 = TradeOrder(
            symbol = "EUR/USD",
            side = OrderSide.SELL,
            type = OrderType.BOT_TRIGGER,
            status = OrderStatus.CLOSED,
            lots = 0.15,
            openPrice = 1.0880,
            closePrice = 1.0840,
            pnl = 60.00,
            openTime = System.currentTimeMillis() - 14400000,
            closeTime = System.currentTimeMillis() - 10800000,
            strategyTag = "RSI Reversal",
            closeReason = "Take Profit Hit"
        )
        val seed3 = TradeOrder(
            symbol = "GBP/USD",
            side = OrderSide.BUY,
            type = OrderType.BOT_TRIGGER,
            status = OrderStatus.CLOSED,
            lots = 0.10,
            openPrice = 1.2995,
            closePrice = 1.2965,
            pnl = -30.00,
            openTime = System.currentTimeMillis() - 25000000,
            closeTime = System.currentTimeMillis() - 21000000,
            strategyTag = "Bollinger Breakout",
            closeReason = "Stop Loss Hit"
        )
        _tradeHistory.value = listOf(seed1, seed2, seed3)
        _balance.value = 10000.0 + seed1.pnl + seed2.pnl + seed3.pnl
        _realizedPnl.value = seed1.pnl + seed2.pnl + seed3.pnl
    }

    fun updateTick(tick: MarketTick) {
        val currentMap = _liveTicks.value.toMutableMap()
        currentMap[tick.symbol] = tick
        _liveTicks.value = currentMap

        // Update open positions PnL and check exits
        processOpenPositions(tick)

        // Check alerts
        checkAlerts(tick)

        // Bot autonomous execution if enabled for this pair
        if (_botConfig.value.enabled && _botConfig.value.symbol == tick.symbol) {
            evaluateBotDecision(tick)
        }
    }

    private fun processOpenPositions(tick: MarketTick) {
        val pair = TradingPair.find(tick.symbol)
        val pipMultiplier = if (pair.isGold) 1.0 else 10000.0
        val lotScale = if (pair.isGold) 100.0 else 100000.0

        val currentOpen = _openPositions.value.toMutableList()
        val toClose = mutableListOf<Pair<TradeOrder, String>>()

        for (i in 0 until currentOpen.size) {
            val order = currentOpen[i]
            if (order.symbol != tick.symbol) continue

            val currentPrice = tick.price
            val highest = max(order.highestPrice, currentPrice)
            val lowest = min(order.lowestPrice, currentPrice)

            val pnl = if (order.side == OrderSide.BUY) {
                ((currentPrice - order.openPrice) / pipMultiplier) * (order.lots * (lotScale * (1.0 / pipMultiplier)))
            } else {
                ((order.openPrice - currentPrice) / pipMultiplier) * (order.lots * (lotScale * (1.0 / pipMultiplier)))
            }

            var updatedOrder = order.copy(
                currentPrice = currentPrice,
                highestPrice = highest,
                lowestPrice = lowest,
                pnl = pnl
            )

            // Trailing stop loss update
            if (_botConfig.value.trailingStop && order.trailingStopPips != null) {
                val trailDelta = order.trailingStopPips / pipMultiplier
                if (order.side == OrderSide.BUY) {
                    val potentialSl = highest - trailDelta
                    if (order.stopLoss == null || potentialSl > order.stopLoss) {
                        updatedOrder = updatedOrder.copy(stopLoss = potentialSl)
                    }
                } else {
                    val potentialSl = lowest + trailDelta
                    if (order.stopLoss == null || potentialSl < order.stopLoss) {
                        updatedOrder = updatedOrder.copy(stopLoss = potentialSl)
                    }
                }
            }

            // Check TP / SL Exits
            if (order.side == OrderSide.BUY) {
                if (updatedOrder.takeProfit != null && currentPrice >= updatedOrder.takeProfit) {
                    toClose.add(Pair(updatedOrder, "Take Profit Hit (+$${String.format("%.2f", pnl)})"))
                } else if (updatedOrder.stopLoss != null && currentPrice <= updatedOrder.stopLoss) {
                    toClose.add(Pair(updatedOrder, "Stop Loss Hit ($${String.format("%.2f", pnl)})"))
                }
            } else {
                if (updatedOrder.takeProfit != null && currentPrice <= updatedOrder.takeProfit) {
                    toClose.add(Pair(updatedOrder, "Take Profit Hit (+$${String.format("%.2f", pnl)})"))
                } else if (updatedOrder.stopLoss != null && currentPrice >= updatedOrder.stopLoss) {
                    toClose.add(Pair(updatedOrder, "Stop Loss Hit ($${String.format("%.2f", pnl)})"))
                }
            }

            currentOpen[i] = updatedOrder
        }

        // Close orders that hit TP or SL
        toClose.forEach { (order, reason) ->
            closePosition(order.id, reason)
        }

        _openPositions.value = currentOpen
        _unrealizedPnl.value = currentOpen.sumOf { it.pnl }
    }

    private fun evaluateBotDecision(tick: MarketTick) {
        // Circuit breaker check
        val risk = _riskSettings.value
        if (risk.emergencyCircuitBreakerEnabled && risk.currentDailyLoss >= risk.dailyMaxLoss) {
            _botConfig.value = _botConfig.value.copy(enabled = false)
            addLog("EMERGENCY CIRCUIT BREAKER: Daily loss exceeded $${risk.dailyMaxLoss}. Bot halted.")
            return
        }

        if (_openPositions.value.count { it.symbol == tick.symbol } >= _botConfig.value.maxOpenPositions) {
            return
        }

        val candles = MarketDataService.getCandles(tick.symbol)
        val signal = TechnicalAnalysisEngine.evaluateSignal(_botConfig.value.strategy, candles)

        if (signal != null) {
            val pair = TradingPair.find(tick.symbol)
            val pipMultiplier = if (pair.isGold) 1.0 else 10000.0
            val tpDelta = _botConfig.value.takeProfitPips / pipMultiplier
            val slDelta = _botConfig.value.stopLossPips / pipMultiplier

            val tp = if (signal == OrderSide.BUY) tick.price + tpDelta else tick.price - tpDelta
            val sl = if (signal == OrderSide.BUY) tick.price - slDelta else tick.price + slDelta

            openPosition(
                symbol = tick.symbol,
                side = signal,
                type = OrderType.BOT_TRIGGER,
                lots = _botConfig.value.lotSize,
                takeProfit = tp,
                stopLoss = sl,
                trailingStopPips = if (_botConfig.value.trailingStop) _botConfig.value.trailingStopPips else null,
                strategyTag = _botConfig.value.strategy.displayName
            )
            addLog("BOT EXECUTE: $signal on ${tick.symbol} @ ${String.format("%.${pair.decimals}f", tick.price)} (${_botConfig.value.strategy.displayName})")
        }
    }

    fun openPosition(
        symbol: String,
        side: OrderSide,
        type: OrderType,
        lots: Double,
        takeProfit: Double?,
        stopLoss: Double?,
        trailingStopPips: Double? = null,
        strategyTag: String = "Manual"
    ) {
        val tick = _liveTicks.value[symbol]
        val pair = TradingPair.find(symbol)
        val entryPrice = if (side == OrderSide.BUY) (tick?.ask ?: pair.initialPrice) else (tick?.bid ?: pair.initialPrice)

        val newOrder = TradeOrder(
            symbol = symbol,
            side = side,
            type = type,
            status = OrderStatus.OPEN,
            lots = lots,
            openPrice = entryPrice,
            currentPrice = entryPrice,
            takeProfit = takeProfit,
            stopLoss = stopLoss,
            trailingStopPips = trailingStopPips,
            strategyTag = strategyTag
        )

        _openPositions.value = _openPositions.value + newOrder
        addLog("Order Open: $side $lots lots ${symbol} @ $entryPrice")
    }

    fun closePosition(orderId: String, reason: String = "Manual Close") {
        val order = _openPositions.value.find { it.id == orderId } ?: return
        val currentPrice = order.currentPrice
        val finalPnl = order.pnl

        val closedOrder = order.copy(
            status = OrderStatus.CLOSED,
            closePrice = currentPrice,
            closeTime = System.currentTimeMillis(),
            pnl = finalPnl,
            closeReason = reason
        )

        _openPositions.value = _openPositions.value.filter { it.id != orderId }
        _tradeHistory.value = listOf(closedOrder) + _tradeHistory.value
        _balance.value += finalPnl
        _realizedPnl.value += finalPnl

        // Update daily loss counter if loss
        if (finalPnl < 0) {
            val newDailyLoss = _riskSettings.value.currentDailyLoss + abs(finalPnl)
            _riskSettings.value = _riskSettings.value.copy(currentDailyLoss = newDailyLoss)
        }

        addLog("Order Closed [${order.symbol}]: PnL ${if (finalPnl >= 0) "+$" else "-$"}${String.format("%.2f", abs(finalPnl))} ($reason)")
    }

    fun addFunds(amount: Double, reason: String = "Deposit") {
        _balance.value += amount
        addLog("Wallet Credited: +$${String.format("%.2f", amount)} ($reason)")
    }

    private fun checkAlerts(tick: MarketTick) {
        val currentAlerts = _alerts.value.toMutableList()
        for (i in 0 until currentAlerts.size) {
            val alert = currentAlerts[i]
            if (alert.symbol != tick.symbol || alert.isTriggered) continue

            val triggered = when (alert.condition) {
                AlertCondition.PRICE_ABOVE -> tick.price >= alert.targetValue
                AlertCondition.PRICE_BELOW -> tick.price <= alert.targetValue
                AlertCondition.RSI_OVERBOUGHT -> {
                    val rsi = MarketDataService.getCandles(tick.symbol).lastOrNull()?.rsi ?: 50.0
                    rsi >= alert.targetValue
                }
                AlertCondition.RSI_OVERSOLD -> {
                    val rsi = MarketDataService.getCandles(tick.symbol).lastOrNull()?.rsi ?: 50.0
                    rsi <= alert.targetValue
                }
                AlertCondition.GOLDEN_CROSS -> {
                    val c = MarketDataService.getCandles(tick.symbol).lastOrNull()
                    c != null && c.ema20 > c.ema50
                }
            }

            if (triggered) {
                currentAlerts[i] = alert.copy(isTriggered = true)
                _recentTriggeredAlert.value = currentAlerts[i]
                addLog("ALERT TRIGGERED: ${alert.symbol} condition met (${alert.condition.name} @ ${alert.targetValue})")
            }
        }
        _alerts.value = currentAlerts
    }

    fun dismissRecentAlert() {
        _recentTriggeredAlert.value = null
    }

    fun addAlert(alert: MarketAlert) {
        _alerts.value = listOf(alert) + _alerts.value
        addLog("Alert Created: ${alert.symbol} ${alert.condition} @ ${alert.targetValue}")
    }

    fun removeAlert(alertId: String) {
        _alerts.value = _alerts.value.filter { it.id != alertId }
    }

    fun updateBotConfig(config: BotConfig) {
        _botConfig.value = config
        addLog("Bot Config Updated: Enabled=${config.enabled}, Strategy=${config.strategy.displayName}, Pair=${config.symbol}")
    }

    fun updateRiskSettings(settings: RiskSettings) {
        _riskSettings.value = settings
        addLog("Risk Settings Updated: Daily Max Loss=$${settings.dailyMaxLoss}")
    }

    fun updateApiConnection(index: Int, connection: ApiConnection) {
        val list = _apiConnections.value.toMutableList()
        if (index in list.indices) {
            list[index] = connection
            _apiConnections.value = list
            addLog("API Connection Updated: ${connection.exchangeName} (Connected=${connection.isConnected})")
        }
    }

    fun addLog(msg: String) {
        val timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
        val formatted = "[$timestamp] $msg"
        _botLogs.value = listOf(formatted) + _botLogs.value.take(40)
    }
}
