package com.cartlc.tracker.fresh.model.misc

import com.cartlc.tracker.fresh.model.core.table.TableString
import timber.log.Timber

class HashLongList(private val db: TableString) : HashSet<Long>() {

    companion object {
        private val TAG = HashLongList::class.simpleName
    }

    constructor(db: TableString, text: String) : this(db) {
        unmash(text)
    }

    fun add(text: String) {
        add(db.add(text))
    }

    fun set(text: String, selected: Boolean) {
        val id = db.add(text)
        if (selected) {
            add(id)
        } else {
            remove(id)
        }
    }

    fun has(text: String): Boolean {
        val id = db.add(text)
        return contains(id)
    }

    fun expand(): HashStringList {
        val list = HashStringList()
        for (ele in toList()) {
            db.query(ele)?.value?.let { list.add(it) }
        }
        return list
    }

    /**
     * Return a comma separated list of the number strings.
     */
    fun mash(): String {
        val sbuf = StringBuilder()
        var comma = false
        for (ele in toList().sorted()) {
            if (comma) {
                sbuf.append(",")
            } else {
                comma = true
            }
            sbuf.append(ele)
        }
        return sbuf.toString()
    }

    private fun unmash(text: String): HashLongList {
        clear()
        for (ele in text.split(",")) {
            if (ele.isNotBlank()) {
                try {
                    add(ele.toLong())
                } catch (ex: NumberFormatException) {
                    Timber.tag(TAG).e(ex)
                }
            }
        }
        return this
    }

    /**
     * Return a comma separated list of strings, if there
     * are not server ids associated with each of the strings.
     *
     * Otherwise if each string DOES have a server id, then
     * return a comma separated list of those ids.
     */
    fun serverMash(): String {
        val result = serverMashServerIds()
        if (result != null) {
            return result
        }
        return serverMashStrings()
    }

    private fun serverMashServerIds(): String? {
        val sbuf = StringBuilder()
        var comma = false
        for (ele in toList()) {
            db.query(ele)?.let {
                if (it.serverId > 0) {
                    if (comma) {
                        sbuf.append(",")
                    } else {
                        comma = true
                    }
                    sbuf.append(it.serverId)
                } else {
                    return null
                }
            } ?: return null
        }
        return sbuf.toString()
    }

    private fun serverMashStrings(): String {
        val sbuf = StringBuilder()
        var comma = false
        for (ele in expand().sorted()) {
            if (comma) {
                sbuf.append(",")
            } else {
                comma = true
            }
            sbuf.append(ele)
        }
        return sbuf.toString()
    }

}