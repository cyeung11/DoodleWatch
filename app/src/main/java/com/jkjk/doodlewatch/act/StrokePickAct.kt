package com.jkjk.doodlewatch.act

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.wear.widget.WearableLinearLayoutManager
import com.jkjk.doodlewatch.R
import com.jkjk.doodlewatch.core.act.BaseAct
import kotlinx.android.synthetic.main.act_stroke_pick.*
import kotlinx.android.synthetic.main.item_stroke.view.*

/**
 *Created by chrisyeung on 26/3/2021.
 */
class StrokePickAct: BaseAct() {

    override val layoutResId: Int
        get() = R.layout.act_stroke_pick

    private var currentStrokeSize: Float? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        currentStrokeSize = intent?.getFloatExtra(EXTRA_CURRENT_SELECT_SIZE, com.jkjk.doodlewatch.core.model.StrokeSize.M.size) ?: com.jkjk.doodlewatch.core.model.StrokeSize.M.size

        setupBody()
    }

    override fun setupBody() {
        recyclerView?.layoutManager = WearableLinearLayoutManager(this)
        recyclerView?.isEdgeItemsCenteringEnabled = true
        recyclerView?.adapter = StrokeAdapter()

        recyclerView?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val scrollY = recyclerView.computeVerticalScrollOffset()
                if (scrollY > 100) {
                    txtTitle?.visibility = View.GONE
                } else {
                    txtTitle?.visibility = View.VISIBLE
                    txtTitle?.alpha =  Math.max(0f, (100 - scrollY) / 50f)
                }
            }
        })
    }

    private fun onSizeSelect(size: com.jkjk.doodlewatch.core.model.StrokeSize) {
        setResult(RESULT_OK, Intent().putExtra(EXTRA_SELECT_SIZE, size.size))
        finish()
    }

    inner class StrokeAdapter: RecyclerView.Adapter<StrokePickAct.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(layoutInflater.inflate(R.layout.item_stroke, parent, false))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.size = com.jkjk.doodlewatch.core.model.StrokeSize.values()[position]
            holder.itemView.txtSize?.text = holder.size?.text

            if (holder.size?.size == currentStrokeSize) {
                holder.itemView.txtSize?.typeface = Typeface.DEFAULT_BOLD
                holder.itemView.imgSelected?.visibility = View.VISIBLE
            } else {
                holder.itemView.txtSize?.typeface = Typeface.DEFAULT
                holder.itemView.imgSelected?.visibility = View.GONE
            }
        }


        override fun getItemCount(): Int {
            return com.jkjk.doodlewatch.core.model.StrokeSize.values().size
        }
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var size: com.jkjk.doodlewatch.core.model.StrokeSize? = null

        init {
            itemView.setOnClickListener {
                size?.let {
                    onSizeSelect(it)
                }
            }
        }
    }

    companion object {
        const val EXTRA_SELECT_SIZE = "selected_size"
        const val EXTRA_CURRENT_SELECT_SIZE = "current_selected_size"
    }
}