package com.arpadfodor.stolenvehicledetector.android.app.view

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.hardware.display.DisplayManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import android.webkit.MimeTypeMap
import android.widget.ImageButton
import android.widget.ImageView
import androidx.camera.core.*
import androidx.camera.core.Camera
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.core.net.toFile
import androidx.core.view.setPadding
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.arpadfodor.stolenvehicledetector.android.app.R
import com.arpadfodor.stolenvehicledetector.android.app.viewmodel.CameraViewModel
import com.arpadfodor.stolenvehicledetector.android.app.viewmodel.analyzer.ImageAnalyzer
import com.arpadfodor.stolenvehicledetector.android.app.view.utils.simulateClick
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import androidx.lifecycle.Observer
import com.arpadfodor.stolenvehicledetector.android.app.model.LocationService
import com.arpadfodor.stolenvehicledetector.android.app.viewmodel.utils.Report

/** Helper type alias used for analysis use case callbacks */
typealias DetectionListener = (recognition: Bitmap) -> Unit

/**
 * Main fragment of the app. Implements camera operations including:
 * -Viewfinder
 * -Photo taking
 * -Image analysis
 */
class CameraFragment() : Fragment() {

    companion object{
        private const val TAG = "Camera fragment"
    }

    private val viewModel: CameraViewModel by activityViewModels()

    private lateinit var container: ConstraintLayout
    private lateinit var viewFinder: PreviewView
    private lateinit var boundingBoxesImageView: ImageView
    private lateinit var broadcastManager: LocalBroadcastManager

    private var displayId: Int = -1
    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null

    private val displayManager by lazy {
        requireContext().getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
    }

    /**
     * Blocking camera operations using this executor
     */
    private lateinit var cameraExecutor: ExecutorService

    /**
     * Volume down button receiver used to trigger shutter
     */
    private val volumeDownReceiver = object : BroadcastReceiver(){

        override fun onReceive(context: Context, intent: Intent) {

            when(intent.getIntExtra(CameraViewModel.KEY_EVENT_EXTRA, KeyEvent.KEYCODE_UNKNOWN)){
                // When the volume down button is pressed, simulate a shutter button click
                KeyEvent.KEYCODE_VOLUME_DOWN -> {
                    val shutter = container.findViewById<ImageButton>(R.id.camera_capture_button)
                    shutter.simulateClick()
                }
            }

        }

    }

    private val displayListener = object : DisplayManager.DisplayListener{

        override fun onDisplayAdded(displayId: Int) = Unit
        override fun onDisplayRemoved(displayId: Int) = Unit

        override fun onDisplayChanged(displayId: Int) = view?.let{ view ->
            if(displayId == this@CameraFragment.displayId){
                Log.d(TAG, "Rotation changed: ${view.display.rotation}")
                imageCapture?.targetRotation = view.display.rotation
                imageAnalyzer?.targetRotation = view.display.rotation
            }
        } ?: Unit

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_camera, container, false)
    }

    private fun setGalleryThumbnail(uri: Uri){

        // gallery thumbnail holder view's reference
        val thumbnail = container.findViewById<ImageButton>(R.id.photo_view_button)

        thumbnail.post{

            // Remove thumbnail padding
            thumbnail.setPadding(resources.getDimension(R.dimen.stroke_small).toInt())

            // load thumbnail into a circular button using Glide
            Glide.with(thumbnail)
                .load(uri)
                .apply(RequestOptions.circleCropTransform())
                .into(thumbnail)

        }

    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?){

        super.onViewCreated(view, savedInstanceState)

        container = view as ConstraintLayout
        viewFinder = container.findViewById(R.id.view_finder)
        boundingBoxesImageView = container.findViewById(R.id.ivBoundingBoxes)

        // Initialize the background executor
        cameraExecutor = Executors.newSingleThreadExecutor()

        broadcastManager = LocalBroadcastManager.getInstance(view.context)

        // setting up the intent filter that receives main activity events
        val filter = IntentFilter().apply { addAction(CameraViewModel.KEY_EVENT_ACTION) }
        broadcastManager.registerReceiver(volumeDownReceiver, filter)

        // when device screen orientation changes, update rotation for use cases
        displayManager.registerDisplayListener(displayListener, null)

        // waiting for the views to be properly laid out
        viewFinder.post {

            // display in which the view is attached
            displayId = viewFinder.display.displayId

            // build UI controls
            updateCameraUi()

            // bind use cases
            bindCameraUseCases()

        }

    }

    override fun onDestroyView(){

        super.onDestroyView()

        // shuts down the background executor
        cameraExecutor.shutdown()

        // unregisters the broadcast receivers and listeners
        broadcastManager.unregisterReceiver(volumeDownReceiver)
        displayManager.unregisterDisplayListener(displayListener)

    }

    /**
     * Inflate camera controls and update the UI manually upon config changes to avoid removing
     * and re-adding the view finder from the view hierarchy; this provides a seamless rotation
     * transition on devices that support it.
     *
     * NOTE: The flag is supported starting in Android 8 but there still is a small flash on the
     * screen for devices that run Android 9 or below.
     */
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateCameraUi()
    }

    /**
     * Declares and binds preview, runs capture and analyse use cases
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun bindCameraUseCases(){

        // Get screen metrics used to setup camera for full screen resolution
        val metrics = DisplayMetrics().also{viewFinder.display.getRealMetrics(it)}
        Log.d(TAG, "Screen metrics: ${metrics.widthPixels} x ${metrics.heightPixels}")

        val screenAspectRatio = viewModel.aspectRatio(metrics.widthPixels, metrics.heightPixels)
        Log.d(TAG, "Preview aspect ratio: $screenAspectRatio")

        // Get screen metrics used to setup optimal bounding box image size
        val screenMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(screenMetrics)
        viewModel.setScreenProperties(metrics.widthPixels, metrics.heightPixels)

        if(CameraViewModel.settingsShowReceptiveField){
            boundingBoxesImageView.background = resources.getDrawable(R.drawable.receptive_field_marker)
        }
        else{
            boundingBoxesImageView.background = null
        }

        // bind CameraProvider to the LifeCycleOwner
        val cameraSelector = CameraSelector.Builder().requireLensFacing(viewModel.lensFacing).build()
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        val cameraOrientation = viewFinder.display.rotation

        cameraProviderFuture.addListener(Runnable {

            // CameraProvider
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // preview
            preview = Preview.Builder()
                // request aspect ratio but not resolution
                .setTargetAspectRatio(screenAspectRatio)
                // set initial target rotation
                .setTargetRotation(cameraOrientation)
                .build()

            // attach the viewfinder's surface provider to preview use case
            preview?.setSurfaceProvider(viewFinder.createSurfaceProvider())

            // ImageCapture
            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                // Request aspect ratio but no resolution to match preview config, but letting
                // CameraX optimize for whatever specific resolution best fits the use cases
                .setTargetAspectRatio(screenAspectRatio)
                // Set initial target rotation - needs to be called again if rotation changes during
                // the lifecycle of this use case
                .setTargetRotation(cameraOrientation)
                .build()

            // ImageAnalysis
            imageAnalyzer = ImageAnalysis.Builder()
                // Request aspect ratio but no resolution
                .setTargetAspectRatio(screenAspectRatio)
                // Non-blocking behavior: keep only the latest frame
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                // Set initial target rotation - needs to be called again if rotation changes during the lifecycle of this use case
                .setTargetRotation(cameraOrientation)
                .build()
                // The analyzer can then be assigned to the instance
                .also {

                    it.setAnalyzer(cameraExecutor, ImageAnalyzer( { boundingBoxImage ->

                        if(viewFinder.isLaidOut){

                            activity?.runOnUiThread{
                                boundingBoxesImageView.setImageBitmap(boundingBoxImage)
                            }

                        }

                    }, viewModel))

                }

            // Must unbind the use-cases before rebinding them
            cameraProvider.unbindAll()

            try {
                // A variable number of use-cases can be passed here - camera provides access to CameraControl & CameraInfo
                camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture, imageAnalyzer)
            }
            catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))

        /**
         * Add listeners to detect zooming and taping to focus
         **/
        val zoomListener = object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val currentZoomRatio: Float = camera?.cameraInfo?.zoomState?.value?.zoomRatio ?: 0F
                val delta = detector.scaleFactor
                camera?.cameraControl?.setZoomRatio(currentZoomRatio * delta)
                return true
            }
        }
        val zoomDetector = ScaleGestureDetector(context, zoomListener)

        viewFinder.setOnTouchListener{ _, event ->

            zoomDetector.onTouchEvent(event)

            if (event.action == MotionEvent.ACTION_UP) {
                val factory = viewFinder.createMeteringPointFactory(cameraSelector)
                val point = factory.createPoint(event.x, event.y)
                val actionBuilder = FocusMeteringAction.Builder(point)
                val action = actionBuilder.build()
                camera?.cameraControl?.startFocusAndMetering(action)
            }

            return@setOnTouchListener true

        }

    }

    /**
     * Used to re-draw the camera UI controls, called every time configuration changes.
     */
    private fun updateCameraUi(){

        // Remove previous UI if any
        container.findViewById<ConstraintLayout>(R.id.camera_ui_container)?.let {
            container.removeView(it)
        }

        // Inflate a new view containing all UI for controlling the camera
        val controls = View.inflate(requireContext(), R.layout.camera_ui_container, container)

        // In the background, load latest photo taken (if any) for gallery thumbnail
        lifecycleScope.launch(Dispatchers.IO) {
            //TODO: gallery thumbnail update
        }

        setButtonListeners(controls)
        subscribeToViewModel(controls)

    }

    private fun setButtonListeners(controls: View) {

        // Listener for button used to capture photo
        controls.findViewById<ImageButton>(R.id.camera_capture_button).setOnClickListener {

            // Get a stable reference of the modifiable image capture use case
            imageCapture?.let { imageCapture ->

                // Create output file to hold the image
                val photoFile = viewModel.createFile()

                // Setup image capture metadata
                val metadata = ImageCapture.Metadata().apply {
                    // Mirror the image when using the front camera
                    isReversedHorizontal = (viewModel.lensFacing == CameraSelector.LENS_FACING_FRONT)
                }

                // Create output options object which contains file + metadata
                val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile)
                    .setMetadata(metadata)
                    .build()

                // Setup image capture listener which is triggered after photo has been taken
                imageCapture.takePicture(
                    outputOptions, cameraExecutor, object : ImageCapture.OnImageSavedCallback {

                        override fun onError(exc: ImageCaptureException) {
                            Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                        }

                        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                            val savedUri = output.savedUri ?: Uri.fromFile(photoFile)
                            Log.d(TAG, "Photo captured: $savedUri")

                            // Update the gallery thumbnail with latest picture taken
                            setGalleryThumbnail(savedUri)

                            // If the folder selected is an external media directory, this is
                            // unnecessary but otherwise other apps will not be able to access app
                            // images unless scanning them using [MediaScannerConnection]
                            val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(savedUri.toFile().extension)
                            MediaScannerConnection.scanFile(
                                context,
                                arrayOf(savedUri.toString()),
                                arrayOf(mimeType)
                            ) { _, uri ->
                                Log.d(TAG, "Image capture scanned into media store: $uri")
                            }
                        }

                    })

                // Display flash animation to indicate that photo was captured
                container.postDelayed({
                    container.foreground = ColorDrawable(Color.WHITE)
                    container.postDelayed(
                        { container.foreground = null }, resources.getInteger(R.integer.ANIMATION_FAST_MILLIS).toLong())
                }, resources.getInteger(R.integer.ANIMATION_SLOW_MILLIS).toLong())

            }

        }

        // Listener for button used to switch camera
        controls.findViewById<ImageButton>(R.id.camera_switch_button).setOnClickListener {
            viewModel.lensFacing = if (CameraSelector.LENS_FACING_FRONT == viewModel.lensFacing) {
                CameraSelector.LENS_FACING_BACK
            }
            else {
                CameraSelector.LENS_FACING_FRONT
            }
            // Re-bind use cases to update selected camera
            bindCameraUseCases()
        }

        // Listener for button used to view the most recent photo
        controls.findViewById<ImageButton>(R.id.photo_view_button).setOnClickListener {
            // Only navigate when the gallery has photos
            if(true == viewModel.getOutputDirectory().listFiles()?.isNotEmpty()) { }
        }

    }

    private fun subscribeToViewModel(controls: View) {

        val alertButton = controls.findViewById<ConstraintLayout>(R.id.alert_live_button)

        // Create the recognitions observer which notifies when element has been recognized
        val recognitionsObserver = Observer<Array<Report>> { recognitions ->

            if(recognitions.isNotEmpty()){

                alertButton.setOnClickListener {

                    LocationService.updateLocation()
                    val location = LocationService.getLocation()

                    AlertActivity.setActivityParameter(recognitions)
                    val intent = Intent(this.requireActivity(), AlertActivity::class.java)
                    startActivity(intent)

                }

                if(alertButton.visibility == View.GONE){
                    val animation = AnimationUtils.loadAnimation(this.requireContext(), android.R.anim.fade_in)
                    alertButton.visibility = View.VISIBLE
                    alertButton.animation = animation
                    alertButton.animation.start()
                }

            }
            else{

                if(alertButton.visibility == View.VISIBLE){
                    val animation = AnimationUtils.loadAnimation(this.requireContext(), android.R.anim.fade_out)
                    alertButton.animation = animation
                    alertButton.animation.start()
                    alertButton.visibility = View.GONE
                }

            }

        }

        // Observe the LiveData, passing in this viewLifeCycleOwner as the LifecycleOwner and the observer
        viewModel.recognitions.observe(this.viewLifecycleOwner, recognitionsObserver)

    }

}