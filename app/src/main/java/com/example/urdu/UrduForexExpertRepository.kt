package com.example.urdu

import com.example.model.TradingPair
import com.example.model.UrduExpertPost

object UrduForexExpertRepository {

    val EXPERT_POSTS = listOf(
        UrduExpertPost(
            id = "urdu_exp_1",
            expertNameUrdu = "ملک سفیان علی (سینئر گولڈ اینالسٹ)",
            expertRankUrdu = "ماہر ٹیکنیکل اسٹریٹجسٹ • تجربہ 11 سال",
            pair = "AUX/USD",
            actionUrdu = "مضبوط خریداری (STRONG BUY)",
            isBullish = true,
            entryPrice = 2682.00,
            takeProfit = 2715.00,
            stopLoss = 2665.00,
            timeframe = "H4 / Daily",
            titleUrdu = "سونے (AUX/USD) میں تیز رفتار تیزی: مزاحمتی سطح 2700 کی طرف پیش قدمی",
            detailedAnalysisUrdu = "سونے کی قیمت 2675 کی بنیادی سپورٹ سطح کو کامیابی سے ٹیسٹ کرنے کے بعد بلش فلیگ پیٹرن تشکیل دے رہی ہے۔ روزانہ کے چارٹ پر 50-EMA سے واضح ریجیکشن دیکھنے میں آئی ہے۔ ہمارا خودکار بوٹ آر ایس آئی (RSI) ڈائیورجنس پر خریداری کے سگنلز موصول کر رہا ہے۔ جب تک قیمت 2665 کے اوپر برقرار ہے، مارکیٹ کا جھکاؤ واضح طور پر خریداروں کے حق میں ہے۔",
            keySupportResistanceUrdu = "سپورٹ: 2665 | 2650 ڈالر\nمزاحمت: 2700 | 2725 ڈالر",
            winRatePercent = 89,
            timestampAgo = "15 منٹ پہلے"
        ),
        UrduExpertPost(
            id = "urdu_exp_2",
            expertNameUrdu = "طارق محمود (چیف فاریکس اسٹریٹجسٹ)",
            expertRankUrdu = "ای سی بی اور ایف او ایم سی اسپیشلسٹ",
            pair = "EUR/USD",
            actionUrdu = "محتاط فروخت (SELL LIMIT)",
            isBullish = false,
            entryPrice = 1.0875,
            takeProfit = 1.0790,
            stopLoss = 1.0920,
            timeframe = "H1",
            titleUrdu = "یورو بنام ڈالر: بیرش ہیڈ اینڈ شولڈرز پیٹرن اور ممکنہ تنزلی",
            detailedAnalysisUrdu = "یورپی مرکزی بینک کی نرم مانیٹری پالیسی کے بیانات کے بعد یورو پر دباؤ مسلسل بڑھ رہا ہے۔ 1 گھنٹے کے ٹائم فریم پر ہیڈ اینڈ شولڈرز پیٹرن کی نیک لائن بریک ہو چکی ہے۔ ری ٹیسٹ پر سیل پوزیشن کا بہترین موقع ہے۔ اسٹاپ لاس 1.0920 پر سخت رکھیں۔",
            keySupportResistanceUrdu = "سپورٹ: 1.0810 | 1.0780\nمزاحمت: 1.0890 | 1.0930",
            winRatePercent = 84,
            timestampAgo = "45 منٹ پہلے"
        ),
        UrduExpertPost(
            id = "urdu_exp_3",
            expertNameUrdu = "رضوان الحق (میکرو اکنامکس ریسرچر)",
            expertRankUrdu = "کیپیٹل مارکیٹ ریسرچ ہیڈ",
            pair = "GBP/USD",
            actionUrdu = "بریک آؤٹ خریداری (BUY STOP)",
            isBullish = true,
            entryPrice = 1.3010,
            takeProfit = 1.3140,
            stopLoss = 1.2940,
            timeframe = "H4",
            titleUrdu = "برطانوی پاؤنڈ: مہنگائی ڈیٹا کے بعد بولنگر بینڈ بریک آؤٹ",
            detailedAnalysisUrdu = "برطانیہ کے سروسز پی ایم آئی اور افراط زر کے اعداد و شمار متوقع سطح سے بہتر آئے ہیں جس سے بینک آف انگلینڈ کی شرح سود میں کمی کا امکان کم ہو گیا ہے۔ پاؤنڈ 1.2980 کے اوپر مستحکم ہو رہا ہے۔ 1.3010 کے بریک آؤٹ پر فوری بائے بوٹ فعال کریں تاکہ 130 پپس کا ٹارگٹ حاصل کیا جا سکے۔",
            keySupportResistanceUrdu = "سپورٹ: 1.2940 | 1.2890\nمزاحمت: 1.3080 | 1.3150",
            winRatePercent = 86,
            timestampAgo = "2 گھنٹے پہلے"
        ),
        UrduExpertPost(
            id = "urdu_exp_4",
            expertNameUrdu = "انجینئر بلال حیدر (کوانٹ ٹریڈنگ ایکسپرٹ)",
            expertRankUrdu = "الگوریتھمک اور بوٹ اسٹریٹجی ڈویلپر",
            pair = "USD/JPY",
            actionUrdu = "اوور باٹ ریورسل (SELL)",
            isBullish = false,
            entryPrice = 153.20,
            takeProfit = 150.50,
            stopLoss = 154.10,
            timeframe = "Daily",
            titleUrdu = "ین کی ممکنہ بحالی: بینک آف جاپان کی مالیاتی مداخلت کا شدید خطرہ",
            detailedAnalysisUrdu = "امریکی ڈالر بنام جاپانی ین 153.00 کی نفسیاتی اور انتہائی نازک سطح کو چھو چکا ہے۔ پچھلی بار بھی اس سطح پر جاپانی وزارت خزانہ نے براہ راست کرنسی مارکیٹ میں مداخلت کر کے ین کو خریدا تھا۔ یومیہ آر ایس آئی 76 سے اوپر ہے جو شدید اوور باٹ کو ظاہر کرتی ہے۔ گرڈ اسکیلپر بوٹ کے لیے نچلی رینج کا تعین کریں۔",
            keySupportResistanceUrdu = "سپورٹ: 151.20 | 149.80\nمزاحمت: 153.80 | 154.50",
            winRatePercent = 81,
            timestampAgo = "3 گھنٹے پہلے"
        )
    )

    val URDU_GLOSSARY = listOf(
        Pair("پِپ (Pip) کیا ہے؟", "فاریکس ٹریڈنگ میں کرنسی کے ریٹ میں سب سے چھوٹی پیمائش کو پِپ کہتے ہیں۔ عام طور پر یہ چوتھے ڈیسیمل (0.0001) کے برابر ہوتا ہے۔ سونے میں 1 سینٹ یا 10 سینٹ کو پِپ گنا جاتا ہے۔"),
        Pair("لاٹ سائز (Lot Size) اور رسک", "معیاری لاٹ (Standard Lot = 1.0) میں 100,000 یونٹس ہوتے ہیں۔ منی لاٹ (Mini = 0.1) اور مائیکرو لاٹ (Micro = 0.01) نئے اور محتاط تاجروں کے لیے محفوظ ہیں تاکہ کیپیٹل ڈرا ڈاؤن نہ ہو۔"),
        Pair("اسٹاپ لاس (Stop Loss) کی اہمیت", "کسی بھی ٹریڈ میں غیر متوقع نقصان کو محدود کرنے کے لیے اسٹاپ لاس سب سے مؤثر ہتھیار ہے۔ ہمارا خودکار بوٹ ہمیشہ ٹریلنگ اسٹاپ لاس کو ترجیح دیتا ہے۔"),
        Pair("لیوریج (Leverage) کے اثرات", "لیوریج آپ کو کم سرمائے کے ساتھ بڑی پوزیشن کھولنے کی اجازت دیتی ہے، لیکن یہ نفع کے ساتھ ساتھ نقصان کے تناسب کو بھی بڑھا دیتی ہے۔ 1:100 سے زیادہ رسک خطرناک ہے۔"),
        Pair("آٹومیٹک ٹریڈنگ بوٹ کا طریقہ کار", "بوٹ انسانی جذبات جیسے خوف اور لالچ سے پاک ہو کر تکنیکی اشاریوں (EMA Cross, RSI) کے عین مطابق سیکنڈوں میں سودے طے کرتا ہے۔")
    )

    fun generateDynamicUrduCommentary(pair: TradingPair, currentPrice: Double, rsi: Double, ema20: Double, ema50: Double): String {
        val trendUrdu = if (currentPrice > ema20) "بلش (تیزی)" else "بیرش (مندی)"
        val rsiStatusUrdu = when {
            rsi > 70 -> "اوور باٹ (قیمت ضرورت سے زیادہ بڑھ چکی ہے، نیچے آنے کا امکان)"
            rsi < 30 -> "اوور سولڈ (قیمت کم ترین سطح پر ہے، اچھال متوقع ہے)"
            else -> "معتدل زون (${rsi.toInt()})"
        }
        val emaCrossUrdu = if (ema20 > ema50) "گولڈن کراس (مضبوط بلش تسلسل)" else "ڈیتھ کراس (مندی کا دباؤ)"

        return """
            📊 ${pair.urduName} کا لائیو تکنیکی تجزیہ:
            موجودہ لائیو قیمت: $currentPrice ڈالر
            مارکیٹ کا موجودہ رجحان: $trendUrdu
            اشاریہ آر ایس آئی (RSI-14): $rsiStatusUrdu
            حرکت پزیر اوسط (EMA 20/50): $emaCrossUrdu
            
            💡 بوٹ کے لیے ماہرین کی تجویز:
            ${if (currentPrice > ema20 && rsi in 35.0..68.0) 
                "مارکیٹ تیزی میں ہے۔ بائے آرڈر اور ٹریلنگ اسٹاپ لاس 20 پپس کا استعمال کریں۔" 
            else if (rsi > 70) 
                "اوور باٹ زون میں خریدار محتاط رہیں۔ ریورسل سیل سگنل کا انتظار کریں۔" 
            else if (rsi < 30) 
                "سونے / کرنسی میں سپورٹ زون سے بائے انٹری لے سکتے ہیں۔ اسٹاپ لاس مت بھولیں۔" 
            else 
                "رینج مارکیٹ ہے۔ گرڈ اسکیلپر بوٹ آن کرنا سب سے زیادہ منافع بخش رہے گا۔"}
        """.trimIndent()
    }
}
