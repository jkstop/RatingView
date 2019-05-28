package com.github.jkstop.rating_view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View

open class RatingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    var currentRate = 0
        set(value) {
            field = value
            invalidate()
        }

    var itemsCount = 0
        set(value) {
            field = value

            itemsRects.clear()
            for (i in 1..itemsCount) {
                itemsRects.add(Rect())
            }

            requestLayout()
        }

    private var emptyDrawable: Drawable? = null
    private var filledDrawable: Drawable? = null

    private var maxItemSize = Int.MAX_VALUE
    private var itemMiddleDivider = 0
    private var rateChange: (rate: Int) -> Unit = {}
    private val itemsRects = mutableListOf<Rect>()

    private val gestureDetector: GestureDetector by lazy {
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent?): Boolean {
                e?.run {
                    itemsRects
                        .firstOrNull {
                            it.contains(x.toInt(), y.toInt())
                        }
                        ?.run {
                            val rateIndex = itemsRects.indexOf(this) + 1
                            currentRate = if (currentRate == rateIndex) {
                                0
                            } else {
                                rateIndex
                            }

                            rateChange(currentRate)
                        }
                }

                return super.onSingleTapUp(e)
            }
        })
    }

    init {
        context.obtainStyledAttributes(attrs, R.styleable.RatingView)
            .run {
                emptyDrawable = resources.getDrawable(
                    getResourceId(R.styleable.RatingView_rating_view_item_empty, R.drawable.ic_star_border_black_24dp),
                    context.theme
                )
                filledDrawable = resources.getDrawable(
                    getResourceId(R.styleable.RatingView_rating_view_item_filled, R.drawable.ic_star_black_24dp),
                    context.theme
                )
                itemsCount = getInt(R.styleable.RatingView_rating_view_items_count, 5)
                currentRate = getInt(R.styleable.RatingView_rating_view_rate, 0)
                maxItemSize = getDimensionPixelSize(R.styleable.RatingView_rating_view_max_item_size, Int.MAX_VALUE)
                itemMiddleDivider = getDimensionPixelSize(R.styleable.RatingView_rating_view_items_middle_divider, 0)

                recycle()
            }
    }

    fun setOnRateChangeListener(onRateChange: (newRating: Int) -> Unit) {
        rateChange = onRateChange
        setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            return@setOnTouchListener true
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val dividersWidth = itemMiddleDivider * (itemsCount - 1)

        var cellWidth = (measuredWidth - dividersWidth) / itemsCount
        var cellHeight = cellWidth

        if (cellWidth > maxItemSize) cellWidth = maxItemSize
        if (cellHeight > maxItemSize) cellHeight = maxItemSize

        setMeasuredDimension(measuredWidth, cellHeight)

        itemsRects.forEachIndexed { index, rectF ->

            val prevEnd = itemsRects.getOrNull(index - 1)?.right ?: 0

            val currStart = if (index in 1..itemsRects.lastIndex) {
                prevEnd + itemMiddleDivider
            } else {
                prevEnd
            }

            val currEnd = if (index in 1..itemsRects.lastIndex) {
                prevEnd + cellWidth + itemMiddleDivider
            } else {
                prevEnd + cellHeight
            }

            rectF.set(
                currStart,
                0,
                currEnd,
                cellHeight
            )
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        itemsRects.forEachIndexed { index, rect ->
            if (index > currentRate - 1) {
                emptyDrawable?.let { drawable ->
                    drawable.bounds = rect
                    canvas?.let { drawable.draw(it) }
                }
            } else {
                filledDrawable?.let { drawable ->
                    drawable.bounds = rect
                    canvas?.let { drawable.draw(it) }
                }
            }
        }
    }
}