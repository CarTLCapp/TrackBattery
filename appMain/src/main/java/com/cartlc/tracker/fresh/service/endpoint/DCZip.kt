/*
 * *
 *   * Copyright 2019, FleetTLC. All rights reserved
 *
 */
package com.cartlc.tracker.fresh.service.endpoint

import android.net.Uri

import com.cartlc.tracker.fresh.ui.app.TBApplication
import com.cartlc.tracker.fresh.model.core.data.DataZipCode
import com.cartlc.tracker.fresh.model.core.sql.SqlTableTruck
import com.cartlc.tracker.fresh.model.core.table.DatabaseTable

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

import timber.log.Timber

/**
 * Created by dug on 8/24/17.
 */

class DCZip(
        private val db: DatabaseTable
) : DCPost() {

    companion object {
        internal val AUTHORITY = "maps.googleapis.com"
        private val TAG = DCZip::class.simpleName
    }

    internal enum class ObjectType {
        IGNORED,
        CITY,
        STATE,
        ZIPCODE;

        companion object {

            @Throws(JSONException::class)
            fun from(types: JSONArray): ObjectType {
                for (i in 0 until types.length()) {
                    if ("locality" == types.getString(i)) {
                        return CITY
                    }
                    if ("administrative_area_level_1" == types.getString(i)) {
                        return STATE
                    }
                    if ("postal_code" == types.getString(i)) {
                        return ZIPCODE
                    }
                }
                return IGNORED
            }
        }
    }

    fun findZipCode(zipcode: String) {
        try {
            val builder = Uri.Builder()
            builder.scheme("http")
                    .authority(AUTHORITY)
                    .appendPath("maps")
                    .appendPath("api")
                    .appendPath("geocode")
                    .appendPath("json")
                    .appendQueryParameter("address", zipcode)
                    .appendQueryParameter("sensor", "true")
            val url = URL(builder.build().toString())
            val result = post(url)
            val data = DataZipCode()
            val root = JSONObject(result)
            val results = root.getJSONArray("results")
            if (results.length() == 0) {
                return
            }
            val result0 = results.getJSONObject(0)
            val components = result0.getJSONArray("address_components")
            for (i in 0 until components.length()) {
                val ele = components.getJSONObject(i)
                val types = ele.getJSONArray("types")
                val objType = ObjectType.from(types)
                if (objType == ObjectType.CITY) {
                    data.city = ele.getString("long_name")
                } else if (objType == ObjectType.STATE) {
                    data.stateLongName = ele.getString("long_name")
                    data.stateShortName = ele.getString("short_name")
                } else if (objType == ObjectType.ZIPCODE) {
                    data.zipCode = ele.getString("long_name")
                }
            }
            data.check()

            if (data.isValid) {
                db.tableZipCode.add(data)
            } else {
                Timber.tag(TAG).e("Invalid zipcode response: $result")
            }
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, DCZip::class.java, "findZipCode()", zipcode)
        }
    }

    @Throws(IOException::class)
    internal fun post(url: URL): String {
        val connection = url.openConnection() as HttpURLConnection
        val result = getResult(connection)
        connection.disconnect()
        return result
    }

}
