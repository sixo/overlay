package eu.sisik.overlay

import android.app.ActivityManager
import android.app.AlertDialog
import android.content.*
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import android.view.MotionEvent
import android.widget.Toast
import eu.sisik.overlay.OverlayService.Companion.ACTION_OVERLAY_SERVICE_STARTED
import eu.sisik.overlay.OverlayService.Companion.ACTION_OVERLAY_SERVICE_STOPPED
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    var isShowingExplanation = false

    val overlayReceiver = object: BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            when (p1?.action) {
                ACTION_OVERLAY_SERVICE_STARTED -> {
                    butStartStop.text = getString(R.string.but_stop)
                }

                ACTION_OVERLAY_SERVICE_STOPPED -> {
                    butStartStop.text = getString(R.string.but_start)
                }
            }
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initView(savedInstanceState)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        processActivityResult(requestCode)
    }

    override fun onResume() {
        super.onResume()
        if (isServiceRunning(OverlayService::class.java))
            butStartStop.text = getString(R.string.but_stop)
        else
            butStartStop.text = getString(R.string.but_start)

        registerOverlayReceiver()
    }

    override fun onPause() {
        super.onPause()

        LocalBroadcastManager.getInstance(this).unregisterReceiver(overlayReceiver)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putBoolean("isShowingExplanation", isShowingExplanation)
    }



    fun initView(savedInstanceState: Bundle?) {

        butStartStop.setOnClickListener {
            if (butStartStop.text == getString(R.string.but_start)) {
                // Start the service, but only when we have necessary permission
                if (hasOverlayPermission()) {
                    startOverlayService()
                } else {
                    showExplanation()
                }

            } else {
                stopOverlayService()
            }
        }

        // Restore permission explanation dialog if necessary
        isShowingExplanation = savedInstanceState?.getBoolean("isShowingExplanation", false) ?: false
        if (isShowingExplanation)
            showExplanation()

        // TODO: remove
        layout.setOnTouchListener { view, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                Log.d("overlayService", "touch coords= " + motionEvent.x + ", " + motionEvent.y)
            }

            false
        }
    }

    fun hasOverlayPermission(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(this)
    }

    fun startOverlayService() {
        val intent = Intent(this, OverlayService::class.java)
        startService(intent)
    }

    fun requestOverlayPermission() {
        val settingsIntent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
        startActivityForResult(settingsIntent, PERMISSION_REQUEST_CODE)
    }

    fun showExplanation() {
        val alertDialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.permission_explanation))
            .setPositiveButton(R.string.but_change_permissions,
                    DialogInterface.OnClickListener { dialog, id ->
                        requestOverlayPermission()
                    })
            .setNegativeButton(R.string.but_cancel,
                    DialogInterface.OnClickListener { dialog, id ->
                        isShowingExplanation = false
                    })
            .setOnCancelListener { isShowingExplanation = false }
            .create()

        isShowingExplanation = true
        alertDialog.show()
    }

    fun stopOverlayService() {
        val intent = Intent(this, OverlayService::class.java)
        stopService(intent)
    }

    fun processActivityResult(requestCode: Int) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (hasOverlayPermission()) {
                startOverlayService()
            } else {
                Toast.makeText(applicationContext, getString(R.string.msg_permission_not_granted),
                    Toast.LENGTH_LONG).show()
            }
        }
    }

    fun isServiceRunning(clazz: Class<*>): Boolean {
        val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        @Suppress("DEPRECATION")
        for (i in am.getRunningServices(Integer.MAX_VALUE)) {
            if (i.service.className == clazz.name)
                return true
        }

        return false
    }

    fun registerOverlayReceiver() {
        val filter = IntentFilter(ACTION_OVERLAY_SERVICE_STARTED)
        filter.addAction(ACTION_OVERLAY_SERVICE_STOPPED)
        LocalBroadcastManager.getInstance(this).registerReceiver(overlayReceiver, filter)
    }


    companion object {
        val PERMISSION_REQUEST_CODE = 6660
    }
}
