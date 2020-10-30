/*
 * Copyright 2020, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.app.factory

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.cartlc.tracker.fresh.ui.entrysimple.EntrySimpleViewMvc
import com.cartlc.tracker.fresh.ui.entrysimple.EntrySimpleViewMvcImpl
import com.cartlc.tracker.fresh.ui.buttons.ButtonsViewMvc
import com.cartlc.tracker.fresh.ui.buttons.ButtonsViewMvcImpl
import com.cartlc.tracker.fresh.ui.confirm.ConfirmFinalViewMvc
import com.cartlc.tracker.fresh.ui.confirm.ConfirmFinalViewMvcImpl
import com.cartlc.tracker.fresh.ui.confirm.item.*
import com.cartlc.tracker.fresh.ui.listentries.ListEntriesViewMvc
import com.cartlc.tracker.fresh.ui.listentries.ListEntriesViewMvcImpl
import com.cartlc.tracker.fresh.ui.listentries.item.ListEntriesItemViewMvc
import com.cartlc.tracker.fresh.ui.listentries.item.ListEntriesItemViewMvcImpl
import com.cartlc.tracker.fresh.ui.login.LoginViewMvc
import com.cartlc.tracker.fresh.ui.login.LoginViewMvcImpl
import com.cartlc.tracker.fresh.ui.main.MainViewMvc
import com.cartlc.tracker.fresh.ui.main.MainViewMvcImpl
import com.cartlc.tracker.fresh.ui.mainlist.MainListViewMvc
import com.cartlc.tracker.fresh.ui.mainlist.MainListViewMvcImpl
import com.cartlc.tracker.fresh.ui.mainlist.adapter.item.*
import com.cartlc.tracker.fresh.ui.picture.PictureListViewMvc
import com.cartlc.tracker.fresh.ui.picture.PictureListViewMvcImpl
import com.cartlc.tracker.fresh.ui.picture.item.*
import com.cartlc.tracker.fresh.ui.title.TitleViewMvc
import com.cartlc.tracker.fresh.ui.title.TitleViewMvcImpl

class FactoryViewMvc(
        private val inflater: LayoutInflater,
        private val factoryAdapterController: FactoryAdapterController
) {
    private fun getInflater(container: ViewGroup?): LayoutInflater {
        container?.let {
            return LayoutInflater.from(it.context)
        } ?: return inflater
    }

    fun allocEntrySimpleViewMvc(container: ViewGroup?): EntrySimpleViewMvc {
        return EntrySimpleViewMvcImpl(getInflater(container), container)
    }

    fun allocLoginViewMvc(container: ViewGroup?): LoginViewMvc {
        return LoginViewMvcImpl(getInflater(container), container)
    }

    fun allocButtonsViewMvc(container: ViewGroup?): ButtonsViewMvc {
        return ButtonsViewMvcImpl(getInflater(container), container)
    }

    fun allocTitleViewMvc(container: ViewGroup?): TitleViewMvc {
        return TitleViewMvcImpl(getInflater(container), container)
    }

    fun allocConfirmViewMvc(context: Context): ConfirmFinalViewMvc {
        return ConfirmFinalViewMvcImpl(LayoutInflater.from(context), null, this)
    }

    fun allocMainListViewMvc(container: ViewGroup?): MainListViewMvc {
        return MainListViewMvcImpl(getInflater(container), container, this, factoryAdapterController)
    }

    fun allocSimpleListItemViewMvc(container: ViewGroup?, @LayoutRes entryItemLayoutId: Int): SimpleItemViewMvc {
        return SimpleItemViewMvcImpl(getInflater(container), container, entryItemLayoutId)
    }

    fun allocProjectGroupItemViewMvc(container: ViewGroup?): ProjectGroupItemViewMvc {
        return ProjectGroupItemViewMvcImpl(getInflater(container), container)
    }

    fun allocEquipmentSelectItemViewMvc(container: ViewGroup?): EquipmentSelectItemViewMvc {
        return EquipmentSelectItemViewMvcImpl(getInflater(container), container)
    }

    fun allocRadioListItemViewMvc(container: ViewGroup?): RadioListItemViewMvc {
        return RadioListItemViewMvcImpl(getInflater(container), container)
    }

    fun allocCheckBoxItemViewMvc(container: ViewGroup?): CheckBoxItemViewMvc {
        return CheckBoxItemViewMvcImpl(getInflater(container), container)
    }

    fun allocNoteListEntryItemViewMvc(container: ViewGroup?): NoteListEntryItemViewMvc {
        return NoteListEntryItemViewMvcImpl(getInflater(container), container)
    }

    fun allocPictureListViewMvc(container: ViewGroup?): PictureListViewMvc {
        return PictureListViewMvcImpl(getInflater(container), container, this)
    }

    fun allocPictureListItemViewMvc(container: ViewGroup?): PictureListItemViewMvc {
        return PictureListItemViewMvcImpl(getInflater(container), container)
    }

    fun allocPictureNoteItemViewMvc(container: ViewGroup?): PictureNoteItemViewMvc {
        return PictureNoteItemViewMvcImpl(getInflater(container), container)
    }

    fun allocMainViewMvc(container: ViewGroup?, factoryViewHelper: FactoryViewHelper): MainViewMvc {
        return MainViewMvcImpl(getInflater(container), container, factoryViewHelper)
    }

    fun allocListEntriesViewMvc(container: ViewGroup?): ListEntriesViewMvc {
        return ListEntriesViewMvcImpl(getInflater(container), container, this)
    }

    fun allocConfirmPictureItemViewMvc(container: ViewGroup?): ConfirmPictureItemViewMvc {
        return ConfirmPictureItemViewMvcImpl(getInflater(container), container)
    }

    fun allocConfirmPictureNoteItemViewMvc(container: ViewGroup?): ConfirmPictureNoteItemViewMvc {
        return ConfirmPictureNoteItemViewMvcImpl(getInflater(container), container)
    }

    fun allocConfirmBasicsViewMvc(container: ViewGroup?): ConfirmBasicsViewMvc {
        return ConfirmBasicsViewMvcImpl(getInflater(container), container)
    }

    fun allocConfirmEquipmentViewMvc(container: ViewGroup?): ConfirmEquipmentViewMvc {
        return ConfirmEquipmentViewMvcImpl(getInflater(container), container, this)
    }

    fun allocConfirmNotesViewMvc(container: ViewGroup?): ConfirmNotesViewMvc {
        return ConfirmNotesViewMvcImpl(getInflater(container), container)
    }

    fun allocConfirmPictureViewMvc(container: ViewGroup?): ConfirmPictureViewMvc {
        return ConfirmPictureViewMvcImpl(getInflater(container), container, this)
    }

    fun allocListEntriesViewItemViewMvc(container: ViewGroup): ListEntriesItemViewMvc {
        return ListEntriesItemViewMvcImpl(getInflater(container), container)
    }

    fun allocSubFlowItemViewMvc(container: ViewGroup): SubFlowItemViewMvc {
        return SubFlowItemViewMvcImpl(getInflater(container), container)
    }

}