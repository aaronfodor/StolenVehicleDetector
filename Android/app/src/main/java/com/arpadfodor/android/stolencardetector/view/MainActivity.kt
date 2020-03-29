package com.arpadfodor.android.stolencardetector.view

import android.content.Intent
import android.hardware.SensorManager
import android.os.Bundle
import android.view.KeyEvent
import android.view.OrientationEventListener
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.arpadfodor.android.stolencardetector.R
import com.arpadfodor.android.stolencardetector.view.utils.FLAGS_FULLSCREEN
import com.arpadfodor.android.stolencardetector.viewmodel.MainViewModel

const val KEY_EVENT_ACTION = "key_event_action"
const val KEY_EVENT_EXTRA = "key_event_extra"
private const val IMMERSIVE_FLAG_TIMEOUT = 500L

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel

    private lateinit var container: FrameLayout

    lateinit var deviceOrientationListener: OrientationEventListener

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        container = findViewById(R.id.fragment_container)

        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)

        deviceOrientationListener = object : OrientationEventListener(this,
            SensorManager.SENSOR_DELAY_NORMAL) {
            override fun onOrientationChanged(orientation: Int) {

                var currentOrientation = 0

                if(315 < orientation || orientation <= 45){
                    currentOrientation = 0
                }
                else if(orientation in 46..135){
                    currentOrientation = 90
                }
                else if(orientation in 136..225){
                    currentOrientation = 180
                }
                else if(orientation in 226..315){
                    currentOrientation = 270
                }

                MainViewModel.deviceOrientation = currentOrientation
            }
        }

        supportActionBar?.hide()

    }

    override fun onResume() {

        super.onResume()
        /**
         * Before setting full screen flags, a wait is needed to let the UI settle.
         * Trying to set app to immersive mode before it's ready causes the flags not sticking.
         */
        container.postDelayed({
            container.systemUiVisibility =
                FLAGS_FULLSCREEN
        }, IMMERSIVE_FLAG_TIMEOUT)

        deviceOrientationListener.enable()

    }

    override fun onPause() {
        deviceOrientationListener.disable()
        super.onPause()
    }

    override fun onDestroy() {
        viewModel.closeResources()
        super.onDestroy()
    }

    /**
     * When key down event is triggered, relay it via local broadcast so fragments can handle it
     */
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {

        return when (keyCode){

            KeyEvent.KEYCODE_VOLUME_DOWN -> {

                val intent = Intent(KEY_EVENT_ACTION).apply {
                    putExtra(KEY_EVENT_EXTRA, keyCode)
                }
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
                true

            }
            else -> {
                super.onKeyDown(keyCode, event)
            }

        }

    }

}
