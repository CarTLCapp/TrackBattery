/**
 * Copyright 2020, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.main.process

import com.cartlc.tracker.fresh.model.core.data.DataFlowElement
import com.cartlc.tracker.fresh.model.core.data.DataFlowElement.Type
import com.cartlc.tracker.fresh.model.core.data.DataNote
import com.cartlc.tracker.fresh.model.flow.CustomFlow
import com.cartlc.tracker.fresh.ui.main.MainController
import com.cartlc.tracker.fresh.model.msg.StringMessage

class StageCustomFlow(
        shared: MainController.Shared,
        private val taskPicture: TaskPicture
) : ProcessBase(shared) {

    private var showingDialog = 0L

    private val isFlowComplete: Boolean
        get() = with(shared) { repo.isCurrentFlowEntryComplete }

    fun process() {
        with(shared) {
            repo.currentFlowElement?.let { element ->
                if (element.type == Type.SUB_FLOW_DIVIDER) {
                    buttonsUseCase.skip()
                } else {
                    process(element)
                }
            } ?: run {
                buttonsUseCase.skip()
            }
        }
    }

    private fun process(element: DataFlowElement) {
        with(shared) {
            val currentNumPictures: Int
            var hasNotes = false
            var hasPictures = false

            if (element.hasImages && !element.isConfirmType) {
                hasPictures = true
                picturesVisible = true
                pictureUseCase.pictureItems = db.tablePicture.removeFileDoesNotExist(
                        db.tablePicture.query(prefHelper.currentPictureCollectionId, repo.curFlowValueStage)
                )
                pictureUseCase.pictureNotes = getNotes(element.id)

                currentNumPictures = pictureUseCase.pictureItems.size

                titleUseCase.mainTitleText = getPhotoTitle(element.prompt, currentNumPictures, element.numImages.toInt())

                if (currentNumPictures < element.numImages) {
                    if (element.numImages > 1 || taskPicture.takingPictureAborted) {
                        buttonsUseCase.centerVisible = true
                        buttonsUseCase.centerText = messageHandler.getString(StringMessage.btn_another)
                    }
                    if (currentNumPictures == 0 && !taskPicture.takingPictureAborted) {
                        taskPicture.dispatchPictureRequest()
                    }
                }
            } else {
                currentNumPictures = 0
            }
            buttonsUseCase.nextVisible = ready

            // Process list -- if any
            when (element.type) {
                Type.CONFIRM,
                Type.CONFIRM_NEW -> {
                    mainListUseCase.visible = true
                    titleUseCase.mainTitleText = messageHandler.getString(StringMessage.title_confirm_checklist)
                    titleUseCase.mainTitleVisible = true
                }
                else -> {
                    if (!hasPictures && db.tableFlowElementNote.hasNotes(element.id)) {
                        titleUseCase.mainTitleText = messageHandler.getString(StringMessage.title_notes)
                        mainListUseCase.visible = true
                        hasNotes = true
                    } else {
                        mainListUseCase.visible = false
                    }
                }
            }

            // Process messages -- if any
            when (element.type) {
                Type.NONE -> {
                    if (!hasPictures && !hasNotes) {
                        buttonsUseCase.skip()
                    }
                }
                Type.TOAST ->
                    if (!taskPicture.takingPictureSuccess) {
                        getToastMessage(element, currentNumPictures)?.let {
                            screenNavigator.showToast(it)
                        }
                    }
                Type.DIALOG ->
                    if (showingDialog != element.id && !taskPicture.takingPictureSuccess) {
                        element.prompt?.let {
                            showingDialog = element.id
                            dialogNavigator.showDialog(it) {
                                showingDialog = 0L
                                if (!hasPictures && !hasNotes) {
                                    buttonsUseCase.skip()
                                }
                            }
                        }
                    }
                else -> {
                }
            }
            progress = computeProgress(element)
        }
    }

    private fun getNotes(elementId: Long): List<DataNote> {
        with(shared) {
            return db.noteHelper.getNotesOverlaidFrom(elementId, prefHelper.currentEditEntry)
        }
    }

    fun save(isNext: Boolean): Boolean {
        with(shared) {
            repo.currentFlowElement?.let { element ->
                val hasNotes = db.tableFlowElementNote.hasNotes(element.id)
                val notes = if (hasNotes) {
                    db.tableFlowElementNote.queryNotes(element.id)
                } else {
                    emptyList()
                }
                when (element.type) {
                    Type.NONE -> {
                        if (hasNotes) {
                            if (isNext && !mainListUseCase.areNotesComplete) {
                                dialogNavigator.showNoteError(notes)
                                return false
                            }
                        }
                    }
                    Type.CONFIRM,
                    Type.CONFIRM_NEW -> {
                        if (!mainListUseCase.isConfirmReady && isNext) {
                            screenNavigator.showToast(messageHandler.getString(StringMessage.error_need_all_checked))
                            return false
                        }
                    }
                    else -> {
                    }
                }
                if (hasNotes) {
                    prefHelper.currentEditEntry?.let {
                        db.tableCollectionNoteEntry.save(it.noteCollectionId, notes)
                    }
                }
                taskPicture.clearFlags()
            } ?: return true
        }
        return true
    }

    enum class MoveResult {
        MOVED,
        FLOW_ENDED,
        SUB_FLOW_ENDED
    }

    /**
     * Move to the next flow element.
     * @return false if there is no next flow element.
     */
    fun next(): MoveResult {
        return with(shared) {
            repo.currentFlowElement?.let { element ->
                if (db.tableFlowElement.hasSubFlows(element.flowId)) {
                    db.tableFlowElement.nextOfSubFlow(element.id)?.let {
                        repo.curFlowValue = CustomFlow(it)
                        MoveResult.MOVED
                    } ?: if (isFlowComplete) MoveResult.FLOW_ENDED else MoveResult.SUB_FLOW_ENDED
                } else {
                    db.tableFlowElement.next(element.id)?.let {
                        repo.curFlowValue = CustomFlow(it)
                        MoveResult.MOVED
                    } ?: MoveResult.FLOW_ENDED
                }
            } ?: MoveResult.FLOW_ENDED
        }
    }

    /**
     * Move to the previous flow element
     * @return false if there was no previous flow element.
     */
    fun prev(): MoveResult {
        return with(shared) {
            repo.currentFlowElement?.let { element ->
                if (db.tableFlowElement.hasSubFlows(element.flowId)) {
                    db.tableFlowElement.prevOfSubFlow(element.id)?.let {
                        repo.curFlowValue = CustomFlow(it)
                        MoveResult.MOVED
                    } ?: MoveResult.SUB_FLOW_ENDED
                } else {
                    db.tableFlowElement.prev(element.id)?.let {
                        repo.curFlowValue = CustomFlow(it)
                        MoveResult.MOVED
                    } ?: MoveResult.FLOW_ENDED
                }
            } ?: MoveResult.FLOW_ENDED
        }
    }

    fun pictureStateChanged() {
        with(shared) {
            buttonsUseCase.nextVisible = ready
            repo.currentFlowElement?.let { element ->
                val currentNumPictures = pictureUseCase.pictureItems.size
                if (element.numImages > 1 && currentNumPictures < element.numImages) {
                    buttonsUseCase.centerVisible = true
                    buttonsUseCase.centerText = messageHandler.getString(StringMessage.btn_another)
                }
                titleUseCase.mainTitleText = getPhotoTitle(element.prompt, currentNumPictures, element.numImages.toInt())
            }
        }
    }

    fun updateNextButtonVisible() {
        with(shared) {
            buttonsUseCase.nextVisible = ready
        }
    }

    private val ready: Boolean
        get() {
            with(shared) {
                repo.currentFlowElement?.let { element ->
                    val stage = repo.curFlowValueStage
                    val hasNotes = db.tableFlowElementNote.hasNotes(element.id)
                    if (hasNotes) {
                        when (element.type) {
                            Type.NONE -> {
                                if (!mainListUseCase.areNotesComplete) {
                                    return false
                                }
                            }
                            else -> {
                                if (element.hasImages) {
                                    if (!pictureUseCase.notesReady) {
                                        return false
                                    }
                                } else if (!repo.areNotesComplete(element)) {
                                    return false
                                }
                            }
                        }
                    }
                    val currentNumPictures =
                            db.tablePicture.countPictures(prefHelper.currentPictureCollectionId, stage)
                    if (currentNumPictures < element.numImages) {
                        return false
                    }
                }
                return true
            }
        }

    private fun getPhotoTitle(prompt: String?, count: Int, max: Int): String? {
        return with(shared) {
            if (count == max) {
                prompt
            } else if (max > 1 && count < max) {
                messageHandler.getString(StringMessage.title_photos(count, max))
            } else {
                null
            }
        }
    }

    private fun getToastMessage(element: DataFlowElement, count: Int): String? {
        with(shared) {
            val prompt = element.prompt ?: return null
            val max = element.numImages.toInt()
            if (max > 0) {
                if (max == 1 && count == 0 || count == max - 1) {
                    return messageHandler.getString(StringMessage.prompt_custom_photo_1(prompt))
                } else if (max > 1 && count == 0) {
                    return messageHandler.getString(StringMessage.prompt_custom_photo_N(max, prompt))
                } else if (max > 1 && count < max) {
                    return messageHandler.getString(StringMessage.prompt_custom_photo_more(max - count, prompt))
                }
            }
            val hasNotes = db.tableFlowElementNote.hasNotes(element.id)
            if (hasNotes) {
                val notesComplete = mainListUseCase.areNotesComplete
                if (!notesComplete) {
                    return messageHandler.getString(StringMessage.prompt_notes(prompt))
                }
            }
            if (max > 0 && max == count) {
                return null
            }
            return prompt
        }
    }

    private fun computeProgress(element: DataFlowElement): String? {
        return with(shared) {
            val prompt = repo.currentFlowElement?.let { element ->
                db.tableFlowElement.firstOfSubFlow(element.flowId, element.id)?.let { elementId ->
                    db.tableFlowElement.query(elementId)?.let { element ->
                        element.prompt
                    }
                }
            }
            val progress = shared.db.tableFlowElement.progressInSubFlow(element.id)?.let {
                "${it.first + 1}/${it.second}"
            }
            prompt?.let {
                "$prompt: $progress"
            } ?: run {
                progress
            }
        }
    }

    fun center() {
        taskPicture.clearFlags()
        taskPicture.dispatchPictureRequest()
    }
}