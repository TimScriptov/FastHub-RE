package com.prettifier.pretty

import android.webkit.WebView
import androidx.core.view.NestedScrollingChild
import androidx.core.view.NestedScrollingChildHelper
import kotlin.jvm.JvmOverloads
import androidx.annotation.RequiresApi
import android.os.Build
import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.core.view.ViewCompat
import android.R

open class NestedWebView : WebView, NestedScrollingChild {
    private var mLastY = 0
    private val mScrollOffset = IntArray(2)
    private val mScrollConsumed = IntArray(2)
    private var mNestedOffsetY = 0
    private var mChildHelper: NestedScrollingChildHelper? = null
    private var firstScroll = true

    @JvmOverloads
    constructor(
        context: Context?,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = R.attr.webViewStyle
    ) : super(
        context!!, attrs, defStyleAttr
    ) {
        mChildHelper = NestedScrollingChildHelper(this)
        isNestedScrollingEnabled = true
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(
        context!!, attrs, defStyleAttr, defStyleRes
    )

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        val returnValue: Boolean
        val event = MotionEvent.obtain(ev)
        val action = event.action
        if (action == MotionEvent.ACTION_DOWN) {
            mNestedOffsetY = 0
        }
        val eventY = event.y.toInt()
        event.offsetLocation(0f, mNestedOffsetY.toFloat())
        when (action) {
            MotionEvent.ACTION_MOVE -> {
                var deltaY = mLastY - eventY
                // NestedPreScroll
                if (dispatchNestedPreScroll(0, deltaY, mScrollConsumed, mScrollOffset)) {
                    deltaY -= mScrollConsumed[1]
                    mLastY = eventY - mScrollOffset[1]
                    event.offsetLocation(0f, (-mScrollOffset[1]).toFloat())
                    mNestedOffsetY += mScrollOffset[1]
                }
                returnValue = super.onTouchEvent(event)

                // NestedScroll
                if (dispatchNestedScroll(0, mScrollOffset[1], 0, deltaY, mScrollOffset)) {
                    event.offsetLocation(0f, mScrollOffset[1].toFloat())
                    mNestedOffsetY += mScrollOffset[1]
                    mLastY -= mScrollOffset[1]
                }
            }
            MotionEvent.ACTION_DOWN -> {
                returnValue = super.onTouchEvent(event)
                if (firstScroll) {
                    mLastY = eventY - 5
                    firstScroll = false
                } else {
                    mLastY = eventY
                }
                startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL)
            }
            else -> {
                returnValue = super.onTouchEvent(event)
                // end NestedScroll
                stopNestedScroll()
            }
        }
        return returnValue
    }

    override fun setNestedScrollingEnabled(enabled: Boolean) {
        mChildHelper!!.isNestedScrollingEnabled = enabled
    }

    override fun isNestedScrollingEnabled(): Boolean {
        return mChildHelper!!.isNestedScrollingEnabled
    }

    override fun startNestedScroll(axes: Int): Boolean {
        return mChildHelper!!.startNestedScroll(axes)
    }

    override fun stopNestedScroll() {
        mChildHelper!!.stopNestedScroll()
    }

    override fun hasNestedScrollingParent(): Boolean {
        return mChildHelper!!.hasNestedScrollingParent()
    }

    override fun dispatchNestedScroll(
        dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int,
        offsetInWindow: IntArray?
    ): Boolean {
        return mChildHelper!!.dispatchNestedScroll(
            dxConsumed,
            dyConsumed,
            dxUnconsumed,
            dyUnconsumed,
            offsetInWindow
        )
    }

    override fun dispatchNestedPreScroll(
        dx: Int,
        dy: Int,
        consumed: IntArray?,
        offsetInWindow: IntArray?
    ): Boolean {
        return mChildHelper!!.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow)
    }

    override fun dispatchNestedFling(
        velocityX: Float,
        velocityY: Float,
        consumed: Boolean
    ): Boolean {
        return mChildHelper!!.dispatchNestedFling(velocityX, velocityY, consumed)
    }

    override fun dispatchNestedPreFling(velocityX: Float, velocityY: Float): Boolean {
        return mChildHelper!!.dispatchNestedPreFling(velocityX, velocityY)
    }
}