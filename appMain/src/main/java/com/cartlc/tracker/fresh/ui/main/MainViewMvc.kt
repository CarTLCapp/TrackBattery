/**
 * Copyright 2020, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.main

import androidx.annotation.ColorRes
import com.cartlc.tracker.fresh.ui.buttons.ButtonsUseCase
import com.cartlc.tracker.fresh.ui.common.viewmvc.ObservableViewMvc
import com.cartlc.tracker.fresh.ui.confirm.ConfirmFinalUseCase
import com.cartlc.tracker.fresh.ui.entrysimple.EntrySimpleUseCase
import com.cartlc.tracker.fresh.ui.mainlist.MainListUseCase
import com.cartlc.tracker.fresh.ui.picture.PictureListUseCase
import com.cartlc.tracker.fresh.ui.title.TitleUseCase

interface MainViewMvc : ObservableViewMvc<MainViewMvc.Listener> {

    interface Listener {
        fun onAddClicked()
    }

    enum class FragmentType {
        NONE,
        LOGIN,
        CONFIRM,
    }

    data class EntryHint(
            val msg: String? = null,
            @ColorRes val textColor: Int = 0
    )

    var fragmentVisible: FragmentType
    var picturesVisible: Boolean
    var addButtonVisible: Boolean
    var customProgress: String?
    
    val buttonsUseCase: ButtonsUseCase
    val titleUseCase: TitleUseCase
    val pictureUseCase: PictureListUseCase
    val mainListUseCase: MainListUseCase
    val entrySimpleUseCase: EntrySimpleUseCase
    val confirmUseCase: ConfirmFinalUseCase?

    fun setEntryHint(hint: EntryHint)
}