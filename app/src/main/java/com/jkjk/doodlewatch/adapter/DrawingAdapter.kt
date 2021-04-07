package com.jkjk.doodlewatch.adapter

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import android.util.Base64
import com.jkjk.doodlewatch.R
import com.jkjk.doodlewatch.act.BaseAct
import com.jkjk.doodlewatch.model.Drawing
import kotlinx.android.synthetic.main.item_drawing.view.*
import java.text.SimpleDateFormat
import java.util.*

/**
 *Created by chrisyeung on 29/3/2021.
 */
class DrawingAdapter(private val act: BaseAct,
                     private val listener: OnDrawingSelectListener): RecyclerView.Adapter<DrawingAdapter.ViewHolder>() {

    private var expandedDrawingId : Int? = null

    private val dateFormatter by lazy {
        SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
    }

    private val timeFormatter by lazy {
        SimpleDateFormat("HH:mm", Locale.ENGLISH)
    }

    var dataList: List<Drawing> = listOf()

    init {
        setHasStableIds(true)
    }

    open inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView), View.OnClickListener {
        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            listener.onCreateSelect()
        }
    }

    inner class DrawingViewHolder(itemView: View) : ViewHolder(itemView) {
        var drawing: Drawing? = null

        init {
            itemView.llDelete?.setOnClickListener(this)
            itemView.llEdit?.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            if (drawing != null) {
                when (v?.id) {
                    R.id.llDelete -> {
                        listener.onDrawingDelete(drawing!!)
                    }
                    R.id.llEdit -> {
                        listener.onDrawingSelect(drawing!!)
                    }
                    else -> {
                        if (expandedDrawingId == drawing?.dbId) {
                            expandedDrawingId = null

                            notifyItemChanged(adapterPosition)

                        } else {
                            var lastExpanded = -1

                            if (expandedDrawingId != null) {
                                val index = dataList.indexOfFirst { it.dbId == expandedDrawingId }
                                if (index != -1) {
                                    lastExpanded = index + 1
                                }
                            }

                            expandedDrawingId = drawing?.dbId

                            if (lastExpanded != -1) {
                                notifyItemChanged(lastExpanded)
                            }
                            notifyItemChanged(adapterPosition)

                        }
                    }
                }
            }
        }
    }

    override fun getItemId(position: Int): Long {
        if (position == 0) {
            return -1L
        } else {
            return dataList[position - 1].dbId.toLong()
        }
    }

    override fun getItemCount(): Int {
        return dataList.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            0
        } else {
            1
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if (viewType == 0) {
            val view = act.layoutInflater.inflate(R.layout.item_create_drawing, parent, false)
            ViewHolder(view)
        } else {
            val view = act.layoutInflater.inflate(R.layout.item_drawing, parent, false)
            DrawingViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (holder is DrawingViewHolder) {
            holder.drawing = dataList[position - 1]

            if (holder.drawing?.dbId == expandedDrawingId) {
                holder.itemView.llDelete?.visibility = View.VISIBLE
                holder.itemView.llEdit?.visibility = View.VISIBLE
                holder.itemView.divider?.visibility = View.VISIBLE
            } else {
                holder.itemView.llDelete?.visibility = View.GONE
                holder.itemView.llEdit?.visibility = View.GONE
                holder.itemView.divider?.visibility = View.GONE
            }

            holder.itemView.txtTime?.text = try {
                timeFormatter.format(Date(holder.drawing?.lastEditOn ?: 0L))
            } catch (e: Exception) {
                e.printStackTrace()
                ""
            }
            holder.itemView.txtDate?.text = try {
                dateFormatter.format(Date(holder.drawing?.lastEditOn ?: 0L))
            } catch (e: Exception) {
                e.printStackTrace()
                ""
            }

            if (holder.drawing?.base64Image?.isNotBlank() == true) {
                Glide.with(act)
                    .asBitmap()
                    .load(Base64.decode(holder.drawing?.base64Image!!, Base64.DEFAULT))
                    .circleCrop()
                    .into(holder.itemView.imgDraw)

                Glide.with(act)
                    .load(ColorDrawable(holder.drawing?.backgroundColor ?: Color.WHITE))
                    .circleCrop()
                    .into(holder.itemView.imgBackground)
            }
        }
    }

    interface OnDrawingSelectListener {
        fun onDrawingSelect(drawing: Drawing)
        fun onDrawingDelete(drawing: Drawing)
        fun onCreateSelect()
    }

}