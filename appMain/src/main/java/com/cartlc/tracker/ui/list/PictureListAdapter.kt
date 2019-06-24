/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.ui.list

import java.io.File
import java.util.HashMap

import com.cartlc.tracker.R
import com.cartlc.tracker.ui.app.TBApplication
import com.cartlc.tracker.fresh.model.core.data.DataPicture
import com.squareup.picasso.Picasso

import android.app.AlertDialog
import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import com.cartlc.tracker.ui.util.helper.BitmapHelper
import kotlinx.android.synthetic.main.entry_item_picture.view.*

import java.lang.ref.WeakReference

/**
 * Created by dug on 5/10/17.
 */
open class PictureListAdapter(
        protected val mContext: Context,
        protected val mListener: (Int) -> Unit?
) : RecyclerView.Adapter<PictureListAdapter.CustomViewHolder>() {

    companion object {
        private const val MSG_REMOVE_ITEM = 1
        private const val DELAY_REMOVE_ITEM = 100
    }

    private class MyHandler(other: PictureListAdapter) : Handler() {

        private val obj = WeakReference<PictureListAdapter>(other)

        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_REMOVE_ITEM -> if (msg.obj is DataPicture) {
                    val item = msg.obj as DataPicture
                    obj.get()?.itemRemove(item)
                }
            }
        }
    }

    private val mLayoutInflater: LayoutInflater = LayoutInflater.from(mContext)
    private var mRotation: HashMap<String, Int> = HashMap()
    protected var mItems: MutableList<DataPicture> = mutableListOf()
    private var mHandler = MyHandler(this)

    protected open val itemLayout: Int
        get() = R.layout.entry_item_picture

    protected open val maxHeightResource: Int
        get() = R.dimen.image_full_max_height

    private var maxHeight: Int = 0
        get() {
            if (field == 0) {
                field = mContext.resources.getDimension(maxHeightResource).toInt()
            }
            return field
        }

    val commonRotation: Int
        get() {
            var commonRotation = 0
            for (picture in mItems) {
                val path = picture.unscaledFile.absolutePath
                if (!mRotation.containsKey(path)) {
                    return 0
                }
                val rotation = mRotation[path]
                if (commonRotation == 0 && rotation != null) {
                    commonRotation = rotation
                } else if (commonRotation != rotation) {
                    return 0
                }
            }
            return commonRotation
        }

    private fun itemRemove(item: DataPicture) {
        item.remove()
        mItems.remove(item)
        notifyDataSetChanged()
    }

    inner class CustomViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

        fun bind(item: DataPicture) {
            with(view) {
                Picasso.get().cancelRequest(picture)
                val pictureFile: File?
                when {
                    item.existsUnscaled -> pictureFile = item.unscaledFile
                    item.existsScaled -> pictureFile = item.scaledFile
                    else -> pictureFile = null
                }
                if (pictureFile == null || !pictureFile.exists()) {
                    val msg = Message()
                    msg.what = MSG_REMOVE_ITEM
                    msg.obj = item
                    mHandler.sendMessageDelayed(msg, DELAY_REMOVE_ITEM.toLong())
                    picture.setImageResource(android.R.color.transparent)
                    loading.setText(R.string.error_picture_removed)
                } else {
                    BitmapHelper.loadBitmap(pictureFile.absolutePath, maxHeight, picture)
                    loading.visibility = View.GONE
                    remove?.let { view ->
                        view.setOnClickListener {
                            item.remove()
                            mItems.remove(item)
                            notifyDataSetChanged()
                            mListener.invoke(mItems.size)
                        }
                    }
                    rotate_cw?.let { view ->
                        view.setOnClickListener {
                            incRotation(item, item.rotateCW())
                            notifyDataSetChanged()
                        }
                    }
                    rotate_ccw?.let { view ->
                        view.setOnClickListener {
                            incRotation(item, item.rotateCCW())
                            notifyDataSetChanged()
                        }
                    }
                    note_dialog?.let { view ->
                        view.setOnClickListener { showPictureNoteDialog(item) }
                    }
                    note?.let { view ->
                        if (TextUtils.isEmpty(item.note)) {
                            view.visibility = View.INVISIBLE
                        } else {
                            view.visibility = View.VISIBLE
                            view.text = item.note
                        }
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val view = mLayoutInflater.inflate(itemLayout, parent, false)
        return CustomViewHolder(view)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        holder.bind(mItems[position])
    }

    fun getUri(file: File): Uri {
        return TBApplication.getUri(mContext, file)
    }

    override fun getItemCount(): Int {
        return mItems.size
    }

    fun setList(list: List<DataPicture>) {
        mItems = list.toMutableList()
        maxHeight = 0
        notifyDataSetChanged()
    }

    internal fun showPictureNoteDialog(item: DataPicture) {
        val builder = AlertDialog.Builder(mContext)
        val noteView = mLayoutInflater.inflate(R.layout.picture_note, null)
        builder.setView(noteView)

        val edt = noteView.findViewById<View>(R.id.note) as EditText
        edt.setText(item.note)

        builder.setTitle(R.string.picture_note_title)
        builder.setPositiveButton("Done") { dialog, _ ->
            item.note = edt.text.toString().trim { it <= ' ' }
            dialog.dismiss()
            notifyDataSetChanged()
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
        val b = builder.create()
        b.show()
    }

    internal fun incRotation(item: DataPicture, degrees: Int) {
        val file = item.unscaledFile
        val path = file.absolutePath
        if (mRotation.containsKey(path)) {
            mRotation[path] = (mRotation[path]!! + degrees) % 360
        } else {
            mRotation[path] = degrees
        }
    }

    fun hadSomeRotations(): Boolean {
        for (key in mRotation.keys) {
            if (mRotation[key] != 0) {
                return true
            }
        }
        return false
    }

}
