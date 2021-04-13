package com.jkjk.doodlewatch.act

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.wear.widget.ConfirmationOverlay
import androidx.wear.widget.WearableLinearLayoutManager
import com.jkjk.doodlewatch.R
import com.jkjk.doodlewatch.adapter.DrawingAdapter
import com.jkjk.doodlewatch.core.act.CommunicationAct
import com.jkjk.doodlewatch.core.database.AppDatabase
import com.jkjk.doodlewatch.core.model.Drawing
import com.jkjk.doodlewatch.core.model.DrawingHistory
import kotlinx.android.synthetic.main.act_draw_list.*
import kotlinx.android.synthetic.main.act_stroke_pick.recyclerView
import kotlin.math.max

/**
 *Created by chrisyeung on 26/3/2021.
 */
class DrawListAct: CommunicationAct(), DrawingAdapter.OnDrawingSelectListener {

    override val layoutResId: Int
        get() = R.layout.act_draw_list

    private var drawingToRename: Drawing? = null

    private val adapter: DrawingAdapter by lazy {
        DrawingAdapter(this, this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupBody()
    }

    override fun setupBody() {
        recyclerView?.layoutManager = WearableLinearLayoutManager(this)
        recyclerView?.isEdgeItemsCenteringEnabled = true
        recyclerView?.adapter = adapter

        recyclerView?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val scrollY = recyclerView.computeVerticalScrollOffset()
                if (scrollY > 100) {
                    txtAppTitle?.visibility = View.GONE
                } else {
                    txtAppTitle?.visibility = View.VISIBLE
                    txtAppTitle?.alpha =  max(0f, (100 - scrollY) / 50f)
                }
            }
        })

        AppDatabase.getInstance(this)
            .getDrawingDao()
            .getAllAsync()
            .observe(this) {
                if (it != null) {
                    adapter.dataList = it
                    adapter.notifyDataSetChanged()
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_CODE_NEW_DRAWING -> {
                if (resultCode == RESULT_OK) {
                    val newId = data?.getIntExtra(EXTRA_NEW_DRAWING_ID, -1) ?: -1
                    if (newId != -1) {
                        drawingDao.getSync(newId)?.let {
                            // Sync to other devices
                            dataClient.putDataItem(
                                    createDrawingSendRequest(it)
                            )
                        }
                    }
                }
            }
            REQUEST_CODE_RENAME -> {
                if (drawingToRename != null) {
                    if (resultCode == RESULT_OK) {
                        val newName = data?.getStringExtra(InputAct.EXTRA_INPUT)
                        if (newName?.isNotBlank() == true) {
                            drawingToRename?.name = newName
                            drawingToRename?.lastEditOn = System.currentTimeMillis()
                            drawingDao.insert(drawingToRename!!)
                            // Sync to other devices
                            dataClient.putDataItem(
                                    createDrawingSendRequest(drawingToRename!!)
                            )
                        }
                    }
                    drawingToRename = null
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
    
    override fun onDrawingSelect(drawing: Drawing) {
        startActivity(
            Intent(this, DrawActivity::class.java)
                .putExtra(DrawActivity.EXTRA_DRAWING, drawing)
        )
    }

    override fun onCreateSelect() {
        startActivityForResult(
            Intent(this, DrawActivity::class.java),
            REQUEST_CODE_NEW_DRAWING
        )
    }

    override fun onDrawingDelete(drawing: Drawing) {
        AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
            .setMessage(R.string.delete_confirm)
            .setPositiveButton(R.string.delete, DialogInterface.OnClickListener { _, _ ->
                AppDatabase.getInstance(this)
                    .getDrawingDao()
                    .remove(drawing.dbId)
                AppDatabase.getInstance(this)
                    .getDrawingHistoryDao()
                    .insert(DrawingHistory(drawing.dbId, deletedOn = System.currentTimeMillis()))
                sendDrawingSyncInfo(this, null)

                ConfirmationOverlay()
                    .setType(ConfirmationOverlay.SUCCESS_ANIMATION)
                    .setMessage(getString(R.string.doodle_deleted))
                    .showOn(this)

            })
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    override fun onDrawingRename(drawing: Drawing) {
        drawingToRename = drawing
        startActivityForResult(
                Intent(this, InputAct::class.java)
                        .putExtra(InputAct.EXTRA_INPUT, drawing.name),
                REQUEST_CODE_RENAME
        )
    }

    companion object {
        private const val REQUEST_CODE_NEW_DRAWING = 546
        private const val REQUEST_CODE_RENAME = 547

        const val EXTRA_NEW_DRAWING_ID = "EXTRA_NEW_DRAWING_ID"
    }
}