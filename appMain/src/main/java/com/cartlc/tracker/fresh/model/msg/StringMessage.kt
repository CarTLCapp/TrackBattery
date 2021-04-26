package com.cartlc.tracker.fresh.model.msg

import com.cartlc.tracker.R

sealed class StringMessage(
        val text: Int
) {

    object app_name: StringMessage(R.string.app_name)
    object entry_hint_edit_project : StringMessage(R.string.entry_hint_edit_project)
    object entry_hint_truck: StringMessage(R.string.entry_hint_truck)

    object btn_prev : StringMessage(R.string.btn_prev)
    object btn_next: StringMessage(R.string.btn_next)
    object btn_add: StringMessage(R.string.btn_add)
    object btn_edit: StringMessage(R.string.btn_edit)
    object btn_new_project: StringMessage(R.string.btn_new_project)
    object btn_another: StringMessage(R.string.btn_another)
    object btn_done: StringMessage(R.string.btn_done)
    object btn_confirm: StringMessage(R.string.btn_confirm)
    object btn_save: StringMessage(R.string.title_save)

    object error_picture_removed: StringMessage(R.string.error_picture_removed)
    object error_need_all_checked: StringMessage(R.string.error_need_all_checked)
    object error_has_no_flows: StringMessage(R.string.error_has_no_flows)

    object title_login: StringMessage(R.string.title_login)
    object title_root_project: StringMessage(R.string.title_root_project)
    object title_sub_project: StringMessage(R.string.title_sub_project)
    object title_sub_flows: StringMessage(R.string.title_sub_flows)
    object title_company: StringMessage(R.string.title_company)
    object title_state: StringMessage(R.string.title_state)
    object title_city: StringMessage(R.string.title_city)
    object title_street: StringMessage(R.string.title_street)
    object title_current_project: StringMessage(R.string.title_current_project)
    object title_element: StringMessage(R.string.title_element)
    object title_truck: StringMessage(R.string.title_truck)
    object title_truck_number: StringMessage(R.string.title_truck_number_)
    object title_truck_damage: StringMessage(R.string.title_truck_damage_)
    object title_equipment_installed: StringMessage(R.string.title_equipment_installed)
    object title_equipment: StringMessage(R.string.title_equipment)
    object title_notes: StringMessage(R.string.title_notes)
    object title_status: StringMessage(R.string.title_status)
    object title_confirmation: StringMessage(R.string.title_confirmation)
    object title_confirm_checklist: StringMessage(R.string.title_confirm_checklist)
    object title_photo: StringMessage(R.string.title_photo)
    object title_entries_: StringMessage(R.string.title_entries_)
    object title_uploaded_: StringMessage(R.string.title_uploaded_)
    object title_uploaded_done: StringMessage(R.string.title_uploaded_done)
    object title_saved_: StringMessage(R.string.title_saved_)
    object title_update: StringMessage(R.string.title_update)
    object truck_number_request: StringMessage(R.string.truck_number_request)
    object truck_number_enter: StringMessage(R.string.truck_number_enter)
    object truck_number_hint: StringMessage(R.string.truck_number_hint)

    object truck_damage_query: StringMessage(R.string.truck_damage_query)
    object truck_damage_request: StringMessage(R.string.truck_damage_request)
    object truck_damage_enter: StringMessage(R.string.truck_damage_enter)
    object truck_damage_hint: StringMessage(R.string.truck_damage_hint)

    object daar_title: StringMessage(R.string.daar_title)
    object daar_sub_title: StringMessage(R.string.daar_sub_title)
    object daar_instruction_date: StringMessage(R.string.daar_instruction_date)
    object daar_instruction_project: StringMessage(R.string.daar_instruction_project)
    object daar_instruction_work_completed: StringMessage(R.string.daar_instruction_work_completed)
    object daar_instruction_missed_units: StringMessage(R.string.daar_instruction_missed_units)
    object daar_instruction_issues: StringMessage(R.string.daar_instruction_issues)
    object daar_instruction_injuries: StringMessage(R.string.daar_instruction_injuries)
    object daar_starting_time_tomorrow: StringMessage(R.string.daar_instruction_starting_time_tomorrow)
    object daar_entry_hint: StringMessage(R.string.daar_entry_hint)
    object daar_entry_date: StringMessage(R.string.daar_date_hint)
    object daar_entry_time: StringMessage(R.string.daar_time_hint)

    data class dialog_dialog_entry_done(val name: String): StringMessage(R.string.dialog_entry_done)
    data class dialog_dialog_entry_done2(val name: String, val name2: String): StringMessage(R.string.dialog_entry_done2)
    data class prompt_custom_photo_1(val prompt: String): StringMessage(R.string.prompt_custom_photo_1)
    data class prompt_custom_photo_N(val count: Int, val prompt: String): StringMessage(R.string.prompt_custom_photo_N)
    data class prompt_custom_photo_more(val count: Int, val prompt: String): StringMessage(R.string.prompt_custom_photo_more)
    data class prompt_notes(val prompt: String): StringMessage(R.string.prompt_notes)
    data class note_incomplete(val progress: Int, val size: Int = 0): StringMessage(R.string.entry_incomplete)

    data class title_elements(val count: Int): StringMessage(R.string.title_elements)
    data class title_photos(val count: Int, val max: Int): StringMessage(R.string.title_photos)

    data class status_installed_equipments(val checkedEquipment: Int, val maxEquip: Int) : StringMessage(R.string.status_installed_equipments)
    data class status_installed_pictures(val countPictures: Int) : StringMessage(R.string.status_installed_pictures)
    data class status_notes_used(val countNotes: Int): StringMessage(R.string.status_notes_used)

    data class error_incorrect_note_count(val length: Int, val digits: Int): StringMessage(R.string.error_incorrect_note_count)
    data class error_incorrect_digit_count(val msg: String): StringMessage(R.string.error_incorrect_digit_count)
}
