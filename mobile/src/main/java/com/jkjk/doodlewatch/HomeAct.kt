package com.jkjk.doodlewatch

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.jkjk.doodlewatch.adapter.DrawPreviewAdapter
import com.jkjk.doodlewatch.core.act.CommunicationAct
import com.jkjk.doodlewatch.core.database.AppDatabase
import kotlinx.android.synthetic.main.act_home.*

class HomeAct : CommunicationAct() {

    override val layoutResId: Int = R.layout.act_home

    private val adapter = DrawPreviewAdapter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupBody()
    }

    override fun setupBody() {
        rvDraw?.adapter = adapter

        AppDatabase.getInstance(this).getDrawingDao().getAllAsync().observe(this) {
            if (it != null) {
                adapter.drawingList = it
                adapter.notifyDataSetChanged()

                txtNoData?.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menuSync) {
            sendDrawingSyncTime(this, object : StringListener {
                override fun onString(value: String?) {
                    if (value?.isNotBlank() == true) {
                        Toast.makeText(this@HomeAct, R.string.syncing, Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@HomeAct, R.string.sync_fail, Toast.LENGTH_SHORT).show()
                    }
                }
            })
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}