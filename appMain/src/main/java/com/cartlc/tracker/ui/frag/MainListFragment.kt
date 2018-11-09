package com.cartlc.tracker.ui.frag

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cartlc.tracker.databinding.FragMainListBinding
import com.cartlc.tracker.model.flow.Stage
import com.cartlc.tracker.model.data.DataNote
import com.cartlc.tracker.model.event.EventRefreshProjects
import com.cartlc.tracker.ui.list.*
import com.cartlc.tracker.model.misc.EntryHint
import com.cartlc.tracker.model.misc.TruckStatus
import com.cartlc.tracker.ui.act.MainActivity
import com.cartlc.tracker.viewmodel.ButtonsViewModel
import com.cartlc.tracker.viewmodel.EntrySimpleViewModel
import com.cartlc.tracker.viewmodel.MainListViewModel
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber

class MainListFragment : BaseFragment() {

    lateinit var binding: FragMainListBinding

    val vm: MainListViewModel
        get() = baseVM as MainListViewModel

    var showing: Boolean
        get() = vm.showing.get()
        set(value) {
            vm.showing.set(value)
        }

    var showEmpty: Boolean
        get() = vm.showEmpty.get()
        set(value) {
            vm.showEmpty.set(value)
        }

    val mainList: RecyclerView
        get() = binding.mainList
    val empty: TextView
        get() = binding.empty

    val ctx: Context
        get() = context!!

    val mainActivity: MainActivity?
        get() = activity as? MainActivity

    val buttonsViewModel: ButtonsViewModel?
        get() = mainActivity?.buttonsFragment?.vm

    val entrySimpleViewModel: EntrySimpleViewModel?
        get() = mainActivity?.entrySimpleFragment?.vm

    lateinit private var simpleAdapter: SimpleListAdapter
    lateinit private var projectAdapter: ProjectGroupListAdapter
    lateinit private var equipmentSelectAdapter: EquipmentSelectListAdapter
    lateinit private var noteEntryAdapter: NoteListEntryAdapter
    lateinit private var radioAdapter: RadioListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragMainListBinding.inflate(layoutInflater, container, false)
        baseVM = MainListViewModel(ctx)
        binding.viewModel = vm
        super.onCreateView(inflater, container, savedInstanceState)

        val linearLayoutManager = LinearLayoutManager(mainList.context)
        mainList.layoutManager = linearLayoutManager
        val divider = DividerItemDecoration(mainList.context, linearLayoutManager.orientation)
        mainList.addItemDecoration(divider)
        simpleAdapter = SimpleListAdapter(mainList.context) { pos, text ->
            vm.key.value = text
        }
        vm.key.observe(this, Observer { value ->
            val cur = vm.curFlowValue
            when (vm.curFlowValue.stage) {
                Stage.PROJECT,
                Stage.CITY,
                Stage.STATE,
                Stage.STREET -> {
                    buttonsViewModel?.showNextButtonValue = true
                }
                Stage.COMPANY -> {
                    mainActivity?.checkCenterButtonIsEdit()
                }
                Stage.TRUCK -> {
                    entrySimpleViewModel?.simpleText?.set(value)
                }
                else -> {
                }
            }
        })
        projectAdapter = ProjectGroupListAdapter(mainList.context, vm)
        equipmentSelectAdapter = EquipmentSelectListAdapter(vm)
        radioAdapter = RadioListAdapter(mainList.context)
        radioAdapter.listener = { _, text -> vm.onStatusButtonClicked(TruckStatus.from(ctx, text)) }
        noteEntryAdapter = NoteListEntryAdapter(mainList.context, vm, object : NoteListEntryAdapter.EntryListener {
            var currentFocus: DataNote? = null

            override fun textEntered(note: DataNote) {
                if (currentFocus === note) {
                    display(note)
                }
            }

            override fun textFocused(note: DataNote) {
                currentFocus = note
                display(note)
            }

            private fun display(note: DataNote) {
                if (!vm.isInNotes) {
                    return
                }
                if (note.num_digits > 0) {
                    if (note.value != null && note.value!!.isNotBlank()) {
                        val sbuf = StringBuilder()
                        val count = note.value!!.length
                        sbuf.append(count)
                        sbuf.append("/")
                        sbuf.append(note.num_digits.toInt())
                        vm.entryHint.value = EntryHint(sbuf.toString(), (count > note.num_digits))
                    } else {
                        vm.entryHint.value = EntryHint("", false)
                    }
                } else {
                    vm.entryHint.value = EntryHint("", false)
                }
            }
        })
        return binding.root
    }

    fun setAdapter(stage: Stage) {
        when (stage) {
            Stage.CURRENT_PROJECT -> {
                vm.curKey = null
                mainList.adapter = projectAdapter
                projectAdapter.onDataChanged()
            }
            Stage.EQUIPMENT -> {
                mainList.adapter = equipmentSelectAdapter
                equipmentSelectAdapter.onDataChanged(vm.tmpPrefHelper.currentProjectGroup)
            }
            Stage.NOTES -> {
                // TODO: Push visibility logic to VM
                noteEntryAdapter.onDataChanged()
                if (noteEntryAdapter.itemCount == 0) {
                    mainList.visibility = View.GONE
                    empty.visibility = View.VISIBLE
                } else {
                    mainList.adapter = noteEntryAdapter
                    empty.visibility = View.GONE
                }
            }
            Stage.STATUS -> {
                mainList.adapter = radioAdapter
                radioAdapter.list = listOf(
                        TruckStatus.COMPLETE.getString(ctx),
                        TruckStatus.PARTIAL.getString(ctx),
                        TruckStatus.NEEDS_REPAIR.getString(ctx)
                )
                radioAdapter.selectedText = vm.status?.getStringNull(ctx)
            }
            else -> mainList.adapter = simpleAdapter
        }
    }

    fun setList(key: String, list: List<String>): Boolean {
        showing = true
        mainList.adapter = simpleAdapter
        simpleAdapter.items = list
        vm.curKey = key
        val curValue = vm.tmpPrefHelper.getKeyValue(key)
        if (curValue == null) {
            simpleAdapter.setNoneSelected()
            return false
        }
        val position = simpleAdapter.setSelected(curValue)
        if (position >= 0) {
            mainList.scrollToPosition(position)
        }
        return true
    }

    // TODO: temporary only
    fun isNotesComplete(): Boolean {
        return noteEntryAdapter.isNotesComplete
    }

    // TODO: temporary only
    fun notes(): List<DataNote> {
        return noteEntryAdapter.notes
    }
}