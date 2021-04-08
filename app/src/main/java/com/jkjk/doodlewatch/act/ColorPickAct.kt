package com.jkjk.doodlewatch.act

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.wear.widget.SwipeDismissFrameLayout
import com.jkjk.doodlewatch.R
import com.jkjk.doodlewatch.core.act.BaseAct
import kotlinx.android.synthetic.main.act_color_pick.*

class ColorPickAct : BaseAct() {

    override val layoutResId: Int
        get() = R.layout.act_color_pick

    var paletteColor = Color.BLACK

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupBody()
    }

    override fun setupBody() {

        palette.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val bitmap = (palette.drawable as BitmapDrawable).bitmap

                val actionX = event.x
                val actionY = event.y

                val inverse = Matrix()
                palette.imageMatrix.invert(inverse)
                val touchPoint = floatArrayOf(actionX, actionY)
                inverse.mapPoints(touchPoint)

                val xCoord = touchPoint[0].toInt()
                val yCoord = touchPoint[1].toInt()

                if (xCoord < bitmap.width && yCoord < bitmap.height) {

                    val pixel = bitmap?.getPixel(xCoord, yCoord)

                    if (pixel != null) {
                        val red = Color.red(pixel)
                        val blue = Color.blue(pixel)
                        val green = Color.green(pixel)

                        paletteColor = Color.argb(255, red, green, blue)

                        brightnessSelector.imageTintList = ColorStateList.valueOf(paletteColor)

                        swipeBrightness?.visibility = View.VISIBLE
                    }
                }
                v.performClick()
            }
            return@setOnTouchListener true
        }

        brightnessSelector.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val percentage = event.x / brightnessSelector.width

                    val result = Color.argb(
                            255,
                            (Color.red(paletteColor) * percentage).toInt(),
                            (Color.green(paletteColor) * percentage).toInt(),
                            (Color.blue(paletteColor) * percentage).toInt()
                    )
                    setResult(RESULT_OK, Intent().putExtra(EXTRA_COLOR, result))
                    finish()

                v.performClick()
            }
            return@setOnTouchListener true
        }

        swipeBrightness?.addCallback(object : SwipeDismissFrameLayout.Callback(){
            override fun onDismissed(layout: SwipeDismissFrameLayout?) {
                swipeBrightness?.visibility = View.GONE
            }
        })
    }

    companion object {
        const val EXTRA_COLOR = "selected_color"
    }
}