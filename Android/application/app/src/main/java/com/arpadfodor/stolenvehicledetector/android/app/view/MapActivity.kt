package com.arpadfodor.stolenvehicledetector.android.app.view

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.arpadfodor.stolenvehicledetector.android.app.R
import com.arpadfodor.stolenvehicledetector.android.app.model.repository.dataclasses.Report
import com.arpadfodor.stolenvehicledetector.android.app.view.utils.AppActivity
import com.arpadfodor.stolenvehicledetector.android.app.view.utils.overshootAppearingAnimation
import com.arpadfodor.stolenvehicledetector.android.app.view.utils.toBitmap
import com.arpadfodor.stolenvehicledetector.android.app.viewmodel.MapViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.content_map.*
import java.util.*

class MapActivity : AppActivity(), OnMapReadyCallback {

    companion object{
        val TAG = MapActivity::class.java.simpleName
    }

    override lateinit var viewModel: MapViewModel
    private var map: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        val drawer = findViewById<DrawerLayout>(R.id.mapActivityDrawerLayout)
        val navigation = findViewById<NavigationView>(R.id.map_navigation)
        viewModel = ViewModelProvider(this).get(MapViewModel::class.java)
        initUi(drawer, navigation)

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

            // hide the compass button
            it.uiSettings.isCompassEnabled = false

            it.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    viewModel.getLocation(),
                    viewModel.zoomLevel
                )
            )
            setMapStyle(it)
            enableMyLocation()
            viewModel.updateReports(){}


            // Set a custom info window adapter for the google map
            it.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {

                // Use default InfoWindow frame
                override fun getInfoWindow(arg0: Marker?): View? {
                    return null
                }

                // Defines the contents of the InfoWindow
                override fun getInfoContents(arg0: Marker): View? {

                    val licenseId = arg0.title
                    val details = arg0.snippet

                    // Getting view from the layout file
                    val container= layoutInflater.inflate(R.layout.infowindow_map, null)
                    val tvTitle = container.findViewById(R.id.mapRecognitionLicenseId) as TextView
                    val tvInfo = container.findViewById(R.id.mapRecognitionDetails) as TextView

                    tvTitle.text = licenseId
                    tvInfo.text = details

                    // Returning the view containing InfoWindow contents
                    return container
                }

            })

        }

    }

    private fun setMapStyle(map: GoogleMap) {

        try {
            // Customize the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success =
                map.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))
            setMapType(viewModel.mapType.value ?: GoogleMap.MAP_TYPE_NORMAL)

            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }

        }
        catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", e)
        }

    }

    private fun enableMyLocation() : Boolean{

        if (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return false
        }

        map?.isMyLocationEnabled = true
        return true

    }

   override fun appearingAnimations() {
        fabMapUpdate?.overshootAppearingAnimation(this)
        fabMapChange?.overshootAppearingAnimation(this)
    }

    override fun subscribeToViewModel() {

        // Create the observer which updates the UI in case of value change
        val reportsToShowObserver = Observer<Array<Report>> { reports ->

            map?.let{

                viewModel.removeMapMarkers()

                val markerBitmap = ContextCompat.getDrawable(this, R.drawable.icon_recognition)?.toBitmap()
                val markerDesc = BitmapDescriptorFactory.fromBitmap(markerBitmap)

                for(report in reports){

                    val currentMarker = it.addMarker(
                        MarkerOptions()
                            .position(LatLng(report.latitude, report.longitude))
                            .title(report.Vehicle)
                            .snippet(
                                getString(
                                    R.string.marker_snippet, report.latitude, report.longitude,
                                    report.timestampUTC, report.Reporter, report.message
                                )
                            )
                            .icon(markerDesc)
                    )

                    viewModel.markers.add(currentMarker)

                }

            }

        }

        // Create the observer which updates the UI in case of a value change
        val mapTypeObserver = Observer<Int> { mapType ->
            setMapType(mapType)
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
                    preferences.edit().putString(
                        getString(R.string.LAST_SYNCED_DB_REPORTS),
                        currentTime
                    )
                        .apply()

                    showSuccessSnackBar(getString(R.string.updated))

                }
                else{
                    showErrorSnackBar(getString(R.string.update_failed))
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

    private fun setMapType(mapType: Int) {

        map?.let {

            it.mapType = mapType

            val stringResourceId = when (mapType) {
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

}