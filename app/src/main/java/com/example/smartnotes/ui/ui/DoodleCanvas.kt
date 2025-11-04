package com.smartnotes.ui.ui


import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class DoodleCanvas(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private var path = Path()
    private var paint = Paint().apply {
        color = Color.BLACK
        strokeWidth = 8f
        style = Paint.Style.STROKE
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private lateinit var bitmap: Bitmap
    private lateinit var canvasBitmap: Canvas

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        canvasBitmap = Canvas(bitmap)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawBitmap(bitmap, 0f, 0f, null)
        canvas.drawPath(path, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> path.moveTo(x, y)
            MotionEvent.ACTION_MOVE -> path.lineTo(x, y)
            MotionEvent.ACTION_UP -> {
                canvasBitmap.drawPath(path, paint)
                path.reset()
            }
        }
        invalidate()
        return true
    }

    fun clear() {
        bitmap.eraseColor(Color.WHITE)
        invalidate()
    }

    fun getBitmap(): Bitmap = bitmap
}
