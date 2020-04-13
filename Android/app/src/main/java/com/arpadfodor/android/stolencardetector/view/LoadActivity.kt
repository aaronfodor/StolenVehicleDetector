package com.arpadfodor.android.stolencardetector.view

import android.app.ActivityOptions
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.preference.PreferenceManager
import com.arpadfodor.android.stolencardetector.ApplicationRoot
import com.arpadfodor.android.stolencardetector.R
import com.arpadfodor.android.stolencardetector.view.utils.AppDialog
import com.arpadfodor.android.stolencardetector.viewmodel.LoadViewModel
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_load.*
import kotlinx.android.synthetic.main.load_ui_container.*

class LoadActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var viewModel: LoadViewModel

    private lateinit var container: ConstraintLayout

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_load)
        container = findViewById(R.id.loaded_image_container)

        viewModel = ViewModelProviders.of(this).get(LoadViewModel::class.java)

        val toolbar = findViewById<Toolbar>(R.id.camera_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val drawerToggle = ActionBarDrawerToggle(this, loadActivityDrawerLayout, toolbar, R.string.menu_open, R.string.menu_close)
        loadActivityDrawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        load_navigation.setNavigationItemSelectedListener(this)
        val navigationMenuView = findViewById<NavigationView>(R.id.load_navigation)
        val header = navigationMenuView?.getHeaderView(0)

        load_navigation.bringToFront()
        load_navigation.parent.requestLayout()

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
        val settingsMaximumRecognitions = getString(R.string.SETTINGS_MAXIMUM_RECOGNITIONS)
        val settingsMinimumPredictionCertainty = getString(R.string.SETTINGS_MINIMUM_PREDICTION_CERTAINTY)
        val maximumRecognitionsToShow = settings.getInt(settingsMaximumRecognitions, resources.getInteger(R.integer.settings_maximum_recognitions_default))
        val minimumPredictionCertaintyToShow = settings.getInt(settingsMinimumPredictionCertainty, resources.getInteger(R.integer.settings_minimum_prediction_certainty_default))
        LoadViewModel.maximumRecognitionsToShow = maximumRecognitionsToShow
        LoadViewModel.minimumPredictionCertaintyToShow = minimumPredictionCertaintyToShow.toFloat()

        subscribeToViewModel()
        setButtonListeners()

    }

    private fun subscribeToViewModel() {

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

        // Observe the LiveData, passing in this viewLifeCycleOwner as the LifecycleOwner and the observer
        viewModel.loadedImage.observe(this, imageObserver)
        viewModel.boundingBoxImage.observe(this, boundingBoxImageObserver)

    }

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
                    val selectedImageUri = data?.data?:return
                    val imageOrientation = getPhotoOrientation(selectedImageUri)
                    val sourceBitmap = MediaStore.Images.Media.getBitmap(application.contentResolver, selectedImageUri)
                    viewModel.setLoadedImage(sourceBitmap, imageOrientation)

                }
            }

        }

    }

    /**
     * Returns the orientation of the inspected image from MediaStore
     *
     * @param photoUri      URI of the image to get the orientation information for
     * @return Int          Orientation of the image
     **/
    private fun getPhotoOrientation(photoUri: Uri): Int {

        val cursor = applicationContext.contentResolver.query(photoUri,
            arrayOf(MediaStore.Images.ImageColumns.ORIENTATION), null, null, null
        )

        cursor?: return 0

        if (cursor.count != 1) {
            cursor.close()
            return 0
        }

        cursor.moveToFirst()
        val orientation = cursor.getInt(0)
        cursor.close()

        return orientation

    }

    private fun setButtonListeners() {

        ivLoadedImage.setOnClickListener {
            loadImage()
        }

        load_image_button.setOnClickListener {
            loadImage()
        }

        loaded_image_details_button.setOnClickListener {
        }
    }

    /**
     * Called when an item in the navigation menu is selected.
     *
     * @param item The selected item
     * @return true to display the item as the selected item
     **/
    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.navigation_live -> {
                val intent = Intent(this, CameraActivity::class.java)
                startActivity(intent)
            }
            R.id.navigation_load -> {
            }
            R.id.navigation_gallery -> {
            }
            R.id.navigation_reports -> {
            }
            R.id.navigation_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
            }
            else ->{
                return false
            }
        }

        if(loadActivityDrawerLayout.isDrawerOpen(GravityCompat.START)){
            loadActivityDrawerLayout.closeDrawer(GravityCompat.START)
        }
        return true

    }

    override fun onBackPressed() {

        if(loadActivityDrawerLayout.isDrawerOpen(GravityCompat.START)){
            loadActivityDrawerLayout.closeDrawer(GravityCompat.START)
        }
        else{
            exitDialog()
        }

    }

    /**
     * Asks for exit confirmation
     **/
    private fun exitDialog(){

        val exitDialog = AppDialog(this, getString(R.string.exit_title), getString(R.string.exit_dialog), resources.getDrawable(R.drawable.warning))
        exitDialog.setPositiveButton {
            //showing the home screen - app is not visible but running
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_HOME)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
        }
        exitDialog.show()

    }

}
