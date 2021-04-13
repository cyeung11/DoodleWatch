package com.jkjk.doodlewatch.act

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.util.Base64
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.wear.widget.ConfirmationOverlay
import com.jkjk.doodlewatch.R
import com.jkjk.doodlewatch.core.act.BaseAct
import com.jkjk.doodlewatch.core.database.AppDatabase
import com.jkjk.doodlewatch.core.model.Drawing
import com.jkjk.doodlewatch.core.model.DrawingHistory
import com.jkjk.doodlewatch.core.model.StrokeSize
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.act_draw.*
import java.io.ByteArrayOutputStream


class DrawActivity : BaseAct(), View.OnClickListener {

    override val layoutResId: Int
        get() = R.layout.act_draw

    private var openingMenu = false

    private var backgroundLuminance = 1.0

    private var currentStroke = StrokeSize.M.size

    private lateinit var drawing : Drawing

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        drawing = intent?.getParcelableExtra(EXTRA_DRAWING) ?: Drawing()

        setupBody()

        Toast.makeText(this, R.string.exit_instruction, Toast.LENGTH_SHORT).show()
    }

    override fun setupBody() {
        btnMenu?.setOnClickListener(this)
        btnUndo?.setOnClickListener(this)
        btnRedo?.setOnClickListener(this)
        btnColor?.setOnClickListener(this)
        btnBackground?.setOnClickListener(this)
        btnStroke?.setOnClickListener(this)
        btnSave?.setOnClickListener(this)

        btnUndo?.isEnabled = false
        btnRedo?.isEnabled = false

        ink?.setStrokeWidth(currentStroke)

        ink.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                btnUndo?.isEnabled = true
            }
            return@setOnTouchListener false
        }

        if (drawing.base64Image?.isNotBlank() == true) {
            try {
                val bArray = Base64.decode(drawing.base64Image!!, Base64.DEFAULT)
                oldImage?.setImageBitmap(BitmapFactory.decodeByteArray(bArray, 0, bArray.size))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        changeBackgroundColor(drawing.backgroundColor)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_CODE_STROKE_COLOR_SELECT -> {
                if (resultCode == RESULT_OK) {
                    val selectedColor = data?.getIntExtra(ColorPickAct.EXTRA_COLOR, Color.BLACK)
                    if (selectedColor != null) {
                        ink?.setColor(selectedColor)
                        btnColor?.imageTintList = ColorStateList.valueOf(selectedColor)
                    }
                }
            }
            REQUEST_CODE_BACKGROUND_COLOR_SELECT -> {
                if (resultCode == RESULT_OK) {
                    val selectedColor = data?.getIntExtra(ColorPickAct.EXTRA_COLOR, Color.BLACK)
                    if (selectedColor != null) {
                        changeBackgroundColor(selectedColor)
                    }
                }
            }
            REQUEST_CODE_STROKE_SELECT -> {
                if (resultCode == RESULT_OK) {
                    val selectedSize = data?.getFloatExtra(
                            StrokePickAct.EXTRA_SELECT_SIZE,
                            StrokeSize.M.size
                    )
                    if (selectedSize != null) {
                        currentStroke = selectedSize
                        ink?.setStrokeWidth(selectedSize)
                    }
                }
            }
            else -> {
                super.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    private fun changeBackgroundColor(color: Int) {
        drawing.backgroundColor = color

        root?.setBackgroundColor(color)
        btnBackground?.imageTintList = ColorStateList.valueOf(color)

        backgroundLuminance = ColorUtils.calculateLuminance(color)

        vgMenu?.setBackgroundColor(
                ContextCompat.getColor(
                        this,
                        if (backgroundLuminance < 0.3) {
                            R.color.transparent_white
                        } else {
                            R.color.transparent_black
                        }
                )
        )
        btnMenu?.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                        this,
                        if (backgroundLuminance < 0.3) R.color.white else R.color.black
                )
        )
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnMenu -> {
                if (openingMenu) {
                    closeMenu()
                } else {
                    openMenu()
                }
            }
            R.id.btnUndo -> {
                ink.undo()
                btnUndo?.isEnabled = ink.canUndo()
                btnRedo?.isEnabled = true
            }
            R.id.btnRedo -> {
                ink.redo()
                btnRedo?.isEnabled = ink.canRedo()
                btnUndo?.isEnabled = true
            }
            R.id.btnColor -> {
                startActivityForResult(
                        Intent(this, ColorPickAct::class.java),
                        REQUEST_CODE_STROKE_COLOR_SELECT
                )
                closeMenu()
            }
            R.id.btnBackground -> {
                startActivityForResult(
                        Intent(this, ColorPickAct::class.java),
                        REQUEST_CODE_BACKGROUND_COLOR_SELECT
                )
                closeMenu()
            }
            R.id.btnStroke -> {
                startActivityForResult(
                        Intent(this, StrokePickAct::class.java)
                                .putExtra(StrokePickAct.EXTRA_CURRENT_SELECT_SIZE, currentStroke),
                        REQUEST_CODE_STROKE_SELECT
                )
                closeMenu()
            }
            R.id.btnSave -> {
                save()
            }
        }
    }

    private fun save() {
        drawing.lastEditOn = System.currentTimeMillis()

        val drawingBitmap = getBitmapFromView(canvas)
        Observable.fromCallable {
            try {
                val bos = ByteArrayOutputStream()
                drawingBitmap.compress(Bitmap.CompressFormat.PNG, 100, bos)
                return@fromCallable Base64.encodeToString(bos.toByteArray(), Base64.DEFAULT)
            } catch (e: Exception) {
                e.printStackTrace()
                return@fromCallable null
            }
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        Consumer {
                            if (it != null) {
                                drawing.base64Image = it

                                val id = AppDatabase.getInstance(this)
                                        .getDrawingDao()
                                        .insert(
                                                drawing
                                        ).toInt()
                                AppDatabase.getInstance(this)
                                    .getDrawingHistoryDao()
                                    .insert(
                                        DrawingHistory(id, drawing.lastEditOn)
                                    )

                                setResult(RESULT_OK, Intent().putExtra(DrawListAct.EXTRA_NEW_DRAWING_ID, id))

                                ConfirmationOverlay()
                                        .setType(ConfirmationOverlay.SUCCESS_ANIMATION)
                                        .setMessage(getString(R.string.doodle_saved))
                                        .setOnAnimationFinishedListener {
                                            finish()
                                        }
                                        .showOn(this)
                            }
                        }
                )

    }

    private fun openMenu() {
        vgMenu?.visibility = View.VISIBLE
        btnMenu?.setImageResource(R.drawable.ic_close)
        btnMenu?.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white))
        openingMenu = true

        btnRedo?.isEnabled = ink.canRedo()
        btnUndo?.isEnabled = ink.canUndo()
    }

    private fun closeMenu() {
        vgMenu?.visibility = View.GONE
        btnMenu?.setImageResource(R.drawable.ic_more)
        btnMenu?.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                        this,
                        if (backgroundLuminance < 0.3) R.color.white else R.color.black
                )
        )
        openingMenu = false
    }

    private fun getBitmapFromView(v: View): Bitmap {
        val screenshot: Bitmap = Bitmap.createBitmap(v.width, v.height, Bitmap.Config.ARGB_8888)
        val c = Canvas(screenshot)
        c.translate((-v.scrollX).toFloat(), (-v.scrollY).toFloat())
        v.draw(c)
        return screenshot
    }


    companion object {
        private const val REQUEST_CODE_STROKE_COLOR_SELECT = 56
        private const val REQUEST_CODE_BACKGROUND_COLOR_SELECT = 57
        private const val REQUEST_CODE_STROKE_SELECT = 58

        const val EXTRA_DRAWING = "EXTRA_DRAWING"
    }
}