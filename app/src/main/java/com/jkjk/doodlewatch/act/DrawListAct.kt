package com.jkjk.doodlewatch.act

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.wear.activity.ConfirmationActivity
import androidx.wear.widget.WearableLinearLayoutManager
import com.jkjk.doodlewatch.R
import com.jkjk.doodlewatch.adapter.DrawingAdapter
import com.jkjk.doodlewatch.database.AppDatabase
import com.jkjk.doodlewatch.model.Drawing
import kotlinx.android.synthetic.main.act_draw_list.*
import kotlinx.android.synthetic.main.act_stroke_pick.*
import kotlinx.android.synthetic.main.act_stroke_pick.recyclerView
import kotlin.math.max

/**
 *Created by chrisyeung on 26/3/2021.
 */
class DrawListAct: BaseAct(), DrawingAdapter.OnDrawingSelectListener {

    override val layoutResId: Int
        get() = R.layout.act_draw_list

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

    override fun onDrawingSelect(drawing: Drawing) {
        startActivity(
            Intent(this, DrawActivity::class.java)
                .putExtra(DrawActivity.EXTRA_DRAWING, drawing)
        )
    }

    override fun onCreateSelect() {
        startActivity(
            Intent(this, DrawActivity::class.java)
        )
    }

    override fun onDrawingDelete(drawing: Drawing) {
        AlertDialog.Builder(this)
            .setMessage(R.string.delete_confirm)
            .setPositiveButton(R.string.delete, DialogInterface.OnClickListener { _, _ ->
                AppDatabase.getInstance(this)
                    .getDrawingDao()
                    .remove(drawing.dbId)

                startActivity(
                    Intent(this, ConfirmationActivity::class.java).apply {
                        putExtra(
                            ConfirmationActivity.EXTRA_ANIMATION_TYPE,
                            ConfirmationActivity.SUCCESS_ANIMATION
                        )
                        putExtra(ConfirmationActivity.EXTRA_MESSAGE, getString(R.string.doodle_deleted))
                    }
                )
            })
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}