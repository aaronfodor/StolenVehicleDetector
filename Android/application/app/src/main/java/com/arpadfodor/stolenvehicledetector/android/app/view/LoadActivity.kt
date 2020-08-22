package com.arpadfodor.stolenvehicledetector.android.app.view

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.view.animation.AnimationUtils
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.arpadfodor.stolenvehicledetector.android.app.ApplicationRoot
import com.arpadfodor.stolenvehicledetector.android.app.R
import com.arpadfodor.stolenvehicledetector.android.app.view.utils.AppActivity
import com.arpadfodor.stolenvehicledetector.android.app.viewmodel.LoadViewModel
import com.arpadfodor.stolenvehicledetector.android.app.viewmodel.utils.Recognition
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.load_ui_container.*

class LoadActivity : AppActivity() {

    private lateinit var viewModel: LoadViewModel
    private lateinit var container: ConstraintLayout

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_load)
        container = findViewById(R.id.loaded_image_container)
        val drawer = findViewById<DrawerLayout>(R.id.loadActivityDrawerLayout)
        val navigation = findViewById<NavigationView>(R.id.load_navigation)
        initUi(drawer, navigation)

        viewModel = ViewModelProvider(this).get(LoadViewModel::class.java)

        //due to an Android bug, setting clip to outline cannot be done from XML
        ivLoadedImage.clipToOutline = true
        Glide
            .with(this)
            .load(R.drawable.load_image_placeholder)
            .into(ivLoadedImage)

    }

    override fun onResume() {

        super.onResume()

        // Get screen metrics used to setup optimal bounding box image size
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)

        viewModel.setScreenProperties(metrics.widthPixels, metrics.heightPixels)

        /**
         * Before hiding the status bar, a wait is needed to let the UI settle.
         * Trying to set app to immersive mode before it's ready causes the flags not sticking.
         */
        container.postDelayed({
            container.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN and View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
        }, ApplicationRoot.IMMERSIVE_FLAG_TIMEOUT)

        // read settings from preferences
        val settings = PreferenceManager.getDefaultSharedPreferences(applicationContext)

        val settingsNumRecognitionsKey = getString(R.string.SETTINGS_NUM_RECOGNITIONS)
        val settingsMinimumPredictionCertaintyKey = getString(R.string.SETTINGS_MINIMUM_PREDICTION_CERTAINTY)

        val numRecognitionsToShow = settings.getInt(settingsNumRecognitionsKey, resources.getInteger(R.integer.settings_num_recognitions_default))
        val minimumPredictionCertaintyToShow = settings.getInt(settingsMinimumPredictionCertaintyKey, resources.getInteger(R.integer.settings_minimum_prediction_certainty_default))

        LoadViewModel.numRecognitionsToShow = numRecognitionsToShow
        LoadViewModel.minimumPredictionCertaintyToShow = minimumPredictionCertaintyToShow.toFloat()

    }

    override fun subscribeToViewModel() {

        // Create the image observer which updates the UI in case of an image change
        val imageObserver = Observer<Bitmap> { newImage ->
            // Update the UI, in this case, the ImageView
            ivLoadedImage.setImageBitmap(newImage)

            Glide
                .with(this)
                .load(newImage)
                .centerCrop()
                .error(R.drawable.load_image_placeholder)
                .placeholder(R.drawable.load_image_placeholder)
                .into(ivLoadedImage)
        }

        // Create the image observer which updates the UI in case of bounding box image change
        val boundingBoxImageObserver = Observer<Bitmap> { newImage ->
            // Update the UI, in this case, the ImageView
            ivLoadedImageBoundingBoxes.setImageBitmap(newImage)

            Glide
                .with(this)
                .load(newImage)
                .centerCrop()
                .into(ivLoadedImageBoundingBoxes)
        }

        // Create the suspicious Id observer which notifies when suspicious element has been recognized
        val recognitionsObserver = Observer<Array<Recognition>> { recognitions ->

            val alertButton = alert_loaded_button
            val circularAlertButton = circular_alert_load_button

            if(recognitions.isNotEmpty()){

                alertButton.setOnClickListener {
                    viewModel.setAlertActivityParams()
                    val intent = Intent(this, AlertActivity::class.java)
                    startActivity(intent)
                }
                circularAlertButton.setOnClickListener {
                    viewModel.setAlertActivityParams()
                    val intent = Intent(this, AlertActivity::class.java)
                    startActivity(intent)
                }

                if(alertButton.visibility == View.GONE){
                    val animation = AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
                    alertButton.visibility = View.VISIBLE
                    alertButton.animation = animation
                    alertButton.animation.start()
                }
                if(circularAlertButton.visibility == View.GONE){
                    val animation = AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
                    circularAlertButton.visibility = View.VISIBLE
                    circularAlertButton.animation = animation
                    circularAlertButton.animation.start()
                }

            }
            else{

                if(alertButton.visibility == View.VISIBLE){
                    val animation = AnimationUtils.loadAnimation(this, android.R.anim.fade_out)
                    alertButton.animation = animation
                    alertButton.animation.start()
                    alertButton.visibility = View.GONE
                }
                if(circularAlertButton.visibility == View.VISIBLE){
                    val animation = AnimationUtils.loadAnimation(this, android.R.anim.fade_out)
                    circularAlertButton.animation = animation
                    circularAlertButton.animation.start()
                    circularAlertButton.visibility = View.GONE
                }

            }

        }

        // Observe the LiveData, passing in this viewLifeCycleOwner as the LifecycleOwner and the observer
        viewModel.loadedImage.observe(this, imageObserver)
        viewModel.boundingBoxImage.observe(this, boundingBoxImageObserver)
        viewModel.recognitions.observe(this, recognitionsObserver)

    }

    override fun subscribeListeners() {

        ivLoadedImage.setOnClickListener {
            loadImage()
        }

        load_image_button.setOnClickListener {
            loadImage()
        }

        loaded_image_rotate_button.setOnClickListener {
            viewModel.rotateImage()
        }

    }

    override fun unsubscribe() {}

    private fun loadImage(){
        // Create an Intent with action as ACTION_PICK
        val intent = Intent(Intent.ACTION_PICK)
        // Sets the type as image/*. This ensures only components of type image are selected
        intent.type = "image/*"

        intent.putExtra(Intent.EXTRA_MIME_TYPES, viewModel.imageMimeTypes)
        // Launch the Intent
        startActivityForResult(intent, LoadViewModel.GALLERY_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result code is RESULT_OK only if the user has selected an Image
        if (resultCode == RESULT_OK) {
            when (requestCode) {

                LoadViewModel.GALLERY_REQUEST_CODE -> {
                    //data.getData returns the content URI for the selected Image
                    val selectedImageUri = data?.data ?: return
                    viewModel.loadImage(selectedImageUri)
                }
            }
        }
    }

}
