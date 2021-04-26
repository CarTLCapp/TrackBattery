/*
 * Copyright 2021, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.cartlc.tracker.R
import com.cartlc.tracker.fresh.ui.app.factory.FactoryViewHelper
import com.cartlc.tracker.fresh.ui.buttons.ButtonsController
import com.cartlc.tracker.fresh.ui.buttons.ButtonsView
import com.cartlc.tracker.fresh.ui.buttons.ButtonsViewMvc
import com.cartlc.tracker.fresh.ui.common.viewmvc.ObservableViewMvcImpl
import com.cartlc.tracker.fresh.ui.confirm.ConfirmFinalFragment
import com.cartlc.tracker.fresh.ui.confirm.ConfirmFinalUseCase
import com.cartlc.tracker.fresh.ui.entrysimple.EntrySimpleUseCase
import com.cartlc.tracker.fresh.ui.entrysimple.EntrySimpleView
import com.cartlc.tracker.fresh.ui.login.LoginFragment
import com.cartlc.tracker.fresh.ui.main.MainViewMvc.FragmentType
import com.cartlc.tracker.fresh.ui.mainlist.MainListUseCase
import com.cartlc.tracker.fresh.ui.mainlist.MainListView
import com.cartlc.tracker.fresh.ui.picture.PictureListUseCase
import com.cartlc.tracker.fresh.ui.picture.PictureListView
import com.cartlc.tracker.fresh.ui.title.TitleView
import com.cartlc.tracker.fresh.ui.title.TitleViewMvc
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.security.acl.Group

class MainViewMvcImpl(
        inflater: LayoutInflater,
        container: ViewGroup?,
        factoryViewHelper: FactoryViewHelper
) : ObservableViewMvcImpl<MainViewMvc.Listener>(), MainViewMvc {

    override val rootView: View = inflater.inflate(R.layout.content_main, container, false) as ViewGroup

    var fabAdd: FloatingActionButton? = null
        set(value) {
            field = value
            value?.setOnClickListener {
                for (listener in listeners) {
                    listener.onAddClicked()
                }
            }
        }

    private val mainListView = findViewById<MainListView>(R.id.frame_main_list)
    private val buttonsView = findViewById<ButtonsView>(R.id.frame_buttons)
    private val entrySimpleView = findViewById<EntrySimpleView>(R.id.frame_entry_simple)
    private val picturesView = findViewById<PictureListView>(R.id.frame_pictures)
    private val listEntryHint = findViewById<TextView>(R.id.list_entry_hint)
    private val customProgressView = findViewById<TextView>(R.id.custom_progress)
    private val titleView = findViewById<TitleView>(R.id.frame_title)
    private val updateGroup = findViewById<androidx.constraintlayout.widget.Group>(R.id.update)
    private val updateButton = findViewById<Button>(R.id.update_button)

    private val fragmentHelper = factoryViewHelper.fragmentHelper
    private val buttonsController: ButtonsController
        get() = listeners.first().buttonsController

    private val loginFragment: LoginFragment by lazy {
        LoginFragment(buttonsController)
    }

    private val confirmFragment: ConfirmFinalFragment by lazy {
        ConfirmFinalFragment()
    }

    init {
        updateButton.setOnClickListener { listeners.forEach { it.onUpdateClicked() } }
    }
    // region MainViewMvc

    override val pictureUseCase: PictureListUseCase = picturesView.control
    override val mainListUseCase: MainListUseCase = mainListView.control
    override val entrySimpleUseCase: EntrySimpleUseCase = entrySimpleView.control

    override val titleViewMvc: TitleViewMvc
        get() = titleView.viewMvc

    override val buttonsViewMvc: ButtonsViewMvc
        get() = buttonsView.viewMvc

    override var updateVisible: Boolean
        get() = updateGroup.visibility == View.VISIBLE
        set(value) {
            updateGroup.visibility = if (value) View.VISIBLE else View.GONE
        }

    override val confirmUseCase: ConfirmFinalUseCase?
        get() = confirmFragment.useCase

    override var fragmentVisible: FragmentType = FragmentType.NONE
        set(value) {
            field = value
            when (value) {
                FragmentType.LOGIN -> fragmentHelper.bind(R.id.frame_login, loginFragment)
                FragmentType.CONFIRM -> fragmentHelper.bind(R.id.frame_confirm, confirmFragment)
                else -> {
                    fragmentHelper.clear(R.id.frame_confirm)
                    fragmentHelper.clear(R.id.frame_login)
                }
            }
        }

    override var picturesVisible: Boolean
        get() = picturesView.visibility == View.VISIBLE
        set(value) {
            picturesView.visibility = if (value) View.VISIBLE else View.GONE
        }

    override fun setEntryHint(hint: MainViewMvc.EntryHint) {
        if (hint.msg.isNullOrEmpty()) {
            listEntryHint.visibility = View.GONE
        } else {
            listEntryHint.visibility = View.VISIBLE
            listEntryHint.text = hint.msg
            listEntryHint.setTextColor(ContextCompat.getColor(context, hint.textColor))
        }
    }

    override var addButtonVisible: Boolean
        get() = fabAdd?.visibility == View.VISIBLE
        set(value) {
            fabAdd?.visibility = if (value) View.VISIBLE else View.GONE
        }

    override var customProgress: String?
        get() = customProgressView.text.toString()
        set(value) {
            customProgressView.text = value
        }

    // endregion MainViewMvc

}