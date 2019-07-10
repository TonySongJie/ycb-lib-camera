package com.ycb.test

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.ycb.camera.widget.ScanfBaiduAiView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ibtnMainStartScanf.setOnClickListener {
            test.takePicture()
        }

        test.setImagePath("$filesDir/images", "ycb_${System.currentTimeMillis()}.png")
        test.addPhotoCallback(object : ScanfBaiduAiView.TakePictureCallback {

            override fun onTakePictureSuc(picPath: String) {
                // TODO->拍照成功，开始base64编码
                finish()
            }

            override fun onTakePictureFail() {
                Log.e("TAG", "拍照失败")
            }
        })
    }
}
