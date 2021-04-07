package com.jkjk.doodlewatch.act

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.wear.widget.WearableLinearLayoutManager
import com.jkjk.doodlewatch.R
import com.jkjk.doodlewatch.model.StrokeSize
import kotlinx.android.synthetic.main.act_stroke_pick.*

/**
 *Created by chrisyeung on 26/3/2021.
 */
class StrokePickAct: BaseAct() {

    override val layoutResId: Int
        get() = R.layout.act_stroke_pick

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

    private fun onSizeSelect(size: StrokeSize) {
        setResult(RESULT_OK, Intent().putExtra(EXTRA_SELECT_SIZE, size.size))
        finish()
    }

    inner class StrokeAdapter: RecyclerView.Adapter<StrokePickAct.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(layoutInflater.inflate(R.layout.item_stroke, parent, false))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.size = StrokeSize.values()[position]
            (holder.itemView as? TextView)?.text = holder.size?.text
        }

        override fun getItemCount(): Int {
            return StrokeSize.values().size
        }
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var size: StrokeSize? = null

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
    }
}