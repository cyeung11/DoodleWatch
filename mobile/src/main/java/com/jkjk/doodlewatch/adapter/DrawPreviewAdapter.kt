package com.jkjk.doodlewatch.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import android.util.Base64
import com.jkjk.doodlewatch.R
import com.jkjk.doodlewatch.core.act.BaseAct
import com.jkjk.doodlewatch.core.model.Drawing
import kotlinx.android.synthetic.main.item_draw_preview.view.*
import java.text.SimpleDateFormat
import java.util.*

/**
 *Created by chrisyeung on 7/4/2021.
 */
class DrawPreviewAdapter(private val act: BaseAct): GridRecyclerViewAdapter<DrawPreviewAdapter.ViewHolder>(act) {

    private val dateTimeFormatter by lazy {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ENGLISH)
    }

    var drawingList: List<Drawing> = listOf()

    inner class ViewHolder(itemView: View): GridRecyclerViewAdapter.ViewHolder(itemView) {

        var drawing: Drawing? = null

//        init {
//            itemView.setOnClickListener(this)
//        }
//
//        override fun onClick(v: View?) {
//            if (drawing != null) {
//
//            }
//        }
    }

    override fun getLayoutId(viewType: Int): Int {
        return R.layout.item_draw_preview
    }

    override fun getItemCount(): Int {
        return drawingList.size
    }

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(viewGroup)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.drawing = drawingList[position]
        holder.itemView.txtDateTime?.text = try {
            dateTimeFormatter.format(Date(holder.drawing?.lastEditOn ?: 0L))
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
        holder.itemView.txtName?.text = holder.drawing?.name

        if (holder.drawing?.base64Image?.isNotBlank() == true) {
            Glide.with(act)
                .asBitmap()
                .load(Base64.decode(holder.drawing?.base64Image!!, Base64.DEFAULT))
                .circleCrop()
                .into(holder.itemView.imgPreview)

            holder.itemView.imgPreview?.backgroundTintList = ColorStateList.valueOf(holder.drawing?.backgroundColor ?: Color.WHITE)
        }
    }
}