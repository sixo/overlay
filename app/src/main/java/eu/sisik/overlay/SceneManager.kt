package eu.sisik.overlay

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.Matrix

/**
 * Copyright (c) 2018 by Roman Sisik. All rights reserved.
 */

class SceneManager(context: Context) {

    private val time = Time()

    // Bugjaeger hunts the bug. I made it a bit faster, so that
    // it can actually catch the bug.
    private var bugjaegerSpeed = 220f
    private var bugjaegerRenderer: TextureRenderer
    private var bugjaegerFile = "bugjaeger_icon.png"
    private var bugjaegerBehaviour = SteeringBehaviour()

    // Bug tries to run to a random location. Unfortunately, it's not fast
    // enough to escape from Bugjaeger.
    private var bugSpeed = 200f
    private var bugRenderer: TextureRenderer
    private var bugFile = "baseline_bug_report_black_48dp.png"
    private var bugBehaviour = SteeringBehaviour()
    private var bugTarget = Vec3(0f, 0f, 0f)

    // If we come within this distance to target, we actually reached
    // the target
    private var reachDistance = 2f

    // Scale for both - Bugjaeger and bug
    private var scale = 2f

    // Camera properties
    private var camDistance = 20f
    private var fovY = 60f

    // Boundaries of the visible area
    private var top = 0f
    private var bottom = 0f
    private var right = 0f
    private var left = 0f

    // Used to calculate final MVP matrix
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)


    init {

        bugjaegerRenderer = TextureRenderer(BitmapFactory.decodeStream(
            context.assets.open(bugjaegerFile)))

        bugRenderer = TextureRenderer(BitmapFactory.decodeStream(
            context.assets.open(bugFile)))

        bugjaegerBehaviour.speed = bugjaegerSpeed
        bugBehaviour.speed = bugSpeed
    }

    fun setCamera(width: Int, height: Int) {

        // Set perspective projection
        val aspect: Float = width.toFloat() / height.toFloat()
        Matrix.perspectiveM(projectionMatrix, 0, fovY, aspect, 1f, 100f)

        // Position camera
        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, -camDistance, 0f, 0f, 0f, 0f, 1.0f, 0.0f)

        // Calculate visible area boundaries within frustum
        calculateBoundaries(aspect)

        initRandomPositions()
    }

    fun calculateBoundaries(aspect: Float) {

        val frustumHeight = 2.0f * camDistance * Math.tan(Math.toRadians(fovY * 0.5)).toFloat()
        val frustumWidth = frustumHeight * aspect

        top = frustumHeight/2f
        bottom = -top
        left = frustumWidth/2f
        right = -left
    }

    fun initRandomPositions() {

        bugjaegerBehaviour.position = getRandomPosition()
        bugBehaviour.position = getRandomPosition()
        bugTarget = getRandomPosition()
    }

    private fun getRandomPosition(): Vec3 {

        val x = left + Math.random() * (right - left)
        val y = bottom + Math.random() * (top - bottom)

        return Vec3(x.toFloat(), y.toFloat(), 0f)
    }

    fun update() {

        updateBugjaeger()
        updateBug()

        time.update()
    }

    private fun updateBugjaeger() {

        bugjaegerBehaviour.pursue(bugBehaviour.position,  bugBehaviour.velocity * -1f, time.deltaTimeSec)

        val pos = bugjaegerBehaviour.position
        val mvp = getMVP(pos.x, pos.y, pos.z)

        bugjaegerRenderer.draw(mvp)
    }

    fun getMVP(x: Float, y: Float, z: Float): FloatArray {

        // Move model to given position
        val modelMatrix = FloatArray(16)
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, x, y, z)

        // Scale to some reasonable size and flip upright
        Matrix.scaleM(modelMatrix, 0, scale, -scale, scale)

        // Calculate final MVP matrix
        val mvMatrix = FloatArray(16)
        Matrix.multiplyMM(mvMatrix, 0, viewMatrix, 0, modelMatrix, 0)

        val mvpMatrix = FloatArray(16)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvMatrix, 0)

        return mvpMatrix
    }

    private fun updateBug() {

        // Bugjaeger caught the bug - reposition bug to different location
        if (distance(bugBehaviour.position, bugjaegerBehaviour.position) < reachDistance)
            bugBehaviour.position = getRandomPosition()

        // Bug reached destination - find a new destination
        if (distance(bugTarget, bugBehaviour.position) < reachDistance)
            bugTarget = getRandomPosition()

        bugBehaviour.seek(bugTarget, time.deltaTimeSec)

        val pos = bugBehaviour.position
        val mvp = getMVP(pos.x, pos.y, pos.z)

        bugRenderer.draw(mvp)
    }
}