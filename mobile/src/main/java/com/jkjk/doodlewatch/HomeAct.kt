package com.jkjk.doodlewatch

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.util.Base64
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.jkjk.doodlewatch.adapter.DrawPreviewAdapter
import com.jkjk.doodlewatch.core.act.CommunicationAct
import com.jkjk.doodlewatch.core.database.AppDatabase
import com.jkjk.doodlewatch.core.model.Drawing
import com.jkjk.doodlewatch.core.model.DrawingHistory
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.act_home.*
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class HomeAct : CommunicationAct(), PermissionManager.PermissionListener {

    override val layoutResId: Int = R.layout.act_home

    private val adapter = DrawPreviewAdapter(this)

    private val permissionManager by lazy {
        PermissionManager(this, this)
    }

    private var drawingToSave: Drawing? = null

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
            sendDrawingSyncInfo(this, object : StringListener {
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

    fun onDrawingSelect(drawing: Drawing) {
        AlertDialog.Builder(this)
            .setTitle(R.string.option)
            .setItems(arrayOf(
                getString(R.string.rename),
                getString(R.string.export),
                getString(R.string.delete)
            )) { _, which ->
                when (which) {
                    0 -> {
                        val input = EditText(this)
                        input.layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT
                        )
                        input.setText(drawing.name)
                        input.setSelection(drawing.name?.length ?: 0)

                        AlertDialog.Builder(this)
                            .setTitle(R.string.rename)
                            .setView(input)
                            .setPositiveButton(R.string.rename) { _, _ ->
                                if (input.text.isNullOrBlank()) {
                                    Toast.makeText(this, R.string.name_invalid, Toast.LENGTH_SHORT).show()
                                } else {
                                    drawing.name = input.text?.toString()
                                    drawing.lastEditOn = System.currentTimeMillis()
                                    drawingDao.insert(drawing).toInt()
                                    AppDatabase.getInstance(this)
                                        .getDrawingHistoryDao()
                                        .insert(
                                            DrawingHistory(drawing.dbId, drawing.lastEditOn)
                                        )

                                    // Sync to other devices
                                    dataClient.putDataItem(
                                            createDrawingSendRequest(drawing)
                                    )
                                }
                            }
                            .setNegativeButton(R.string.cancel, null)
                            .show()
                    }
                    1 -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q || permissionManager.obtainPermission(
                                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                            Glide.with(this)
                                .asBitmap()
                                .load(Base64.decode(drawing.base64Image!!, Base64.DEFAULT))
                                .into(object : CustomTarget<Bitmap>() {
                                    override fun onResourceReady(
                                        resource: Bitmap,
                                        transition: Transition<in Bitmap>?
                                    ) {

                                        val newBitmap = Bitmap.createBitmap(resource.width, resource.height, resource.config)
                                        val canvas = Canvas(newBitmap)
                                        canvas.drawColor(drawing.backgroundColor)
                                        canvas.drawBitmap(resource, 0F, 0F, null)
                                        saveBitmap(newBitmap)
                                    }

                                    override fun onLoadCleared(placeholder: Drawable?) {
                                    }
                                })

                        } else {
                            drawingToSave = drawing
                        }
                    }
                    2 -> {
                        AlertDialog.Builder(this)
                            .setTitle(R.string.delete)
                            .setMessage(R.string.delete_confirm)
                            .setPositiveButton(R.string.delete) { _, _ ->
                                drawingDao.remove(drawing.dbId)
                                AppDatabase.getInstance(this)
                                    .getDrawingHistoryDao()
                                    .insert(DrawingHistory(drawing.dbId, drawing.lastEditOn, System.currentTimeMillis()))

                                sendDrawingSyncInfo(this, null)
                            }
                            .setNegativeButton(R.string.cancel, null)
                            .show()
                    }
                }
            }
            .show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (!permissionManager.onPermissionResult(requestCode, permissions, grantResults)) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!permissionManager.onActivityResult(requestCode)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onPermissionGrant(permission: String) {
        if (drawingToSave != null) {
            val bgColor = drawingToSave!!.backgroundColor
            Glide.with(this)
                .asBitmap()
                .load(Base64.decode(drawingToSave?.base64Image!!, Base64.DEFAULT))
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {

                        val newBitmap = Bitmap.createBitmap(resource.width, resource.height, resource.config)
                        val canvas = Canvas(newBitmap)
                        canvas.drawColor(bgColor)
                        canvas.drawBitmap(resource, 0F, 0F, null)
                        saveBitmap(newBitmap)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                    }
                })

            drawingToSave = null
        }
    }

    override fun onPermissionReject(permission: String) {
        drawingToSave = null
    }

    private fun saveBitmap(draw: Bitmap) {
        Observable.fromCallable {
            val pendingPhotoUri: Uri?
            var out: OutputStream? = null

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                pendingPhotoUri = getCameraUri()
                if (pendingPhotoUri != null) {
                    out = contentResolver.openOutputStream(pendingPhotoUri)
                }
            } else {
                val photoFile = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), fileNameByTime())
                pendingPhotoUri = FileProvider.getUriForFile(this, packageName + ".provider", photoFile)
                out = FileOutputStream(photoFile)
            }

            if (out == null) {
                return@fromCallable null
            } else {
                draw.compress(Bitmap.CompressFormat.PNG, 90, out)

                try {
                    draw.recycle()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                return@fromCallable pendingPhotoUri
            }
        }
            .onErrorComplete()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError {
                Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show()
            }
            .subscribe {
                if (it != null) {
                    galleryUpdate(this, it)
                    Toast.makeText(this, R.string.saved, Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun galleryUpdate(context: Context, location: Uri) {
        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        mediaScanIntent.data = location
        context.sendBroadcast(mediaScanIntent)
    }


    private fun fileNameByTime(): String {
        return System.currentTimeMillis().toString() + ".png"
    }

    private fun getCameraUri(): Uri? {
        val status = Environment.getExternalStorageState()
        return contentResolver.insert(
            if (status == Environment.MEDIA_MOUNTED)
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            else
                MediaStore.Images.Media.INTERNAL_CONTENT_URI
            , ContentValues()
        )
    }
}