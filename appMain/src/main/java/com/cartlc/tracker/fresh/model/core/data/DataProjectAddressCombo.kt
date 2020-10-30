/**
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.model.core.data

import com.cartlc.tracker.fresh.model.core.table.DatabaseTable
import timber.log.Timber

/**
 * Created by dug on 5/10/17.
 */
class DataProjectAddressCombo : Comparable<DataProjectAddressCombo> {

    companion object {
        private val TAG = DataProjectAddressCombo::class.simpleName

        fun sort(list: List<DataProjectAddressCombo>): List<DataProjectAddressCombo> {
            return list.toMutableList().sortedBy { it.projectDashName}
        }
    }

    constructor(db: DatabaseTable, projectNameId: Long, addressId: Long) {
        this.db = db
        this.projectNameId = projectNameId
        this.addressId = addressId
    }

    constructor(db: DatabaseTable, rowId: Long, projectNameId: Long, addressId: Long) {
        this.db = db
        this.id = rowId
        this.projectNameId = projectNameId
        this.addressId = addressId
    }

    var id: Long = 0
    var projectNameId: Long
    var addressId: Long

    private var mAddress: DataAddress? = null
    private var db: DatabaseTable

    val projectDashName: String
        get() {
            val name = db.tableProjects.queryProjectName(projectNameId) ?: return "-"
            val rootProject = name.first
            val subProject = name.second
            if (subProject.isBlank()) {
                return rootProject
            }
            return "$rootProject - $subProject"
        }

    val isRootProject: Boolean
        get() {
            val name = db.tableProjects.queryProjectName(projectNameId) ?: return false
            return name.second.isEmpty()
        }

    val rootName: String
        get() {
            val name = db.tableProjects.queryProjectName(projectNameId) ?: return ""
            return name.first
        }

    val projectName: Pair<String, String>?
        get() = db.tableProjects.queryProjectName(projectNameId)

    val project: DataProject?
        get() = db.tableProjects.queryById(projectNameId)

    val address: DataAddress?
        get() {
            if (mAddress == null) {
                mAddress = db.tableAddress.query(addressId)
                if (mAddress == null) {
                    Timber.tag(TAG).e("Could not find address ID=$addressId")
                }
            }
            return mAddress
        }

    val addressLine: String
        get() {
            val address = address
            return address?.line ?: "Invalid"
        }

    val companyName: String?
        get() {
            val address = address
            return address?.company
        }

    val entries: List<DataEntry>
        get() {
            return sortIncompleteToTop(if (isRootProject) {
                val list = mutableListOf<DataEntry>()
                for (item in db.tableProjectAddressCombo.query()) {
                    if (!item.isRootProject && item.rootName == rootName && item.addressId == addressId) {
                        list.addAll(db.tableEntry.queryForProjectAddressCombo(item.id))
                    }
                }
                list
            } else {
                db.tableEntry.queryForProjectAddressCombo(id)
            })
        }

    fun reset(projectNameId: Long, addressId: Long) {
        this.projectNameId = projectNameId
        this.addressId = addressId
        mAddress = null
    }

    override fun compareTo(other: DataProjectAddressCombo): Int {
        val name = projectDashName
        val otherName = other.projectDashName
        return name.compareTo(otherName)
    }

    override fun toString(): String {
        val sbuf = StringBuilder()
        sbuf.append("ID=")
        sbuf.append(id)
        sbuf.append(", PROJID=")
        sbuf.append(projectNameId)
        sbuf.append(" [")
        sbuf.append(projectName)
        sbuf.append("] ADDRESS=")
        sbuf.append(addressId)
        if (address != null) {
            sbuf.append(" [")
            sbuf.append(address!!.block)
            sbuf.append("]")
        }
        return sbuf.toString()
    }

    private fun sortIncompleteToTop(list: List<DataEntry>): List<DataEntry> {
        val result = mutableListOf<DataEntry>()
        result.addAll(list.filter { !it.isComplete })
        result.addAll(list.filter { it.isComplete })
        return result
    }

}
