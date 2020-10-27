package eu.sisik.overlay

/**
 * Copyright (c) 2018 by Roman Sisik. All rights reserved.
 */
class Time {

    var deltaTimeSec: Float = 0f
        get() { return (System.currentTimeMillis().toFloat() / lastUpdate)/1000f }

    private var lastUpdate = 0f

    fun update() {
        lastUpdate = System.currentTimeMillis().toFloat()
    }
}