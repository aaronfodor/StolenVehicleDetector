package com.arpadfodor.stolenvehicledetector.android.app.view

import android.annotation.SuppressLint
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.arpadfodor.stolenvehicledetector.android.app.ApplicationRoot
import com.arpadfodor.stolenvehicledetector.android.app.R
import com.arpadfodor.stolenvehicledetector.android.app.model.repository.dataclasses.Report
import com.arpadfodor.stolenvehicledetector.android.app.view.utils.AppActivity
import com.arpadfodor.stolenvehicledetector.android.app.view.utils.AppSnackBarBuilder
import com.arpadfodor.stolenvehicledetector.android.app.view.utils.overshootAppearingAnimation
import com.arpadfodor.stolenvehicledetector.android.app.view.utils.toBitmap
import com.arpadfodor.stolenvehicledetector.android.app.viewmodel.MapViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_map.*
import kotlinx.android.synthetic.main.content_map.*
import java.util.*

class MapActivity : AppActivity(), OnMapReadyCallback {

    companion object{
        val TAG = MapActivity::class.java.simpleName
    }

    private lateinit var viewModel: MapViewModel
    private var map: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        val drawer = findViewById<DrawerLayout>(R.id.mapActivityDrawerLayout)
        val navigation = findViewById<NavigationView>(R.id.map_navigation)
        initUi(drawer, navigation)

        viewModel = ViewModelProvider(this).get(MapViewModel::class.java)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {

        map = googleMap

        map?.let {
            it.moveCamera(CameraUpdateFactory.newLatLngZoom(viewModel.getLocation(), viewModel.zoomLevel))
            setMapStyle(it)
            enableMyLocation()
            viewModel.updateReports(){}
        }

    }

    private fun setMapStyle(map: GoogleMap) {

        try {
            // Customize the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    this,
                    R.raw.map_style
                )
            )

            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }

        }
        catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", e)
        }

    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation(){
        map?.isMyLocationEnabled = true
    }

    override fun appearingAnimations() {
        fabMapUpdate?.overshootAppearingAnimation(this)
        fabMapChange?.overshootAppearingAnimation(this)
    }

    override fun subscribeToViewModel() {

        // Create the observer which updates the UI in case of value change
        val reportsToShowObserver = Observer<Array<Report>> { reports ->

            val currentUser = viewModel.getCurrentUserId()

            val defMarkerBitmap = ContextCompat.getDrawable(this, R.drawable.icon_recognition)?.toBitmap()
            val defMarkerDesc = BitmapDescriptorFactory.fromBitmap(defMarkerBitmap)

            val currentUserMarker = ContextCompat.getDrawable(this, R.drawable.icon_my_recognition)?.toBitmap()
            val currentUserMarkerDesc = BitmapDescriptorFactory.fromBitmap(defMarkerBitmap)

            for(report in reports){

                val marker = when (currentUser) {
                    report.Reporter -> {
                        currentUserMarkerDesc
                    }
                    else -> {
                        defMarkerDesc
                    }
                }

                map?.let{

                    it.addMarker(
                    MarkerOptions()
                        .position(LatLng(report.latitude, report.longitude))
                        .title(report.Vehicle)
                        .snippet(getString(R.string.marker_snippet, report.latitude, report.longitude,
                            report.timestampUTC, report.Reporter, report.message))
                        .icon(marker)
                    )

                }

            }

        }

        // Create the observer which updates the UI in case of value change
        val mapTypeObserver = Observer<Int> { mapType ->

            map?.let {
                it.mapType = mapType

                val stringResourceId = when(mapType){
                    GoogleMap.MAP_TYPE_NORMAL -> R.string.normal_map
                    GoogleMap.MAP_TYPE_SATELLITE -> R.string.satellite_map
                    GoogleMap.MAP_TYPE_HYBRID -> R.string.hybrid_map
                    GoogleMap.MAP_TYPE_TERRAIN -> R.string.terrain_map
                    else -> R.string.normal_map
                }

                val text = getString(stringResourceId)
                showInfoSnackBar(text)
            }

        }

        // Observe the LiveData, passing in this viewLifeCycleOwner as the LifecycleOwner and the observer
        viewModel.reports.observe(this, reportsToShowObserver)
        viewModel.mapType.observe(this, mapTypeObserver)

    }

    override fun subscribeListeners() {

        fabMapUpdate.setOnClickListener {

            viewModel.updateReports(){ isSuccess ->

                if(isSuccess){

                    val preferences = PreferenceManager.getDefaultSharedPreferences(this)
                    val currentTime = Calendar.getInstance().time.toString()
                    preferences.edit().putString(getString(R.string.LAST_SYNCED_DB_REPORTS), currentTime)
                        .apply()

                    showUpdateSuccessSnackBar()

                }
                else{
                    showUpdateFailedSnackBar()
                }

            }

        }

        fabMapChange.setOnClickListener {
            viewModel.changeMapType()
        }

    }

    override fun unsubscribe() {}

    override fun onBackPressed() {
        if(activityDrawerLayout.isDrawerOpen(GravityCompat.START)){
            activityDrawerLayout.closeDrawer(GravityCompat.START)
        }
        else{
            this.finish()
        }
    }

    private fun showInfoSnackBar(text: String){
        AppSnackBarBuilder.buildInfoSnackBar(this, map_container,
            text, Snackbar.LENGTH_SHORT).show()
    }

    private fun showUpdateSuccessSnackBar(){
        AppSnackBarBuilder.buildSuccessSnackBar(this, map_container,
            getString(R.string.updated), Snackbar.LENGTH_SHORT).show()
    }

    private fun showUpdateFailedSnackBar(){
        AppSnackBarBuilder.buildAlertSnackBar(this, map_container,
            getString(R.string.update_failed), Snackbar.LENGTH_SHORT).show()
    }

}