package eu.sisik.overlay

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


/**
 * Copyright (c) 2018 by Roman Sisik. All rights reserved.
 */
class OverlayRenderer(val context: Context): GLSurfaceView.Renderer {

    lateinit var sceneManager: SceneManager


    override fun onDrawFrame(p0: GL10?) {

        // We want to clear buffers to transparent, so we also see stuff rendered by
        // other apps and the system
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        GLES20.glClearColor(0f, 0f, 0f, 0f)

        sceneManager.update()
    }

    override fun onSurfaceChanged(p0: GL10?, width: Int, height: Int) {

        GLES20.glViewport(0, 0, width, height)

        sceneManager.setCamera(width, height)
    }

    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {

        sceneManager = SceneManager(context)
    }
}