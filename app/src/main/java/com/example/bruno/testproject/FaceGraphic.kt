package com.example.bruno.testproject

/**
 * Created by bruno on 11/17/16.
 */

import android.graphics.*
import com.google.android.gms.vision.face.Face


/**
 * Graphic instance for rendering face position, orientation, and landmarks within an associated
 * graphic overlay view.
 */
internal class FaceGraphic(overlay: GraphicOverlay) : GraphicOverlay.Graphic(overlay) {

    private val mFacePositionPaint: Paint
    private val mIdPaint: Paint
    private val mBoxPaint: Paint

    @Volatile private var mFace: Face? = null
    private var mFaceId: Int = 0
    private val mFaceHappiness: Float = 0.toFloat()

    companion object {
        private val FACE_POSITION_RADIUS = 10.0f
        private val ID_TEXT_SIZE = 40.0f
        private val ID_Y_OFFSET = 50.0f
        private val ID_X_OFFSET = -50.0f
        private val BOX_STROKE_WIDTH = 5.0f

        private val COLOR_CHOICES = intArrayOf(Color.BLUE, Color.CYAN, Color.GREEN, Color.MAGENTA, Color.RED, Color.WHITE, Color.YELLOW)
        private var mCurrentColorIndex = 0
    }

    init {

        mCurrentColorIndex = (mCurrentColorIndex + 1) % COLOR_CHOICES.size
        val selectedColor = COLOR_CHOICES[mCurrentColorIndex]

        mFacePositionPaint = Paint()
        mFacePositionPaint.color = selectedColor

        mIdPaint = Paint()
        mIdPaint.color = selectedColor
        mIdPaint.textSize = ID_TEXT_SIZE

        mBoxPaint = Paint()
        mBoxPaint.color = selectedColor
        mBoxPaint.style = Paint.Style.STROKE
        mBoxPaint.strokeWidth = BOX_STROKE_WIDTH
        mBoxPaint.pathEffect = DashPathEffect(floatArrayOf(15f, 20f), 0f)
    }

    fun setId(id: Int) {
        mFaceId = id
    }


    /**
     * Updates the face instance from the detection of the most recent frame.  Invalidates the
     * relevant portions of the overlay to trigger a redraw.
     */
    fun updateFace(face: Face) {
        mFace = face
        postInvalidate()
    }

    /**
     * Draws the face annotations for position on the supplied canvas.
     */
    override fun draw(canvas: Canvas) {
        val face = mFace ?: return

        // Draws a circle at the position of the detected face, with the face's track id below.
        val x = translateX(face.position.x + face.width / 2)
        val y = translateY(face.position.y + face.height / 2)

        //canvas.drawCircle(x, y, FACE_POSITION_RADIUS, mFacePositionPaint)
//        canvas.drawText("id: " + mFaceId, x + ID_X_OFFSET, y + ID_Y_OFFSET, mIdPaint)
//        canvas.drawText("happiness: " + String.format("%.2f", face.isSmilingProbability), x - ID_X_OFFSET, y - ID_Y_OFFSET, mIdPaint)
//        canvas.drawText("right eye: " + String.format("%.2f", face.isRightEyeOpenProbability), x + ID_X_OFFSET * 2, y + ID_Y_OFFSET * 2, mIdPaint)
//        canvas.drawText("left eye: " + String.format("%.2f", face.isLeftEyeOpenProbability), x - ID_X_OFFSET * 2, y - ID_Y_OFFSET * 2, mIdPaint)

        // Draws a bounding box around the face.
        val xOffset = scaleX(face.width / 2.0f)
        val yOffset = scaleY(face.height / 2.0f)
        val left = x - xOffset
        val top = y - yOffset
        val right = x + xOffset
        val bottom = y + yOffset
        val rect = RectF(left, top, right, bottom)
        //mFacePositionPaint.style = Paint.Style.STROKE
        canvas.drawOval(rect,mBoxPaint)
        //canvas.drawCircle(x, y, FACE_POSITION_RADIUS*7, mBoxPaint)
        //canvas.drawRect(left, top, right, bottom, mBoxPaint)
    }

}
