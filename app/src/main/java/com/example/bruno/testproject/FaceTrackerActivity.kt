package com.example.bruno.testproject

/**
 * Created by bruno on 11/17/16.
 */

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.MultiProcessor
import com.google.android.gms.vision.Tracker
import com.google.android.gms.vision.face.Face
import com.google.android.gms.vision.face.FaceDetector
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.IOException


class FaceTrackerActivity : AppCompatActivity() {

    private var mCameraSource: CameraSource? = null
    private var mPreview: CameraSourcePreview? = null
    private var mGraphicOverlay: GraphicOverlay? = null


    companion object {
        private val TAG = "FaceTracker"
        private val RC_HANDLE_GMS = 9001
        private val RC_HANDLE_CAMERA_PERM = 2
    }


    public override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        setContentView(R.layout.activity_facetracker)

        mPreview = findViewById(R.id.preview) as CameraSourcePreview
        mGraphicOverlay = findViewById(R.id.faceOverlay) as GraphicOverlay

        // Check for the camera permission before accessing the camera.  If the
        // permission is not granted yet, request permission.
        val rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource()
        } else {
            requestCameraPermission()
        }

    }

    private fun requestCameraPermission() {
        Log.w(TAG, "Camera permission is not granted. Requesting permission")

        val permissions = arrayOf(Manifest.permission.CAMERA)

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM)
            return
        }

        val thisActivity = this

        val listener = View.OnClickListener {
            ActivityCompat.requestPermissions(thisActivity, permissions,
                    RC_HANDLE_CAMERA_PERM)
        }

        Snackbar.make(mGraphicOverlay!!, R.string.permission_camera_rationale,
                Snackbar.LENGTH_INDEFINITE)

                .setAction("OK", listener)
                .show()
    }


    private fun createCameraSource() {

        val context = applicationContext
        val detector = FaceDetector.Builder(context)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build()

        detector.setProcessor(
                MultiProcessor.Builder(GraphicFaceTrackerFactory())
                        .build())

        if (!detector.isOperational) {
            Log.w(TAG, "Face detector dependencies are not yet available.")
        }

        mCameraSource = CameraSource.Builder(context, detector)
                .setRequestedPreviewSize(640, 480)
                .setFacing(CameraSource.CAMERA_FACING_FRONT)
                .setRequestedFps(60.0f)
                .build()
    }


    override fun onResume() {
        super.onResume()
        startCameraSource()
    }


    override fun onPause() {
        super.onPause()
        mPreview!!.stop()
    }


    override fun onDestroy() {
        super.onDestroy()
        if (mCameraSource != null) {
            mCameraSource!!.release()
        }
    }

    public override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    public override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: MessageEvent) {
        mCameraSource?.takePicture({}) { image ->
            mPreview?.visibility = View.GONE
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode)
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            return
        }

        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission granted - initialize the camera source")
            // we have permission, so create the camerasource
            createCameraSource()
            return
        }

        Log.e(TAG, "Permission not granted: results len = " + grantResults.size +
                " Result code = " + if (grantResults.isNotEmpty()) grantResults[0] else "(empty)")

        val listener = DialogInterface.OnClickListener { dialog, id -> finish() }

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Face Tracker sample")
                .setMessage(R.string.no_camera_permission)
                .setPositiveButton(R.string.ok, listener)
                .show()
    }

    //==============================================================================================
    // Camera Source Preview
    //==============================================================================================


    private fun startCameraSource() {

        // check that the device has play services available.
        val code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                applicationContext)
        if (code != ConnectionResult.SUCCESS) {
            val dlg = GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS)
            dlg.show()
        }

        if (mCameraSource != null) {
            try {
                mPreview!!.start(mCameraSource!!, mGraphicOverlay!!)
            } catch (e: IOException) {
                Log.e(TAG, "Unable to start camera source.", e)
                mCameraSource!!.release()
                mCameraSource = null
            }

        }
    }


    private inner class GraphicFaceTrackerFactory() : MultiProcessor.Factory<Face> {
        override fun create(face: Face): Tracker<Face> {
            return GraphicFaceTracker(mGraphicOverlay!!)
        }
    }


    private inner class GraphicFaceTracker internal constructor(private val mOverlay: GraphicOverlay) : Tracker<Face>() {
        private val mFaceGraphic: FaceGraphic

        init {
            mFaceGraphic = FaceGraphic(mOverlay)
        }


        override fun onNewItem(faceId: Int, item: Face?) {
            mFaceGraphic.setId(faceId)
        }

        override fun onUpdate(detectionResults: Detector.Detections<Face>?, face: Face?) {
            mOverlay.add(mFaceGraphic)
            mFaceGraphic.updateFace(face!!)
            EventBus.getDefault().post(MessageEvent("Achou a Face"))
        }


        override fun onMissing(detectionResults: Detector.Detections<Face>?) {
            mOverlay.remove(mFaceGraphic)
        }

        override fun onDone() {
            mOverlay.remove(mFaceGraphic)
        }
    }
}
