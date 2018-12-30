package eu.sisik.overlay

import android.content.Context
import android.graphics.PixelFormat
import android.opengl.GLSurfaceView
import android.util.AttributeSet

/**
 * Copyright (c) 2018 by Roman Sisik. All rights reserved.
 */
class OverlaySurfaceView : GLSurfaceView {

    var overlayRenderer: OverlayRenderer

    constructor(context: Context?, attrs: AttributeSet) : super(context, attrs) {

        // Configure OpenGL ES 2.0 and enable rendering with transparent background
        setEGLContextClientVersion(2)
        setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        getHolder().setFormat(PixelFormat.RGBA_8888)
        setZOrderOnTop(false)

        overlayRenderer = OverlayRenderer(context!!)
        setRenderer(overlayRenderer)
    }
}