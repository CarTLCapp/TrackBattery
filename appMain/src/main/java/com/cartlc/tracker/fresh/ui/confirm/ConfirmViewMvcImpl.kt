package com.cartlc.tracker.fresh.ui.confirm

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.callassistant.util.viewmvc.ObservableViewMvcImpl
import com.cartlc.tracker.R
import com.cartlc.tracker.fresh.model.core.data.DataNote
import com.cartlc.tracker.fresh.model.core.data.DataPicture
import com.cartlc.tracker.ui.bits.AutoLinearLayoutManager
import com.cartlc.tracker.ui.list.NoteListAdapter
import com.cartlc.tracker.ui.list.PictureThumbnailListAdapter
import com.cartlc.tracker.ui.list.SimpleListAdapter

class ConfirmViewMvcImpl(
        inflater: LayoutInflater,
        container: ViewGroup?
) : ObservableViewMvcImpl<ConfirmViewMvc.Listener>(), ConfirmViewMvc {

    override val rootView: View = inflater.inflate(R.layout.frame_confirm, container, false) as ViewGroup

    private val ctx = rootView.context

    private val equipmentGrid = findViewById<RecyclerView>(R.id.equipment_grid)
    private val notesList = findViewById<RecyclerView>(R.id.notes_list)
    private val confirmPictureList = findViewById<RecyclerView>(R.id.confirm_pictures_list)
    private val equipmentListAdapter: SimpleListAdapter
    private val noteAdapter: NoteListAdapter
    private val pictureAdapter: PictureThumbnailListAdapter
    private val projectNameValue = findViewById<TextView>(R.id.project_name_value)
    private val projectAddressValue = findViewById<TextView>(R.id.project_address_value)
    private val truckNumberValue = findViewById<TextView>(R.id.truck_number_value)
    private val statusValue = findViewById<TextView>(R.id.status_value)
    private val confirmPicturesLabel = findViewById<TextView>(R.id.confirm_pictures_label)

    init {
        equipmentListAdapter = SimpleListAdapter(ctx, R.layout.entry_item_confirm)
        equipmentGrid.adapter = equipmentListAdapter
        val gridLayout = GridLayoutManager(ctx, 2)
        equipmentGrid.layoutManager = gridLayout
        noteAdapter = NoteListAdapter(ctx)
        notesList.adapter = noteAdapter
        notesList.layoutManager = LinearLayoutManager(ctx)
        pictureAdapter = PictureThumbnailListAdapter(ctx)
        val layoutManager = AutoLinearLayoutManager(ctx, LinearLayoutManager.HORIZONTAL, false)
        confirmPictureList.layoutManager = layoutManager
        confirmPictureList.adapter = pictureAdapter
    }

    override var projectName: String
        get() = projectNameValue.text.toString()
        set(value) { projectNameValue.text = value }

    override var projectAddress: String?
        get() = projectAddressValue.text.toString()
        set(value) {
            projectAddressValue.text = value
            projectAddressValue.visibility = if (value == null) View.GONE else View.VISIBLE
        }

    override var truckNumber: String?
        get() = truckNumberValue.text.toString()
        set(value) {
            truckNumberValue.text = value
            truckNumberValue.visibility = if (value == null) View.GONE else View.VISIBLE
        }

    override var status: String
        get() = statusValue.text.toString()
        set(value) { statusValue.text = value }

    override var pictureLabel: String
        get() = confirmPicturesLabel.text.toString()
        set(value) { confirmPicturesLabel.text = value }

    override var notes: List<DataNote>
        get() = TODO("not implemented")
        set(value) { noteAdapter.setItems(value) }

    override var equipmentNames: List<String>
        get() = TODO("not implemented")
        set(value) { equipmentListAdapter.items = value }

    override var pictures: MutableList<DataPicture>
        get() = TODO("not implemented")
        set(value) { pictureAdapter.setList(value) }

}