@file:Suppress("DEPRECATION")

package com.ycb.camera.widget

import android.content.Context
import android.content.res.Configuration
import android.graphics.Matrix
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.os.Build
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.view.WindowManager
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

/**
 * @desc TODO->自动拍照视图
 */
class AutoPhotoView : TextureView, TextureView.SurfaceTextureListener, View.OnLayoutChangeListener {

    private val mContext: Context
    private val mStartPicture = 0
    private val mPreviewWidth = 640
    private val mPreviewHeight = 480

    private var mWidth = 0f
    private var mHeight = 0f
    private var mImagePath = ""
    private var mImageName = ""
    private var mDisplayWidth = 0
    private var mDisplayHeight = 0
    private var isCanTakePicture = false

    private var mCamera: Camera? = null
    private var mCameraParams: Camera.Parameters? = null
    private var mPhotoCallback: AutoPhotoCallback? = null

    private val mTakePicureHandler = Handler(object : Handler.Callback {
        override fun handleMessage(msg: Message): Boolean {
            when (msg.what) {
                mStartPicture -> {
                    takePicture()
                    return true
                }
            }

            return false
        }
    })

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, def: Int) : super(context, attrs, def) {
        this.mContext = context
        init()
    }

    override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture?, width: Int, height: Int) {
        initCamera()

        val orientation: Int = handleDeform()
        mCamera?.apply {
            parameters = mCameraParams
            setDisplayOrientation(orientation)
            try {
                setPreviewTexture(surfaceTexture)
                startPreview()
                isCanTakePicture = true
            } catch (e: Exception) {
                e.printStackTrace()
            }

            Thread(Runnable {
                Thread.sleep(5 * 1000)
                val msg = Message()
                msg.what = mStartPicture
                mTakePicureHandler.sendMessage(msg)
            }).start()
        }
    }

    override fun onSurfaceTextureSizeChanged(surfaceTexture: SurfaceTexture?, width: Int, height: Int) {
    }

    override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture?) {
    }

    override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture?): Boolean {
        releaseCamera()
        return true
    }

    override fun onLayoutChange(
        v: View?,
        left: Int,
        top: Int,
        right: Int,
        botttom: Int,
        oldLeft: Int,
        oldTop: Int,
        oldRight: Int,
        oldBottom: Int
    ) {
        mWidth = (right - left).toFloat()
        mHeight = (bottom - top).toFloat()
    }

    fun setImagePath(imagePath: String, imageName: String) {
        mImagePath = imagePath
        mImageName = imageName
    }

    fun addPhotoCallback(callback: AutoPhotoCallback) {
        mPhotoCallback = callback
    }

    private fun init() {
        releaseCamera()

        if (mCamera == null) {
            mCamera = Camera.open()
        }

        this.apply {
            surfaceTextureListener = this@AutoPhotoView
        }
    }

    private fun initCamera() {
        mCameraParams = mCamera?.parameters
        mCameraParams?.pictureFormat = PixelFormat.JPEG
        mCameraParams?.flashMode = Camera.Parameters.FLASH_MODE_OFF

        if (Build.MODEL != "KORIDY H30") {
            mCameraParams?.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
        } else {
            mCameraParams?.focusMode = Camera.Parameters.FOCUS_MODE_AUTO
        }

        mCamera?.parameters = mCameraParams
    }

    private fun releaseCamera() {
        if (mCamera != null) {
            mCamera?.apply {
                lock()
                stopPreview()
                release()
            }

            mCamera = null

            isCanTakePicture = true
        }
    }

    /**
     * TODO->处理变形
     *
     * @return
     */
    private fun handleDeform(): Int {
        val previewRect = RectF(0f, 0f, mWidth, mHeight)
        var aspect = (mPreviewWidth / mPreviewHeight).toDouble()
        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            aspect = 1 / aspect
        }

        if (mWidth < (mHeight * aspect)) {
            mDisplayWidth = mWidth.toInt()
            mDisplayHeight = (mHeight * aspect + .5).toInt()
        } else {
            mDisplayWidth = (mWidth / aspect + .5).toInt()
            mDisplayHeight = mHeight.toInt()
        }

        val surfaceDimensions = RectF(0f, 0f, mDisplayWidth.toFloat(), mDisplayHeight.toFloat())
        val matrix = Matrix()
        matrix.setRectToRect(previewRect, surfaceDimensions, Matrix.ScaleToFit.FILL)
        this@AutoPhotoView.setTransform(matrix)

        var displayRotation = 0
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        when (windowManager.defaultDisplay.rotation) {
            Surface.ROTATION_0 -> displayRotation = 90 * 0
            Surface.ROTATION_90 -> displayRotation = 90 * 1
            Surface.ROTATION_180 -> displayRotation = 90 * 2
            Surface.ROTATION_270 -> displayRotation = 90 * 3
        }
        val cameraInfo = Camera.CameraInfo()
        Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, cameraInfo)
        var orientation: Int
        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
            orientation = (cameraInfo.orientation - displayRotation + 360) % 360
        } else {
            orientation = (cameraInfo.orientation + displayRotation) % 360
            orientation = (360 - orientation) % 360
        }
        return orientation
    }

    private fun takePicture() {
        mCamera?.apply {
            takePicture(null, null, Camera.PictureCallback { bytes, camera ->
                val imagesPath = if (mImagePath.isEmpty()) "${mContext.filesDir}/images" else mImagePath

                val imagesDir = File(imagesPath)
                if (!imagesDir.exists()) {
                    imagesDir.mkdirs()
                }

                val imageName = if (mImageName.isEmpty()) "ycb-${System.currentTimeMillis()}.png" else mImageName
                val imageFile = File(imagesPath, imageName)
                imageFile.deleteOnExit()

                val fos: FileOutputStream?
                try {
                    fos = FileOutputStream(imageFile)
                    try {
                        fos.write(bytes)
                        fos.flush()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                    fos.close()
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }


                if (mPhotoCallback != null) {
                    if (imageFile.exists()) {
                        mPhotoCallback?.onPhotoSuc(imageFile.path)
                    } else {
                        mPhotoCallback?.onPhotoFail()
                        camera.startPreview()
                    }
                } else {
                    mPhotoCallback?.onPhotoFail()
                    imageFile.deleteOnExit()
                    camera.startPreview()
                }
            })
        }
    }

    interface AutoPhotoCallback {

        fun onPhotoSuc(picPath: String)

        fun onPhotoFail()
    }
}