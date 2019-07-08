package com.ycb.test

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.ycb.camera.widget.AutoPhotoView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        test.setImagePath("$filesDir/images", "ycb_${System.currentTimeMillis()}.png")
        test.addPhotoCallback(object : AutoPhotoView.AutoPhotoCallback {

            override fun onPhotoSuc(picPath: String) {
                // TODO->拍照成功，开始base64编码
//                finish()
            }

            override fun onPhotoFail() {
                Log.e("TAG", "拍照失败")
            }
        })
    }
}
