package com.example.ui

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AuthAndReferralRepository
import com.example.data.MarketDataService
import com.example.data.TradingRepository
import com.example.engine.TechnicalAnalysisEngine
import com.example.model.AlertCondition
import com.example.model.ApiConnection
import com.example.model.BacktestParams
import com.example.model.BacktestResult
import com.example.model.BotConfig
import com.example.model.BotStrategyType
import com.example.model.Candle
import com.example.model.DepositMethod
import com.example.model.DepositTransaction
import com.example.model.MarketAlert
import com.example.model.MarketTick
import com.example.model.OrderSide
import com.example.model.OrderType
import com.example.model.RiskSettings
import com.example.model.TradingPair
import com.example.urdu.UrduForexExpertRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class TradingViewModel(application: Application) : AndroidViewModel(application) {

    val repository = TradingRepository()
    val authRepository = AuthAndReferralRepository()

    // Active Selected Pair
    private val _selectedPair = MutableStateFlow(TradingPair.ALL_PAIRS.first())
    val selectedPair: StateFlow<TradingPair> = _selectedPair.asStateFlow()

    // Chart Timeframe & Indicators Toggles
    private val _selectedTimeframe = MutableStateFlow("15M")
    val selectedTimeframe: StateFlow<String> = _selectedTimeframe.asStateFlow()

    private val _isCandlestickMode = MutableStateFlow(true)
    val isCandlestickMode: StateFlow<Boolean> = _isCandlestickMode.asStateFlow()

    private val _showEma = MutableStateFlow(true)
    val showEma: StateFlow<Boolean> = _showEma.asStateFlow()

    private val _showBollinger = MutableStateFlow(true)
    val showBollinger: StateFlow<Boolean> = _showBollinger.asStateFlow()

    private val _showRsi = MutableStateFlow(true)
    val showRsi: StateFlow<Boolean> = _showRsi.asStateFlow()

    // Chart Visibility Toggle (User can hide/show live chart via icon)
    private val _isChartVisible = MutableStateFlow(true)
    val isChartVisible: StateFlow<Boolean> = _isChartVisible.asStateFlow()

    // Real-time Gold Spot Data Feed (AUX/USD)
    val goldSpotData = MarketDataService.goldSpotDataFlow

    // Chart Candles for selected pair
    private val _currentCandles = MutableStateFlow<List<Candle>>(emptyList())
    val currentCandles: StateFlow<List<Candle>> = _currentCandles.asStateFlow()

    // Backtest State
    private val _backtestParams = MutableStateFlow(BacktestParams(symbol = "AUX/USD"))
    val backtestParams: StateFlow<BacktestParams> = _backtestParams.asStateFlow()

    private val _isBacktesting = MutableStateFlow(false)
    val isBacktesting: StateFlow<Boolean> = _isBacktesting.asStateFlow()

    private val _backtestResult = MutableStateFlow<BacktestResult?>(null)
    val backtestResult: StateFlow<BacktestResult?> = _backtestResult.asStateFlow()

    // Urdu Analysis State
    private val _dynamicUrduAnalysis = MutableStateFlow("")
    val dynamicUrduAnalysis: StateFlow<String> = _dynamicUrduAnalysis.asStateFlow()

    // Online Status
    private val _isOnlineSynced = MutableStateFlow(true)
    val isOnlineSynced: StateFlow<Boolean> = _isOnlineSynced.asStateFlow()

    init {
        // Initial candles
        updateCandlesForCurrentPair()

        // Run automated online feeds and tick updates
        startMarketDataEngine()

        // Run an initial backtest result so the backtesting tab has instant data
        runBacktest()
    }

    private fun startMarketDataEngine() {
        // 1. Immediate and continuous periodic online real rates fetch
        viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                try {
                    val onlinePrices = MarketDataService.fetchLiveOnlinePrices()
                    _isOnlineSynced.value = onlinePrices.isNotEmpty()
                    onlinePrices.forEach { (symbol, price) ->
                        val pair = TradingPair.find(symbol)
                        val tick = MarketDataService.produceLiveTick(pair, price)
                        repository.updateTick(tick)
                    }
                } catch (_: Exception) {
                    _isOnlineSynced.value = false
                }
                delay(3_000)
            }
        }

        // 2. High frequency real-time MT5 tick producer (every 1s)
        viewModelScope.launch(Dispatchers.Default) {
            while (isActive) {
                TradingPair.ALL_PAIRS.forEach { pair ->
                    val tick = MarketDataService.produceLiveTick(pair)
                    repository.updateTick(tick)
                }

                // Refresh candles for active pair
                val candles = MarketDataService.getCandles(_selectedPair.value.symbol, _selectedTimeframe.value)
                _currentCandles.value = candles

                // Refresh dynamic Urdu analysis
                updateUrduAnalysis(candles)

                delay(1000)
            }
        }
    }

    private fun updateUrduAnalysis(candles: List<Candle>) {
        if (candles.isNotEmpty()) {
            val last = candles.last()
            val pair = _selectedPair.value
            _dynamicUrduAnalysis.value = UrduForexExpertRepository.generateDynamicUrduCommentary(
                pair = pair,
                currentPrice = last.close,
                rsi = last.rsi,
                ema20 = last.ema20,
                ema50 = last.ema50
            )
        }
    }

    fun selectPair(pair: TradingPair) {
        _selectedPair.value = pair
        updateCandlesForCurrentPair()
    }

    fun setTimeframe(tf: String) {
        _selectedTimeframe.value = tf
        updateCandlesForCurrentPair()
    }

    fun toggleCandlestickMode() {
        _isCandlestickMode.value = !_isCandlestickMode.value
    }

    fun toggleEma() { _showEma.value = !_showEma.value }
    fun toggleBollinger() { _showBollinger.value = !_showBollinger.value }
    fun toggleRsi() { _showRsi.value = !_showRsi.value }
    fun toggleChartVisibility() { _isChartVisible.value = !_isChartVisible.value }
    fun setChartVisible(visible: Boolean) { _isChartVisible.value = visible }

    private fun updateCandlesForCurrentPair() {
        val candles = MarketDataService.getCandles(_selectedPair.value.symbol, _selectedTimeframe.value)
        _currentCandles.value = candles
        updateUrduAnalysis(candles)
    }

    fun placeQuickOrder(side: OrderSide, lots: Double, tpPips: Double, slPips: Double) {
        val pair = _selectedPair.value
        val pipMultiplier = if (pair.isGold) 1.0 else 10000.0
        val tick = repository.liveTicks.value[pair.symbol]
        val currentPrice = tick?.price ?: pair.initialPrice

        val tp = if (side == OrderSide.BUY) currentPrice + (tpPips / pipMultiplier) else currentPrice - (tpPips / pipMultiplier)
        val sl = if (side == OrderSide.BUY) currentPrice - (slPips / pipMultiplier) else currentPrice + (slPips / pipMultiplier)

        repository.openPosition(
            symbol = pair.symbol,
            side = side,
            type = OrderType.MARKET,
            lots = lots,
            takeProfit = tp,
            stopLoss = sl,
            trailingStopPips = if (repository.botConfig.value.trailingStop) repository.botConfig.value.trailingStopPips else null,
            strategyTag = "Quick Manual"
        )
    }

    fun closePosition(orderId: String) {
        repository.closePosition(orderId, "Manual Close by Trader")
    }

    fun updateBotConfig(config: BotConfig) {
        repository.updateBotConfig(config)
    }

    fun updateRiskSettings(settings: RiskSettings) {
        repository.updateRiskSettings(settings)
    }

    fun updateBacktestParams(params: BacktestParams) {
        _backtestParams.value = params
    }

    fun runBacktest() {
        viewModelScope.launch(Dispatchers.Default) {
            _isBacktesting.value = true
            delay(500) // Brief calculation animation
            val pair = TradingPair.find(_backtestParams.value.symbol)
            val candles = MarketDataService.getCandles(pair.symbol)
            val result = TechnicalAnalysisEngine.runBacktest(_backtestParams.value, candles, pair)
            _backtestResult.value = result
            _isBacktesting.value = false
            repository.addLog("Backtest Finished for ${pair.symbol}: Win Rate ${String.format("%.1f", result.winRate)}%, Total PnL $${String.format("%.2f", result.totalPnl)}")
        }
    }

    fun updateApiConnection(index: Int, connection: ApiConnection) {
        repository.updateApiConnection(index, connection)
    }

    fun deposit(amountUsd: Double, method: DepositMethod, senderPhone: String = "", trxId: String = ""): Result<DepositTransaction> {
        val result = authRepository.processDeposit(amountUsd, method, senderPhone, trxId)
        result.onSuccess {
            repository.addFunds(amountUsd, "Deposit via ${method.displayName}")
        }
        return result
    }

    fun claimReferralCommission(): Double {
        val claimed = authRepository.claimCommission()
        if (claimed > 0) {
            repository.addFunds(claimed, "10% Referral Commission Claimed")
        }
        return claimed
    }
}
