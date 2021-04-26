/**
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.ui.util.helper

import android.Manifest
import android.app.Activity
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.AsyncTask
import android.text.TextUtils
import android.util.Log

import com.cartlc.tracker.fresh.ui.app.TBApplication
import com.cartlc.tracker.fresh.model.core.data.DataAddress
import com.cartlc.tracker.fresh.model.core.data.DataStates
import com.cartlc.tracker.fresh.model.core.table.DatabaseTable
import com.cartlc.tracker.fresh.ui.common.PermissionUseCase
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

import org.json.JSONException
import org.json.JSONObject

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.ArrayList
import java.util.HashSet
import java.util.Locale

/**
 * Find the location the user is at and return the best street address we can find.
 * Created by dug on 3/12/18.
 */
class LocationHelper(
        private val mApp: TBApplication,
        private val db: DatabaseTable
) {

    companion object {

        const val GOOGLE_LOC = "http://maps.googleapis.com/maps/api/geocode/json?"

        private val TAG = LocationHelper::class.java.simpleName
        private const val LOG = false

        lateinit var instance: LocationHelper
            private set

        fun Init(app: TBApplication, db: DatabaseTable) {
            LocationHelper(app, db)
        }
    }

    private val mGeocoder: Geocoder
    private var mLocationSelector: Int = 0
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var mReportMap: HashSet<Long> = HashSet()

    interface OnLocationCallback {
        fun onLocationUpdate(address: Address)
    }

    private inner class GetAddressTask(var mCallback: OnLocationCallback?) : AsyncTask<Location, Void, Address>() {

        override fun doInBackground(vararg locs: Location): Address? {
            val loc = locs[0]
            var address: Address?
            address = getAddressFromDatabase(loc)
            if (address != null) {
                return address
            }
            address = getAddressFromGeocoder(loc)
            return if (address != null) {
                storeAddressToDatabase(loc, address)
            } else storeAddressToDatabase(loc, getAddressFromNetwork(loc))
        }

        /**
         * Lookup the address in private database.
         * The reason is that there is a limit to the number of times you can hit Google's geocacher.
         */
        @Suppress("UNUSED_PARAMETER")
        fun getAddressFromDatabase(location: Location): Address? {
            return null
        }

        @Suppress("UNUSED_PARAMETER")
        fun storeAddressToDatabase(location: Location, address: Address?): Address? {
            return address
        }

        fun getAddressFromGeocoder(location: Location?): Address? {
            if (location == null) {
                return null
            }
            try {
                val addressList = mGeocoder.getFromLocation(
                        location.latitude, location.longitude, 1)
                if (addressList != null && addressList.size > 0) {
                    return addressList[0]
                }
            } catch (ex: Exception) {
                Log.e(TAG, "GEOCODER:" + ex.message)
            }

            return null
        }

        fun getAddressFromNetwork(location: Location?): Address? {
            if (location == null) {
                return null
            }
            val list = getFromLocation(
                    location.latitude, location.longitude, 1)
            return if (list == null || list.size == 0) {
                null
            } else list[0]
        }

        @Suppress("UNUSED_PARAMETER")
        fun getFromLocation(lat: Double, lng: Double, maxResult: Int): List<Address>? {
            val buildUri = Uri.parse(GOOGLE_LOC).buildUpon().appendQueryParameter("latlng",
                    lat.toString() + "," + lng.toString()).appendQueryParameter("sensor", "true").appendQueryParameter("language", Locale.ENGLISH
                    .language).build()
            var urlConnection: HttpURLConnection?
            val stringBuilder = StringBuilder()
            var retList: MutableList<Address>? = null

            if (LOG) {
                Log.i(TAG, "doing query from google")
            }
            try {
                val url = URL(buildUri.toString())
                urlConnection = url.openConnection() as HttpURLConnection
                urlConnection.requestMethod = "GET"
                urlConnection.connect()
                val stream = urlConnection.inputStream
                var b: Int
                while (true) {
                    b = stream.read()
                    if (b == -1) {
                        break
                    }
                    stringBuilder.append(b.toChar())
                }
                val jsonObject: JSONObject

                jsonObject = JSONObject(stringBuilder.toString())

                retList = ArrayList()
                val status = jsonObject.getString("status")
                if ("OK".equals(status, ignoreCase = true)) {
                    val results = jsonObject.getJSONArray("results")
                    for (i in 0 until results.length()) {
                        val result = results.getJSONObject(i)
                        val indiStr = result.getString("formatted_address")
                        val addr = Address(Locale.getDefault())
                        addr.setAddressLine(0, indiStr)
                        val components = result.getJSONArray("address_components")
                        for (j in 0 until components.length()) {
                            val component = components.getJSONObject(j)
                            val types = component.getJSONArray("types")

                            if (types.length() == 0) {
                                continue
                            }
                            val type = types.getString(0)
                            // String long_name = component.getTableString("long_name");
                            val short_name = component.getString("short_name")

                            if ("street_number" == type) {
                                addr.thoroughfare = short_name
                            } else if ("route" == type) {
                                addr.featureName = short_name
                            } else if ("locality" == type) {
                                addr.locality = short_name
                            } else if ("administrative_area_level_2" == type) {
                                addr.subAdminArea = short_name
                            } else if ("administrative_area_level_1" == type) {
                                addr.adminArea = short_name
                            } else if ("country" == type) {
                                addr.countryName = short_name
                            } else if ("postal_code" == type) {
                                addr.postalCode = short_name
                            } else if ("neighborhood" == type) {
                                addr.subThoroughfare = short_name
                            }
                        }
                        retList.add(addr)
                    }
                } else {
                    Log.e(TAG, "google geocode returned: $status")
                }
            } catch (e: IOException) {
                Log.e(TAG, "Geocode parsing error: IOException: ", e)
            } catch (e: JSONException) {
                Log.e(TAG, "TAG,Geocode parsing error: JSONException: ", e)
            } catch (e: Exception) {
                Log.e(TAG, "Geocode parsing error for address $buildUri, ERROR: ", e)
            }

            return retList
        }

        override fun onPostExecute(address: Address?) {
            if (address != null) {
                if (LOG) {
                    Log.i(TAG, "Got address=" + address.toString())
                }
                if (mCallback != null) {
                    mCallback!!.onLocationUpdate(address)
                    infoReport(address)
                }
            }
        }

    }

    init {
        instance = this
        mGeocoder = Geocoder(mApp)
        mLocationSelector = 1
    }


    fun match(string1: String?, string2: String?): Boolean {
        return if (string1 == null || string2 == null) {
            false
        } else string1.compareTo(string2, ignoreCase = true) == 0
    }

    fun matchState(address: Address, state: String?): Boolean {
        val stateFull = DataStates.getAbbr(state!!)
        val addressFull = DataStates.getAbbr(address.adminArea)
        return match(addressFull, stateFull)
    }

    fun matchState(address: Address, states: List<String>): String? {
        return match(address.adminArea, states)
    }

    private fun matchCity(address: Address, city: String?): Boolean {
        return match(address.locality, city)
    }

    fun matchCity(address: Address, cities: List<String>): String? {
        return match(address.locality, cities)
    }

    fun reduceStreets(address: Address, streets: List<String>): List<String> {
        val reduced = ArrayList<String>()
        val thoroughfare1: String? = address.thoroughfare
        val thoroughfare: String = thoroughfare1 ?: return streets
        val compareWith = stripNumbers(replaceOrdinals(thoroughfare))
        for (street in streets) {
            if (replaceOrdinals(stripNumbers(street)).compareTo(compareWith, ignoreCase = true) == 0) {
                reduced.add(street)
            }
        }
        return reduced
    }

    private fun stripNumbers(line: String): String {
        return line.replace("[0-9\\s]+".toRegex(), "")
    }

    private fun replaceOrdinals(line: String): String {
        var result = line.replace("North", "N")
        result = result.replace("South", "S")
        result = result.replace("West", "W")
        result = result.replace("East", "E")
        return result
    }

    private fun match(match: String, items: List<String>): String? {
        return if (hasMatch(items, match)) {
            match
        } else null
    }

    private fun hasMatch(items: List<String>, match: String): Boolean {
        for (item in items) {
            if (item.compareTo(match, ignoreCase = true) == 0) {
                return true
            }
        }
        return false
    }

    private fun requestAddress(location: Location?, callback: OnLocationCallback) {
        location?.let {
            val task = GetAddressTask(callback)
            task.execute(it)
        }
    }

    fun requestLocation(act: Activity, callback: OnLocationCallback) {
        if (mFusedLocationClient == null) {
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(act)
        }
        mApp.checkPermissions(act, object : PermissionUseCase.Listener {
            override fun onSuccess() {
                getLocation(act, callback)
            }

            override fun onDenied(onExit: Boolean) {
            }
        })
    }

    fun onDestroy() {
        mFusedLocationClient = null
    }

    @Throws(SecurityException::class)
    private fun getLocation(act: Activity, callback: OnLocationCallback) {
        mFusedLocationClient?.lastLocation?.addOnSuccessListener(act) { location ->
            mFusedLocationClient?.let {
                requestAddress(location, callback)
            }
        }
    }

    private fun hasState(address: Address): Boolean {
        return !TextUtils.isEmpty(address.adminArea)
    }

    private fun hasCity(address: Address): Boolean {
        return !TextUtils.isEmpty(address.locality)
    }

    fun matchCompany(address: Address, company: DataAddress): Boolean {
        if (instance.hasState(address)) {
            if (!instance.matchState(address, company.state)) {
                return false
            }
        }
        if (instance.hasCity(address)) {
            if (!instance.matchCity(address, company.city)) {
                return false
            }
        }
        return true
    }

    fun infoReport(address: Address) {
        if (TBApplication.REPORT_LOCATION) {
            var code = address.adminArea.hashCode().toLong()
            code += address.locality.hashCode().toLong()
            if (!mReportMap.contains(code)) {
                mReportMap.add(code)
                db.tableCrash.info("LOCATION: " + address.toString())
            }
            Log.i(TAG, address.toString())
        }
    }

}
