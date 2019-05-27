package com.github.jkstop.rating_view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View

class RatingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet?
) : View(context, attrs) {

    var currentRate = 0
        set(value) {
            field = value
            invalidate()
        }

    var stars = 0
        set(value) {
            field = value

            starsRects.clear()
            for (i in 1..stars) {
                starsRects.add(Rect())
            }

            requestLayout()
        }

    private var emptyDrawable: Drawable? = null
    private var filledDrawable: Drawable? = null

    private var maxStarSize = Int.MAX_VALUE
    private var starMiddleDivider = 0
    private var tappable = true
    private var rateChange: (rate: Int) -> Unit = {}

    private val starsRects = mutableListOf<Rect>()

    private val gestureDetector: GestureDetector by lazy {
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent?): Boolean {
                e?.run {
                    starsRects
                        .firstOrNull {
                            it.contains(x.toInt(), y.toInt())
                        }
                        ?.run {
                            val rateIndex = starsRects.indexOf(this) + 1
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
                emptyDrawable = resources.getDrawable(getResourceId(R.styleable.RatingView_rating_view_star_empty, 0), context.theme)
                filledDrawable = resources.getDrawable(getResourceId(R.styleable.RatingView_rating_view_star_filled, 0), context.theme)
                stars = getInt(R.styleable.RatingView_rating_view_stars_count, 0)
                currentRate = getInt(R.styleable.RatingView_rating_view_rate, 0)
                maxStarSize = getDimensionPixelSize(R.styleable.RatingView_rating_view_max_star_size, Int.MAX_VALUE)
                starMiddleDivider = getDimensionPixelSize(R.styleable.RatingView_rating_view_stars_middle_divider, 0)
                tappable = getBoolean(R.styleable.RatingView_rating_view_tappable, true)

                recycle()
            }

        if (tappable) {
            setOnTouchListener { v, event ->
                gestureDetector.onTouchEvent(event)
                return@setOnTouchListener true
            }
        }
    }

    fun setOnRateChangeListener(onRateChange: (newRating: Int) -> Unit) {
        rateChange = onRateChange
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val dividersWidth = starMiddleDivider * (stars - 1)

        var cellWidth = (measuredWidth - dividersWidth) / stars
        var cellHeight = cellWidth

        if (cellWidth > maxStarSize) cellWidth = maxStarSize
        if (cellHeight > maxStarSize) cellHeight = maxStarSize

        setMeasuredDimension(measuredWidth, cellHeight)

        starsRects.forEachIndexed { index, rectF ->

            val prevEnd = starsRects.getOrNull(index - 1)?.right ?: 0

            val currStart = if (index in 1..starsRects.lastIndex) {
                prevEnd + starMiddleDivider
            } else {
                prevEnd
            }

            val currEnd = if (index in 1..starsRects.lastIndex) {
                prevEnd + cellWidth + starMiddleDivider
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
        starsRects.forEachIndexed { index, rect ->
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