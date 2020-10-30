package com.cartlc.tracker.fresh.ui.login

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.cartlc.tracker.fresh.model.SchedulerPlan
import com.cartlc.tracker.fresh.model.event.Action
import com.cartlc.tracker.fresh.model.event.Button
import com.cartlc.tracker.fresh.model.flow.ActionUseCase
import com.cartlc.tracker.fresh.model.flow.Flow
import com.cartlc.tracker.fresh.model.flow.FlowUseCase
import com.cartlc.tracker.fresh.model.flow.Stage
import com.cartlc.tracker.fresh.model.msg.MessageHandler
import com.cartlc.tracker.fresh.model.msg.StringMessage
import com.cartlc.tracker.fresh.service.alarm.AlarmController
import com.cartlc.tracker.fresh.service.endpoint.DCServerRx
import com.cartlc.tracker.fresh.ui.app.TBApplication
import com.cartlc.tracker.fresh.ui.app.dependencyinjection.BoundFrag
import com.cartlc.tracker.fresh.ui.buttons.ButtonsUseCase
import com.cartlc.tracker.fresh.ui.common.rx.RxDisposables

class LoginController(
        boundFrag: BoundFrag,
        private val viewMvc: LoginViewMvc,
        private val buttonsUseCase: ButtonsUseCase,
        private val dcRx: DCServerRx,
        private val schedulerPlan: SchedulerPlan
) : LifecycleObserver,
        LoginUseCase,
        LoginViewMvc.Listener,
        FlowUseCase.Listener,
        ActionUseCase.Listener,
        ButtonsUseCase.Listener {

    private val repo = boundFrag.repo
    private val context = boundFrag.act
    private val prefHelper = repo.prefHelper
    private val messageHandler: MessageHandler = boundFrag.componentRoot.messageHandler
    private val dialogHelper = boundFrag.dialogHelper
    private val curFlowValue: Flow
        get() = repo.curFlowValue
    private var firstCodeEdit: String = ""
    private var secondaryCodeEdit: String = ""
    private var isSecondaryPromptsEnabled = false
    private val disposables = RxDisposables()

    init {
        boundFrag.bindObserver(this)
    }

    private val loginValid: Boolean
        get() {
            if (isSecondaryPromptsEnabled) {
                if (secondaryCodeEdit.isBlank()) {
                    return false
                }
            }
            return firstCodeEdit.isNotBlank()
        }

    private val loginSuccess: Boolean
        get() {
            return prefHelper.techID != 0
        }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        repo.flowUseCase.registerListener(this)
        viewMvc.registerListener(this)
        firstCodeEdit = prefHelper.firstTechCode ?: ""
        secondaryCodeEdit = prefHelper.secondaryTechCode ?: ""
        isSecondaryPromptsEnabled = prefHelper.hasSecondary
        viewMvc.firstTechCode = firstCodeEdit
        viewMvc.firstTechName = prefHelper.techName
        viewMvc.secondaryTechCode = secondaryCodeEdit
        viewMvc.secondaryTechName = prefHelper.secondaryTechName
        viewMvc.secondaryCheckBoxChecked = isSecondaryPromptsEnabled
        viewMvc.secondaryTechCodeEnabled = isSecondaryPromptsEnabled
        repo.actionUseCase.registerListener(this)
        onStageChangedAboutTo(repo.flowUseCase.curFlow)
        onStageChanged(repo.flowUseCase.curFlow)
        buttonsUseCase.registerListener(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        viewMvc.unregisterListener(this)
        repo.flowUseCase.unregisterListener(this)
        repo.actionUseCase.unregisterListener(this)
        buttonsUseCase.unregisterListener(this)
        disposables.dispose()
    }

    override fun onActionChanged(action: Action) {
    }

    // region FlowUseCase.Listener

    override fun onStageChangedAboutTo(flow: Flow) {
    }

    override fun onStageChanged(flow: Flow) {
        when (flow.stage) {
            Stage.LOGIN -> {
                buttonsUseCase.prevVisible = false
                buttonsUseCase.centerText = messageHandler.getString(StringMessage.title_login)
                buttonsUseCase.nextVisible = loginSuccess
                detectShowCenter()
            }
            else -> {
            }
        }
    }

    // endregion FlowUseCase.Listener

    // region ButtonsUseCase.Listener

    override fun onButtonConfirm(action: Button): Boolean {
        return true
    }

    override fun onButtonEvent(action: Button) {
        when (curFlowValue.stage) {
            Stage.LOGIN -> {
                when (action) {
                    Button.BTN_CENTER -> {
                        login()
                    }
                    Button.BTN_NEXT -> {
                        next()
                    }
                    else -> {
                    }
                }
            }
            else -> {
            }
        }
    }

    private fun login() {
        val firstCode = firstCodeEdit
        val secondCode = if (isSecondaryPromptsEnabled) {
            secondaryCodeEdit
        } else {
            null
        }
        @Suppress("UNUSED_PARAMETER")
        disposables.add(
                dcRx.sendRegistration(firstCode, secondCode)
                        .subscribeOn(schedulerPlan.subscribeWith)
                        .observeOn(schedulerPlan.observeWith)
                        .subscribe { result: DCServerRx.Result ->
                            when {
                                result.errorMessage != null -> {
                                    TBApplication.ShowError(result.errorMessage)
                                    buttonsUseCase.nextVisible = false
                                    viewMvc.firstTechName = ""
                                    viewMvc.secondaryTechName = ""
                                }
                                loginSuccess -> {
                                    buttonsUseCase.nextVisible = true
                                    buttonsUseCase.centerVisible = false
                                    viewMvc.firstTechName = prefHelper.techName
                                    viewMvc.secondaryTechName = prefHelper.secondaryTechName
                                    if (prefHelper.secondaryTechName.isNotBlank()) {
                                        dialogHelper.showMessage(
                                                messageHandler.getString(StringMessage.dialog_dialog_entry_done2(
                                                        prefHelper.techName,
                                                        prefHelper.secondaryTechName
                                                ))
                                        )
                                    } else {
                                        dialogHelper.showMessage(
                                                messageHandler.getString(StringMessage.dialog_dialog_entry_done(prefHelper.techName))
                                        )
                                    }
                                    AlarmController.justLoggedIn(context)
                                }
                                else -> {
                                    buttonsUseCase.nextVisible = false
                                    viewMvc.firstTechName = ""
                                    viewMvc.secondaryTechName = ""
                                }
                            }
                        }
        )
    }

    private fun next() {
        repo.onPreviousFlow()
    }

    // endregion ButtonsUseCase.Listener

    override fun onFirstTechCodeChanged(value: String) {
        firstCodeEdit = value
        viewMvc.firstTechName = ""
        detectShowCenter()
    }

    override fun onSecondaryTechCodeChanged(value: String) {
        secondaryCodeEdit = value
        viewMvc.secondaryTechName = ""
        detectShowCenter()
    }

    override fun onSecondaryCheckBoxChanged(value: Boolean) {
        secondaryCodeEdit = ""
        isSecondaryPromptsEnabled = value

        viewMvc.secondaryTechName = ""
        viewMvc.secondaryTechCode = ""
        viewMvc.secondaryTechCodeEnabled = isSecondaryPromptsEnabled

        detectShowCenter()
    }

    private fun detectShowCenter() {
        buttonsUseCase.centerVisible = loginValid
    }

}