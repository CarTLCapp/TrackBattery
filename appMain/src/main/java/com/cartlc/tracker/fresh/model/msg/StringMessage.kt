package com.cartlc.tracker.fresh.model.msg

sealed class StringMessage {

    object app_name: StringMessage()

    object entry_hint_edit_project : StringMessage()
    object entry_hint_truck: StringMessage()

    object btn_prev : StringMessage()
    object btn_next: StringMessage()
    object btn_add: StringMessage()
    object btn_edit: StringMessage()
    object btn_new_project: StringMessage()
    object btn_another: StringMessage()
    object btn_done: StringMessage()
    object btn_confirm: StringMessage()

    object error_picture_removed: StringMessage()
    object error_need_all_checked: StringMessage()
    object error_has_no_flows: StringMessage()

    object title_login: StringMessage()
    object title_root_project: StringMessage()
    object title_sub_project: StringMessage()
    object title_sub_flows: StringMessage()
    object title_company: StringMessage()
    object title_state: StringMessage()
    object title_city: StringMessage()
    object title_street: StringMessage()
    object title_current_project: StringMessage()
    object title_element: StringMessage()
    object title_truck: StringMessage()
    object title_truck_number: StringMessage()
    object title_truck_damage: StringMessage()
    object title_equipment_installed: StringMessage()
    object title_equipment: StringMessage()
    object title_notes: StringMessage()
    object title_status: StringMessage()
    object title_confirmation: StringMessage()
    object title_confirm_checklist: StringMessage()
    object title_photo: StringMessage()
    object title_entries_: StringMessage()
    object title_uploaded_: StringMessage()
    object title_uploaded_done: StringMessage()
    object title_saved_: StringMessage()

    object truck_number_request: StringMessage()
    object truck_number_enter: StringMessage()
    object truck_number_hint: StringMessage()
    object truck_damage_query: StringMessage()
    object truck_damage_request: StringMessage()
    object truck_damage_enter: StringMessage()
    object truck_damage_hint: StringMessage()

    data class dialog_dialog_entry_done(val name: String): StringMessage()
    data class dialog_dialog_entry_done2(val name: String, val name2: String): StringMessage()
    data class prompt_custom_photo_1(val prompt: String): StringMessage()
    data class prompt_custom_photo_N(val count: Int, val prompt: String): StringMessage()
    data class prompt_custom_photo_more(val count: Int, val prompt: String): StringMessage()
    data class prompt_notes(val prompt: String): StringMessage()
    data class note_incomplete(val progress: Int, val size: Int = 0): StringMessage()

    data class title_elements(val count: Int): StringMessage()
    data class title_photos(val count: Int, val max: Int): StringMessage()

    data class status_installed_equipments(val checkedEquipment: Int, val maxEquip: Int) : StringMessage()
    data class status_installed_pictures(val countPictures: Int) : StringMessage()
    data class status_notes_used(val countNotes: Int): StringMessage()

    data class error_incorrect_note_count(val length: Int, val digits: Int): StringMessage()
    data class error_incorrect_digit_count(val msg: String): StringMessage()
}