package com.example.bruno.testproject

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.blundell.woody.Woody
import com.blundell.woody.core.FaceDetectionCamera
import android.R.string.cancel
import android.media.FaceDetector
import android.util.Log
import android.widget.Toast
import com.blundell.woody.Woody.onCreateMonitor



class MainActivity : AppCompatActivity(), Woody.ActivityMonitorListener {

    private var toast: Toast? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Woody.onCreateMonitor(this)
    }

    override fun onFaceDetected() {
        Log.d("XXX", toast("onFaceDetected"))
    }

    override fun onFaceTimedOut() {
        Log.d("XXX", toast("onFaceTimedOut"))
    }

    override fun onFaceDetectionNonRecoverableError() {
        Log.e("XXX", toast("onFaceDetectionNonRecoverableError"))
    }

    private fun toast(msg: String): String {
        toast?.cancel()
        toast = Toast.makeText(this, msg, Toast.LENGTH_SHORT)
        toast?.show()
        return msg
    }


}
