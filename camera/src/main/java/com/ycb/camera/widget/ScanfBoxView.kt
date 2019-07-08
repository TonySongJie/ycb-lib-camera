package com.ycb.camera.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import com.xuan.recorder.R

class ScanfBoxView : View {

    private var mMaskColor = 0

    private var mBorderSize = 0
    private var mBorderColor = 0

    private var mCornerColor = 0
    private var mCornerLength = 0
    private var mCornerSize = 0
    private var mHalfCornerSize = 0f

    private var mIsTipTextBelowRect = false
    private var mTipTextMargin = 0
    private var mTipTextSize = 0
    private var mTipTextColor = 0
    private var mTipText = "这是随便写的测试文字"
    private var mTipTextSl: StaticLayout? = null

    private var mScanLineY = 0
    private var mScanLineSize = 0
    private var mScanLineColor = 0

    private var mTopOffset = 0
    private var mRectWidth = 0
    private var mRectHeight = 0
    private var mBarcodeRectHeight = 0
    private var mScanLineMargin = 0

    private var mFramingRect: Rect? = null

    private val mPaint = Paint()
    private val mTipTextPaint = TextPaint()

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, def: Int) : super(context, attrs, def) {
        init(context)

        val typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.ScanfBoxView)
        mMaskColor = typedArray.getColor(R.styleable.ScanfBoxView_sbv_mask_color, Color.parseColor("#33FFFFFF"))

        mBorderColor = typedArray.getColor(R.styleable.ScanfBoxView_sbv_border_color, Color.WHITE)

        mCornerColor = typedArray.getColor(R.styleable.ScanfBoxView_sbv_corner_color, Color.WHITE)

        mTipTextMargin = typedArray.getInt(R.styleable.ScanfBoxView_sbv_tip_text_margin, dp2px(context, 20f))
        mTipTextSize = typedArray.getInt(R.styleable.ScanfBoxView_sbv_tip_text_size, dp2px(context, 14f))
        mTipTextColor = typedArray.getColor(R.styleable.ScanfBoxView_sbv_tip_text_color, Color.WHITE)

        typedArray.recycle()
    }

    override fun onDraw(canvas: Canvas?) {

        if (mFramingRect == null) {
            return
        }

//        drawMask(canvas)
//        drawBorderLine(canvas)
//        drawCornerLine(canvas)
//        drawTipText(canvas)
        drawSanfLine(canvas)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        calFramingRect()
    }

    private fun init(context: Context) {
        mPaint.isAntiAlias = true

        mTipTextPaint.isAntiAlias = true

        mBorderSize = dp2px(context, 1f)

        mCornerLength = dp2px(context, 20f)
        mCornerSize = dp2px(context, 3f)
        mHalfCornerSize = 1.0f * mCornerSize / 2

        mScanLineSize = dp2px(context, 1f)
        mScanLineColor = Color.WHITE

        mTopOffset = dp2px(context, 90f)
        mRectWidth = dp2px(context, 200f)
        mRectHeight = dp2px(context, 200f)
        mBarcodeRectHeight = dp2px(context, 140f)

        mScanLineMargin = 0
    }

    private fun drawMask(canvas: Canvas?) {
        val width = canvas!!.width
        val height = canvas.height

        if (mMaskColor != Color.TRANSPARENT) {
            mPaint.style = Paint.Style.FILL
            mPaint.color = mMaskColor

            // TODO->绘制扫码框上面的mask
            canvas.drawRect(0f, 0f, width.toFloat(), mFramingRect!!.top.toFloat(), mPaint)

            canvas.drawRect(
                0f,
                mFramingRect!!.top.toFloat(),
                mFramingRect!!.left.toFloat(),
                (mFramingRect!!.bottom + 1).toFloat(),
                mPaint
            )
            canvas.drawRect(
                (mFramingRect!!.right + 1).toFloat(), mFramingRect!!.top.toFloat(),
                width.toFloat(),
                (mFramingRect!!.bottom + 1).toFloat(),
                mPaint
            )
            canvas.drawRect(0f, (mFramingRect!!.bottom + 1).toFloat(), width.toFloat(), height.toFloat(), mPaint)
        }
    }

    private fun drawBorderLine(canvas: Canvas?) {
        if (mBorderSize > 0) {
            mPaint.style = Paint.Style.STROKE
            mPaint.color = mBorderColor
            mPaint.strokeWidth = mBorderSize.toFloat()
            canvas?.drawRect(mFramingRect!!, mPaint)
        }
    }

    private fun drawCornerLine(canvas: Canvas?) {
        if (mHalfCornerSize > 0) {
            mPaint.style = Paint.Style.STROKE
            mPaint.color = mCornerColor
            mPaint.strokeWidth = mCornerSize.toFloat()
        }

        // TODO->绘制左上角corner
        canvas!!.drawLine(
            mFramingRect!!.left - mHalfCornerSize,
            mFramingRect!!.top.toFloat(),
            mFramingRect!!.left - mHalfCornerSize + mCornerLength,
            mFramingRect!!.top.toFloat(),
            mPaint
        )
        canvas.drawLine(
            mFramingRect!!.left.toFloat(),
            mFramingRect!!.top - mHalfCornerSize,
            mFramingRect!!.left.toFloat(),
            mFramingRect!!.top - mHalfCornerSize + mCornerLength,
            mPaint
        )

        // TODO->绘制右上角corner
        canvas.drawLine(
            mFramingRect!!.right + mHalfCornerSize,
            mFramingRect!!.top.toFloat(),
            mFramingRect!!.right + mHalfCornerSize - mCornerLength,
            mFramingRect!!.top.toFloat(),
            mPaint
        )
        canvas.drawLine(
            mFramingRect!!.right.toFloat(),
            mFramingRect!!.top - mHalfCornerSize,
            mFramingRect!!.right.toFloat(),
            mFramingRect!!.top - mHalfCornerSize + mCornerLength,
            mPaint
        )

        // TODO->绘制左下角corner
        canvas.drawLine(
            mFramingRect!!.left - mHalfCornerSize,
            mFramingRect!!.bottom.toFloat(),
            mFramingRect!!.left - mHalfCornerSize + mCornerLength,
            mFramingRect!!.bottom.toFloat(),
            mPaint
        )
        canvas.drawLine(
            mFramingRect!!.left.toFloat(), mFramingRect!!.bottom + mHalfCornerSize, mFramingRect!!.left.toFloat(),
            mFramingRect!!.bottom + mHalfCornerSize - mCornerLength, mPaint
        )

        // TODO->绘制右下角corner
        canvas.drawLine(
            mFramingRect!!.right + mHalfCornerSize,
            mFramingRect!!.bottom.toFloat(),
            mFramingRect!!.right + mHalfCornerSize - mCornerLength,
            mFramingRect!!.bottom.toFloat(),
            mPaint
        )
        canvas.drawLine(
            mFramingRect!!.right.toFloat(), mFramingRect!!.bottom + mHalfCornerSize, mFramingRect!!.right.toFloat(),
            mFramingRect!!.bottom + mHalfCornerSize - mCornerLength, mPaint
        )
    }

    private fun drawTipText(canvas: Canvas?) {

        mTipTextPaint.textSize = mTipTextSize.toFloat()
        mTipTextPaint.color = mTipTextColor

        if (mIsTipTextBelowRect) {
            // TODO->绘制在扫描框下面
        } else {
            // TODO->绘制在扫描框上面
            val tipRect = Rect()
            mTipTextPaint.getTextBounds(mTipText, 0, mTipText.length, tipRect)

            mTipTextSl =
                StaticLayout(mTipText, mTipTextPaint, canvas?.width!!, Layout.Alignment.ALIGN_NORMAL, 1.0f, 1.0f, false)

            canvas.save()
            canvas.translate(
                ((canvas.width - tipRect.width()) / 2).toFloat(),
                (mFramingRect!!.bottom + mTipTextMargin).toFloat()
            )
            mTipTextSl?.draw(canvas)
            canvas.restore()
        }

        postInvalidateDelayed(
            1000L,
            mFramingRect!!.left,
            mFramingRect!!.top,
            mFramingRect!!.right,
            mFramingRect!!.bottom
        )
    }

    private fun drawSanfLine(canvas: Canvas?) {
        mPaint.color = Color.WHITE

        if (mScanLineY > (canvas?.height!! - 48)) {
            mScanLineY = 0
        } else {
            mScanLineY += 1
        }

        canvas.drawLine(
            0f,
            mScanLineY.toFloat(),
            canvas.width.toFloat(),
            mScanLineY.toFloat(),
            mPaint
        )
    }

    private fun calFramingRect() {
        val leftOffset = (width - mRectWidth) / 2
        mFramingRect = Rect(leftOffset, mTopOffset, leftOffset + mRectWidth, mTopOffset + mRectHeight)
    }

    private fun dp2px(context: Context, dpValue: Float): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, context.resources.displayMetrics).toInt()
    }
}