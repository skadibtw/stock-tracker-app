package com.example.stockexchange

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class LineChartView @JvmOverloads constructor(
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

        paint.color = Color.BLUE
        paint.strokeWidth = 3f
        paint.style = Paint.Style.STROKE

        val points = mutableListOf<PointF>()
        data.forEachIndexed { index, point ->
            val x = padding + (index * (width - 2 * padding) / (data.size - 1).coerceAtLeast(1))
            val y = height - padding - ((point.price - minPrice) / priceRange * (height - 2 * padding)).toFloat()
            points.add(PointF(x, y))
        }

        for (i in 0 until points.size - 1) {
            canvas.drawLine(points[i].x, points[i].y, points[i + 1].x, points[i + 1].y, paint)
        }

        paint.color = Color.RED
        paint.style = Paint.Style.FILL
        points.forEach { canvas.drawCircle(it.x, it.y, 4f, paint) }
    }
}