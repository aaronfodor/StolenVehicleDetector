package com.arpadfodor.stolenvehicledetector.android.app.model

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.getSystemService
import java.lang.Exception
import java.util.*

object LocationService {

    private lateinit var appContext: Context
    private lateinit var locationManager: LocationManager

    private var deviceLatitude = 0.0
    private var deviceLongitude = 0.0

    private const val MINIMUM_TIME_BETWEEN_REFRESH = 5000L
    private var locationTimeStamp = 0L

    private var isLocationUpdating = false

    fun initialize(appContext_: Context){
        appContext = appContext_
        locationManager = getSystemService(appContext, LocationManager::class.java) as LocationManager
        updateLocation()
    }

    /**
     * Retrieves the location to the given lambda
     * */
    fun updateLocation(){

        var latitude = 0.0
        var longitude = 0.0

        if (ActivityCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        // if the minimum time since the last refresh is not exceeded, do not refresh location
        if(locationTimeStamp + MINIMUM_TIME_BETWEEN_REFRESH > Calendar.getInstance().timeInMillis){
            return
        }

        if(isLocationUpdating){
            return
        }

        isLocationUpdating = true

        Thread {

            try{

                val gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                val networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

                if(gpsLocation != null){
                    latitude = gpsLocation.latitude
                    longitude = gpsLocation.longitude
                }
                else if(networkLocation != null){
                    latitude = networkLocation.latitude
                    longitude = networkLocation.longitude
                }

            }
            catch (e: Exception){
                e.printStackTrace()
            }

            if(latitude != 0.0 && longitude != 0.0){
                deviceLatitude = latitude
                deviceLongitude = longitude
                locationTimeStamp = Calendar.getInstance().timeInMillis
            }

            isLocationUpdating = false

        }.start()

    }

    fun getLocation() : Array<Double>{
        return arrayOf(deviceLatitude, deviceLongitude)
    }

}