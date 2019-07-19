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
    private var mTipText = ""
    private var mTipTextSl: StaticLayout? = null

    private var mRectTopOffset = 0
    private var mRectWidth = 0
    private var mRectHeight = 0
    private var mBarcodeRectHeight = 0

    private var mScanfLineStartY = 1f
    private var mScanfLineSize = 1
    private var mScanfLineMarginTop = 0
    private var mScanfLineMarginX = 0
    private var mScanfLineColor = Color.parseColor("#FFFFFFFF")

    private var mFramingRect: Rect? = null

    private var isScreenFull = false

    private val mPaint = Paint()
    private val mTipTextPaint = TextPaint()

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, def: Int) : super(context, attrs, def) {
        init(context)

        val typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.ScanfBoxView)
        mMaskColor = typedArray.getColor(R.styleable.ScanfBoxView_sbv_mask_color, Color.parseColor("#33FFFFFF"))
        mRectTopOffset =
            typedArray.getDimensionPixelOffset(R.styleable.ScanfBoxView_sbv_top_offset, dp2px(context, 200f))

        mRectWidth =
            typedArray.getDimensionPixelOffset(R.styleable.ScanfBoxView_sbv_box_width, dp2px(context, 320f))
        mRectHeight =
            typedArray.getDimensionPixelOffset(R.styleable.ScanfBoxView_sbv_box_height, dp2px(context, 200f))

        mBorderColor = typedArray.getColor(R.styleable.ScanfBoxView_sbv_border_color, Color.WHITE)
        mBorderSize = typedArray.getDimensionPixelOffset(R.styleable.ScanfBoxView_sbv_border_size, dp2px(context, 1f))

        mCornerColor = typedArray.getColor(R.styleable.ScanfBoxView_sbv_corner_color, Color.WHITE)
        mCornerSize = typedArray.getDimensionPixelOffset(R.styleable.ScanfBoxView_sbv_corner_size, dp2px(context, 2f))

        mIsTipTextBelowRect = typedArray.getBoolean(R.styleable.ScanfBoxView_sbv_tip_text_showboxup, false)
        mTipText = typedArray.getString(R.styleable.ScanfBoxView_sbv_tip_text) ?: "提示文字"
        mTipTextMargin =
            typedArray.getDimensionPixelOffset(R.styleable.ScanfBoxView_sbv_tip_text_margin, dp2px(context, 0f))
        mTipTextSize =
            typedArray.getDimensionPixelOffset(R.styleable.ScanfBoxView_sbv_tip_text_size, dp2px(context, 14f))
        mTipTextColor = typedArray.getColor(R.styleable.ScanfBoxView_sbv_tip_text_color, Color.WHITE)

        mScanfLineSize =
            typedArray.getDimensionPixelOffset(R.styleable.ScanfBoxView_sbv_scanf_line_size, 1)
        mScanfLineColor = typedArray.getColor(R.styleable.ScanfBoxView_sbv_scanf_line_color, Color.WHITE)
        mScanfLineMarginTop =
            typedArray.getDimensionPixelOffset(R.styleable.ScanfBoxView_sbv_scanf_line_margin_top, dp2px(context, 24f))
        mScanfLineMarginX =
            typedArray.getDimensionPixelOffset(R.styleable.ScanfBoxView_sbv_scanf_line_margin_x, dp2px(context, 16f))

        isScreenFull = typedArray.getBoolean(R.styleable.ScanfBoxView_sbv_screen_full, false)

        typedArray.recycle()
    }

    override fun onDraw(canvas: Canvas?) {

        if (mFramingRect == null) {
            return
        }

        if (isScreenFull) {
            drawScreenFullScanfLine(canvas)
        } else {
            drawMask(canvas)
            drawBorderLine(canvas)
            drawCornerLine(canvas)
            drawTipText(canvas)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        calFramingRect()
    }

    fun setScreenFull(isScreenFull: Boolean) {
        this.isScreenFull = isScreenFull
        invalidate()
    }

    private fun init(context: Context) {
        mPaint.isAntiAlias = true

        mTipTextPaint.isAntiAlias = true

        mCornerLength = dp2px(context, 20f)
        mCornerSize = dp2px(context, 3f)
        mHalfCornerSize = 1.0f * mCornerSize / 2

        mRectHeight = dp2px(context, 200f)
        mBarcodeRectHeight = dp2px(context, 140f)

        mScanfLineStartY = mScanfLineMarginTop.toFloat()
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
            // TODO->绘制在扫描框上面
            val tipRect = Rect()
            mTipTextPaint.getTextBounds(mTipText, 0, mTipText.length, tipRect)

            mTipTextSl =
                StaticLayout(mTipText, mTipTextPaint, canvas?.width!!, Layout.Alignment.ALIGN_NORMAL, 1.0f, 1.0f, false)

            canvas.save()
            canvas.translate(
                ((canvas.width - tipRect.width()) / 2).toFloat(),
                (mFramingRect!!.top - mTipTextMargin - (mTipTextSl?.height ?: 0)).toFloat()
            )
            mTipTextSl?.draw(canvas)
            canvas.restore()
        } else {
            // TODO->绘制在扫描框下面
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

    private fun drawScreenFullScanfLine(canvas: Canvas?) {
        mPaint.style = Paint.Style.FILL
        mPaint.color = mScanfLineColor
        mPaint.strokeWidth = mScanfLineSize.toFloat()
        canvas?.drawLine(
            mScanfLineMarginX.toFloat(),
            mScanfLineStartY,
            width.toFloat() - mScanfLineMarginX,
            mScanfLineStartY,
            mPaint
        )

        if (mScanfLineStartY < height)
            mScanfLineStartY += (height - mScanfLineMarginTop) / 200
        else
            mScanfLineStartY = mScanfLineMarginTop.toFloat()

        postInvalidate()
    }

    private fun calFramingRect() {
        val leftOffset = (width - mRectWidth) / 2
        mFramingRect = Rect(leftOffset, mRectTopOffset, leftOffset + mRectWidth, mRectTopOffset + mRectHeight)
    }

    private fun dp2px(context: Context, dpValue: Float): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, context.resources.displayMetrics).toInt()
    }
}