package com.example.stockexchange

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.max
import kotlin.math.min

class CandleChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var data: List<PricePoint> = emptyList()
    private val padding = 40f

    fun setData(data: List<PricePoint>) {
        this.data = data
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (data.isEmpty()) return

        val width = width.toFloat()
        val height = height.toFloat()

        val minPrice = data.minOf { it.low }
        val maxPrice = data.maxOf { it.high }
        val priceRange = maxPrice - minPrice
        if (priceRange.toFloat() == 0f) return

        paint.color = Color.GRAY
        paint.strokeWidth = 2f
        canvas.drawLine(padding, padding, padding, height - padding, paint)
        canvas.drawLine(padding, height - padding, width - padding, height - padding, paint)

        val candleWidth = (width - 2 * padding) / data.size * 0.6f
        val halfCandle = candleWidth / 2f

        data.forEachIndexed { index, point ->
            val x = padding + (index * (width - 2 * padding) / data.size) + halfCandle

            val openY = getY(point.open, minPrice, priceRange, height)
            val closeY = getY(point.close, minPrice, priceRange, height)
            val highY = getY(point.high, minPrice, priceRange, height)
            val lowY = getY(point.low, minPrice, priceRange, height)

            val color = if (point.close >= point.open) Color.GREEN else Color.RED

            paint.color = color
            paint.strokeWidth = 2f
            canvas.drawLine(x, highY, x, lowY, paint)

            val topY = min(openY, closeY)
            val bottomY = max(openY, closeY)
            paint.style = Paint.Style.FILL
            paint.alpha = 128 // полупрозрачность
            canvas.drawRect(x - halfCandle, topY, x + halfCandle, bottomY, paint)
        }
    }

    private fun getY(price: Double, minPrice: Double, priceRange: Double, height: Float): Float {
        return height - padding - ((price - minPrice) / priceRange * (height - 2 * padding)).toFloat()
    }
}