/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.model.event

/**
 * Created by dug on 5/31/17.
 */

class EventPingStatus(
        val uploadsAllDone: Boolean = false,
        val noConnection: Boolean = false,
        val didWork: Boolean = false
): EventCommon()
