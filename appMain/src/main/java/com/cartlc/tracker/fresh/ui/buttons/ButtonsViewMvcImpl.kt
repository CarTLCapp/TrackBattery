package com.cartlc.tracker.fresh.ui.buttons

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.cartlc.tracker.R
import com.cartlc.tracker.fresh.ui.common.viewmvc.ObservableViewMvcImpl

class ButtonsViewMvcImpl(
        inflater: LayoutInflater,
        container: ViewGroup?
) : ObservableViewMvcImpl<ButtonsViewMvc.Listener>(), ButtonsViewMvc {

    override val rootView: View = inflater.inflate(R.layout.frame_buttons, container, false) as ViewGroup

    private val btnPrev = findViewById<Button>(R.id.btn_prev)
    private val btnNext = findViewById<Button>(R.id.btn_next)
    private val btnCenter = findViewById<Button>(R.id.btn_center)
    private val btnChange = findViewById<Button>(R.id.btn_change)

    init {
        btnPrev.setOnClickListener { view ->
            listeners.forEach { it.onBtnPrevClicked(view) }
        }
        btnNext.setOnClickListener { view ->
            listeners.forEach { it.onBtnNextClicked(view) }
        }
        btnCenter.setOnClickListener { view ->
            listeners.forEach { it.onBtnCenterClicked(view) }
        }
        btnChange.setOnClickListener { view ->
            listeners.forEach { it.onBtnChangeClicked(view) }
        }
    }

    override var showing: Boolean
        get() = rootView.visibility == View.VISIBLE
        set(value) {
            rootView.visibility = if (value) View.VISIBLE else View.GONE
        }

    override var btnPrevVisible: Boolean
        get() = btnPrev.visibility == View.VISIBLE
        set(value) {
            btnPrev.visibility = if (value) View.VISIBLE else View.INVISIBLE
        }

    override var btnNextVisible: Boolean
        get() = btnNext.visibility == View.VISIBLE
        set(value) {
            btnNext.visibility = if (value) View.VISIBLE else View.INVISIBLE
        }

    override var btnCenterVisible: Boolean
        get() = btnCenter.visibility == View.VISIBLE
        set(value) {
            btnCenter.visibility = if (value) View.VISIBLE else View.INVISIBLE
        }

    override var btnChangeVisible: Boolean
        get() = btnChange.visibility == View.VISIBLE
        set(value) {
            btnChange.visibility = if (value) View.VISIBLE else View.GONE
        }

    override var btnPrevText: String?
        get() = btnPrev.text.toString()
        set(value) {
            btnPrev.text = value
        }

    override var btnNextText: String?
        get() = btnNext.text.toString()
        set(value) {
            btnNext.text = value
        }

    override var btnCenterText: String?
        get() = btnCenter.text.toString()
        set(value) {
            btnCenter.text = value
        }

    // region supporting classes
    // endregion supporting classes
}