package com.jkjk.doodlewatch.core.act

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.*
import com.jkjk.doodlewatch.core.CommunicationModule
import com.jkjk.doodlewatch.core.DaggerCommunicationComponent
import com.jkjk.doodlewatch.core.database.DrawingDao
import com.jkjk.doodlewatch.core.model.Drawing
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.ByteArrayOutputStream
import java.io.InputStream
import javax.inject.Inject

/**
 * Activity that handle the communication between wear and phone. Exchange database status and sync the drawing
 */
abstract class CommunicationAct : BaseAct(), DataClient.OnDataChangedListener,
    MessageClient.OnMessageReceivedListener {

    @Inject lateinit var dataClient: DataClient

    @Inject lateinit var messageClient: MessageClient

    @Inject lateinit var drawingDao: DrawingDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DaggerCommunicationComponent.builder().communicationModule(CommunicationModule(this)).build().inject(this)
    }

    override fun onResume() {
        super.onResume()
        dataClient.addListener(this)
        messageClient.addListener(this)

        sendDrawingSyncInfo(this, null)
    }

    override fun onPause() {
        dataClient.removeListener(this)
        messageClient.removeListener(this)
        super.onPause()
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        dataEvents.forEach { e ->
            if (e.type == DataEvent.TYPE_CHANGED) {
                if (e?.dataItem?.uri?.path?.startsWith(DOODLE_URI) == true) {
                    val dataMap = DataMapItem.fromDataItem(e.dataItem)?.dataMap
                    Observable.fromCallable {
                        createDrawingFromDataEvent(this, dataMap)
                    }.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnError {
                            it?.printStackTrace()
                        }
                        .onErrorComplete()
                        .subscribe {
                            if (it != null) {
                                drawingDao.insert(it)
                            }
                        }
                }
            }
        }
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        if (messageEvent.path == DOODLE_WATCH_PATH) {
            val drawing = retrieveDrawingToSend(messageEvent)
            drawing.forEach { d ->
                dataClient.putDataItem(
                    createDrawingSendRequest(d)
                )
            }
        }
    }

    private fun retrieveDrawingToSend(event: MessageEvent): List<Drawing> {
        val rawString = try {
            String(event.data)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

        val drawingToSend = arrayListOf<Drawing>()

        if (rawString != null) {

            val ids = arrayListOf<Int>()

            val splitString = rawString.split(";")
            splitString.forEach { info ->
                val splitInfo = info.split(":")
                if (splitInfo.size == 2) {
                    val dbId = splitInfo[0].toIntOrNull()
                    val lastEdit = splitInfo[1].toLongOrNull()

                    if (dbId != null) {
                        ids.add(dbId)
                        if (lastEdit != null) {
                            drawingDao.getNewer(dbId, lastEdit)?.let {
                                drawingToSend.add(it)
                            }
                        }
                    }
                }
            }

            drawingToSend.addAll(drawingDao.getNotExist(ids.toIntArray()))
        }

        return drawingToSend
    }

    private fun createDrawingSendRequest(drawing: Drawing): PutDataRequest {
        val request = PutDataMapRequest.create("${DOODLE_URI}/${drawing.dbId}").also {
            it.dataMap.apply {
                putString(MAP_DRAWING_NAME, drawing.name)
                putLong(MAP_DRAWING_CREATED_ON, drawing.createdDate)
                putLong(MAP_DRAWING_LAST_EDIT, drawing.lastEditOn)
                putInt(MAP_DRAWING_BACKGROUND, drawing.backgroundColor)
                putBoolean(MAP_DRAWING_FLAG, drawing.isFlagged)
                putInt(MAP_DRAWING_ID, drawing.dbId)
                putLong("TIMESTAMP", System.currentTimeMillis())

                if (drawing.base64Image?.isNotBlank() == true) {
                    putAsset(
                        MAP_DRAWING_IMAGE,
                        Asset.createFromBytes(drawing.base64Image!!.toByteArray())
                    )
                }
            }
        }
        return request.asPutDataRequest()
    }

    protected fun sendDrawingSyncInfo(context: Context, listener: StringListener?) {
        Observable.fromCallable {
            val c = Tasks.await(
                Wearable.getCapabilityClient(context).getCapability(DOODLE_WATCH_CAPABILITY, CapabilityClient.FILTER_REACHABLE)
            )

            return@fromCallable (c.nodes?.firstOrNull { it.isNearby }?.id ?: c.nodes?.firstOrNull()?.id) ?: ""
        }.doOnError {
            it.printStackTrace()
        }.onErrorComplete()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {  nodeId ->
                listener?.onString(nodeId)

                if (nodeId.isNotBlank()) {

                    val all = drawingDao.getAllSync()
                    val stringBuilder = StringBuilder()
                    all.forEach {
                        stringBuilder.append("${it.dbId}:${it.lastEditOn};")
                    }

                    Wearable.getMessageClient(context).sendMessage(
                        nodeId,
                        DOODLE_WATCH_PATH,
                        stringBuilder.toString().toByteArray()
                    )
                }
            }
    }

    private fun createDrawingFromDataEvent(context: Context, dataMap: DataMap?): Drawing {
        val result = Drawing(
            createdDate = dataMap?.getLong(MAP_DRAWING_CREATED_ON, 0L) ?: 0L,
            lastEditOn = dataMap?.getLong(MAP_DRAWING_LAST_EDIT, 0L) ?: 0L,
            name = dataMap?.getString(MAP_DRAWING_NAME),
            backgroundColor = dataMap?.getInt(MAP_DRAWING_BACKGROUND, Color.WHITE) ?: Color.WHITE,
            isFlagged = dataMap?.getBoolean(MAP_DRAWING_FLAG, false) ?: false
        )
        result.dbId = dataMap?.getInt(MAP_DRAWING_ID, 0) ?: 0

        val drawAsset = dataMap?.getAsset(MAP_DRAWING_IMAGE)
        if (drawAsset != null) {
            try {
                Tasks.await(
                    Wearable.getDataClient(context).getFdForAsset(
                        drawAsset
                    )
                )?.inputStream?.let {
                    result.base64Image = extractStringFromInputStream(it)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return result
    }

    private fun extractStringFromInputStream(inputStream: InputStream): String? {
        val result = ByteArrayOutputStream()
        val buffer = ByteArray(1024)

        var length = inputStream.read(buffer)
        while (length != -1) {
            result.write(buffer, 0, length)
            length = inputStream.read(buffer)
        }

        return result.toString("UTF-8")
    }

    companion object {
        private const val DOODLE_URI = "/doodledata"
        private const val MAP_DRAWING_NAME = "name"
        private const val MAP_DRAWING_CREATED_ON = "createdDate"
        private const val MAP_DRAWING_LAST_EDIT = "lastEditOn"
        private const val MAP_DRAWING_IMAGE = "base64Image"
        private const val MAP_DRAWING_BACKGROUND = "backgroundColor"
        private const val MAP_DRAWING_FLAG = "isFlagged"
        private const val MAP_DRAWING_ID = "dbId"

        private const val DOODLE_WATCH_CAPABILITY = "doodle_watch_capability"
        private const val DOODLE_WATCH_PATH = "/doodlelatest"
    }

    interface StringListener {
        fun onString(value: String?)
    }
}