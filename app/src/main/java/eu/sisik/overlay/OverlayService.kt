package eu.sisik.overlay

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.support.v4.content.LocalBroadcastManager
import android.graphics.PixelFormat
import android.os.Build
import android.view.*


/**
 * Copyright (c) 2018 by Roman Sisik. All rights reserved.
 */

class OverlayService: Service() {
    lateinit var rootView: View
    lateinit var overlaySurfaceView: OverlaySurfaceView
    lateinit var wm: WindowManager

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        startForeground()

        return Service.START_STICKY
    }

    override fun onCreate() {
        super.onCreate()

        initOverlay()
        overlaySurfaceView.onResume()
        sendBroadcast(ACTION_OVERLAY_SERVICE_STARTED)
    }

    override fun onDestroy() {
        super.onDestroy()

        overlaySurfaceView.onPause()
        wm.removeView(rootView)
        sendBroadcast(ACTION_OVERLAY_SERVICE_STOPPED)
    }

    fun startForeground() {

        val pendingIntent: PendingIntent =
            Intent(this, OverlayService::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, 0)
            }

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getText(R.string.app_name))
            .setContentText(getText(R.string.app_name))
            .setSmallIcon(R.drawable.notification_template_icon_bg)
            .setContentIntent(pendingIntent)
            .setTicker(getText(R.string.app_name))
            .build()

        startForeground(ONGOING_NOTIFICATION_ID, notification)
    }

    fun initOverlay() {

        val li = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        rootView = li.inflate(R.layout.overlay, null)
        overlaySurfaceView = rootView.findViewById(R.id.overlaySurfaceView)

        val type = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
                            WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY
                        else
                            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY

        val params = WindowManager.LayoutParams(
            type,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            PixelFormat.TRANSLUCENT
        )

        wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        wm.addView(rootView, params)
    }

    fun sendBroadcast(action: String) {

        val intent = Intent(action)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    companion object {

        val ONGOING_NOTIFICATION_ID = 6661
        val CHANNEL_ID = "overlay_channel_id"

        val ACTION_OVERLAY_SERVICE_STARTED = "eu.sisik.action.OVERLAY_SERVICE_STARTED"
        val ACTION_OVERLAY_SERVICE_STOPPED = "eu.sisik.action.OVERLAY_SERVICE_STOPPED"
    }
}