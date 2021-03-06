/*
 * Copyright 2021, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import com.cartlc.tracker.R
import com.cartlc.tracker.fresh.ui.app.factory.FactoryController
import com.cartlc.tracker.fresh.ui.app.factory.FactoryViewMvc
import com.cartlc.tracker.fresh.ui.base.BaseActivity
import com.cartlc.tracker.fresh.ui.bits.HideOnSoftKeyboard
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {

    companion object {
        private const val KEY_TAKING_PICTURE = "picture"
    }

    private val factoryViewMvc: FactoryViewMvc
        get() = componentRoot.factoryViewMvc
    private val factoryController: FactoryController
        get() = componentRoot.factoryController

    private lateinit var mainController: MainController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar_main))

        val content = findViewById<ViewGroup>(R.id.content)

        val mainViewMvc = factoryViewMvc.allocMainViewMvc(content, boundAct.factoryViewHelper)
        (mainViewMvc as MainViewMvcImpl).fabAdd = fab_add // Give access to top layout button

        val titleController = factoryController.allocTitleController(boundAct, mainViewMvc.titleViewMvc)
        val buttonController = factoryController.allocButtonsController(boundAct, mainViewMvc.buttonsViewMvc)

        mainController = factoryController.allocMainController(boundAct, mainViewMvc, titleController, buttonController)
        mainController.hideOnSoftKeyboard = HideOnSoftKeyboard(root)
        mainController.updateTitle = { updateTitle() }

        content.addView(mainViewMvc.rootView)

        updateTitle()
    }

    private fun updateTitle() {
        title = mainController.versionedTitle
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        mainController.handlePermissionResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mainController.onActivityResult(requestCode, resultCode, data)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (mainController.onOptionsItemSelected(item.itemId)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    // TODO: Do for real?
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(KEY_TAKING_PICTURE, mainController.onSaveInstanceState())
        super.onSaveInstanceState(outState)
    }

    // TODO: Do for real?
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        mainController.onRestoreInstanceState(savedInstanceState.getString(KEY_TAKING_PICTURE, null))
        super.onRestoreInstanceState(savedInstanceState)
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        mainController.onTrimMemory(level)
    }
}
