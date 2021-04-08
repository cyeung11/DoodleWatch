package com.jkjk.doodlewatch.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 *Created by chrisyeung on 29/11/2018.
 */

// A class which can auto adjust to width of the view holder to fit the recycler view
abstract class GridRecyclerViewAdapter<T: GridRecyclerViewAdapter.ViewHolder>(context: Context): RecyclerView.Adapter<T>(){

    private var recyclerView: RecyclerView? = null
    protected val layoutInflater: LayoutInflater by lazy { LayoutInflater.from(context) }

    private var leftMargin: Int? = null
    private var rightMargin: Int? = null
    private var spanCount: Int = 2

    @LayoutRes
    abstract fun getLayoutId(viewType: Int): Int

    abstract class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        //keep reference to recycler view to calculate appropriate size of holder
        this.recyclerView = recyclerView
        if (recyclerView.layoutManager is GridLayoutManager) {
            spanCount = (recyclerView.layoutManager as GridLayoutManager).spanCount
        }
    }

    abstract fun onCreateViewHolder(viewGroup: ViewGroup, parent: ViewGroup, viewType: Int): T

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): T {
        val viewGroup = layoutInflater.inflate(getLayoutId(viewType), parent, false) as ViewGroup

        if (leftMargin == null || rightMargin == null) {
            val marginLayoutParams = viewGroup.layoutParams as? ViewGroup.MarginLayoutParams
            if (marginLayoutParams != null) {
                leftMargin = marginLayoutParams.leftMargin
                rightMargin = marginLayoutParams.rightMargin
            }
        }

        if (recyclerView != null) {
            if (recyclerView!!.width > 0){
                adjustHolderSize(viewGroup)
            } else {
                // view not finished layout yet
                recyclerView!!.viewTreeObserver.addOnPreDrawListener(object: ViewTreeObserver.OnPreDrawListener{
                    override fun onPreDraw(): Boolean {
                        if (recyclerView!!.width > 0) {
                            adjustHolderSize(viewGroup)
                            recyclerView!!.viewTreeObserver.removeOnPreDrawListener(this)
                        }
                        return true
                    }
                })
            }
        }
        return onCreateViewHolder(viewGroup, parent, viewType)
    }

    private fun adjustHolderSize(viewGroup: ViewGroup) {
        val recyclerViewWidth = recyclerView!!.width - recyclerView!!.paddingLeft - recyclerView!!.paddingRight
        val size = (recyclerViewWidth / spanCount) - (leftMargin ?: 0) - (rightMargin ?: 0)
        if (size > 0) {
            viewGroup.layoutParams.width = size
            viewGroup.requestLayout()
        }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = null
        super.onDetachedFromRecyclerView(recyclerView)
    }

}