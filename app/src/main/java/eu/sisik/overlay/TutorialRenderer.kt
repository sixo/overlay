package eu.sisik.overlay

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.Log
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import android.opengl.GLES20.glGenerateMipmap
import android.opengl.GLUtils
import android.opengl.Matrix
import java.nio.*

/**
 * Copyright (c) 2018 by Roman Sisik. All rights reserved.
 */


/**
 * This code is only used as example code in the tutorial. Even though
 * the methods here are big and clunky, I find it easier to follow OpenGL
 * initialization and drawing when it is presented in steps in one place.
 *
 * The main purpose of this code is to initialize OpenGL and draw a static
 * texture  somewhere in the middle of the viewport.
 */
class TutorialRenderer(val context: Context): GLSurfaceView.Renderer {

    private val mvpMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)

    private val vertexShaderCode =
        "precision highp float;\n" +
                "attribute vec3 vertexPosition;\n" +
                "attribute vec2 uvs;\n" +
                "varying vec2 varUvs;\n" +
                "uniform mat4 mvp;\n" +
                "\n" +
                "void main()\n" +
                "{\n" +
                "\tvarUvs = uvs;\n" +
                "\tgl_Position = mvp * vec4(vertexPosition, 1.0);\n" +
                "}"

    private val fragmentShaderCode =
        "precision mediump float;\n" +
                "\n" +
                "varying vec2 varUvs;\n" +
                "uniform sampler2D texSampler;\n" +
                "\n" +
                "void main()\n" +
                "{\t\n" +
                "\tgl_FragColor = texture2D(texSampler, varUvs);\n" +
                "}"


    private var vertices = floatArrayOf(
        // x, y, z, u, v
        -1.0f, -1.0f, 0.0f, 0f, 0f,
        -1.0f, 1.0f, 0.0f, 0f, 1f,
        1.0f, 1.0f, 0.0f, 1f, 1f,
        1.0f, -1.0f, 0.0f, 1f, 0f
    )

    private var indices = intArrayOf(
        2, 1, 0, 0, 3, 2
    )

    private var program: Int = 0
    private var vertexHandle: Int = 0
    private var bufferHandles = IntArray(2)
    private var uvsHandle: Int = 0
    private var mvpHandle: Int = 0
    private var samplerHandle: Int = 0
    private val textureHandle = IntArray(1)

    var vertexBuffer: FloatBuffer = ByteBuffer.allocateDirect(vertices.size * 4).run {
        order(ByteOrder.nativeOrder())
        asFloatBuffer().apply {
            put(vertices)
            position(0)
        }
    }

    var indexBuffer: IntBuffer = ByteBuffer.allocateDirect(indices.size * 4).run {
        order(ByteOrder.nativeOrder())
        asIntBuffer().apply {
            put(indices)
            position(0)
        }
    }

    override fun onDrawFrame(p0: GL10?) {

        // We want to clear buffers to transparent, so we also see stuff rendered by
        // other apps and the system
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        GLES20.glClearColor(0f, 0f, 0f, 0f)


        // Prepare transformations for our texture quad

        // Position model
        // ..

        // Position camera
        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, -80f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)

        // Combine all our transformations
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, viewMatrix, 0)

        GLES20.glUseProgram(program)

        // Pass transformations to shader
        GLES20.glUniformMatrix4fv(mvpHandle, 1, false, mvpMatrix, 0)


        // Prepare texture for drawing
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0])
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glUniform1i(samplerHandle, 0)


        // Prepare buffers with vertices and indices
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, bufferHandles[0])
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, bufferHandles[1])

        GLES20.glEnableVertexAttribArray(vertexHandle)
        GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT, false, 4 * 5, 0)

        GLES20.glEnableVertexAttribArray(uvsHandle)
        GLES20.glVertexAttribPointer(uvsHandle, 2, GLES20.GL_FLOAT, false, 4 * 5, 3 * 4)


        // Ready to draw
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_INT, 0)
    }

    override fun onSurfaceChanged(p0: GL10?, width: Int, height: Int) {

        GLES20.glViewport(0, 0, width, height)

        // Set perspective projection
        val apect: Float = width.toFloat() / height.toFloat()
        Matrix.perspectiveM(projectionMatrix, 0, -apect, apect, 1f, 100f)
    }

    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {

        // Create shader program
        val vertexShader: Int = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER).also { shader ->
            GLES20.glShaderSource(shader, vertexShaderCode)
            GLES20.glCompileShader(shader)
        }

        val fragmentShader: Int = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER).also { shader ->
            GLES20.glShaderSource(shader, fragmentShaderCode)
            GLES20.glCompileShader(shader)
        }

        program = GLES20.glCreateProgram().also {

            GLES20.glAttachShader(it, vertexShader)
            GLES20.glAttachShader(it, fragmentShader)
            GLES20.glLinkProgram(it)

            // Get handles
            vertexHandle = GLES20.glGetAttribLocation(it, "vertexPosition")
            uvsHandle = GLES20.glGetAttribLocation(it, "uvs")
            mvpHandle = GLES20.glGetUniformLocation(it, "mvp")
            samplerHandle = GLES20.glGetUniformLocation(it, "texSampler")
        }


        // Initialize buffers
        GLES20.glGenBuffers(2, bufferHandles, 0)

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, bufferHandles[0])
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertices.size * 4, vertexBuffer, GLES20.GL_DYNAMIC_DRAW)

        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, bufferHandles[1])
        GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, indices.size * 4, indexBuffer, GLES20.GL_DYNAMIC_DRAW)


        // Load the texture
        val bitmap = BitmapFactory.decodeStream(context.assets.open("bugjaeger_icon.png"))

        GLES20.glGenTextures(1, textureHandle, 0)
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0])
        GLES20.glPixelStorei(GLES20.GL_UNPACK_ALIGNMENT, 1)
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST)
        glGenerateMipmap(GLES20.GL_TEXTURE_2D)


        // Ensure I can draw transparent stuff that overlaps properly
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
    }
}
