package com.arpadfodor.stolenvehicledetector.android.app.viewmodel

import androidx.lifecycle.MutableLiveData
import com.arpadfodor.stolenvehicledetector.android.app.model.AccountService
import com.arpadfodor.stolenvehicledetector.android.app.model.LocationService
import com.arpadfodor.stolenvehicledetector.android.app.model.repository.ReportRepository
import com.arpadfodor.stolenvehicledetector.android.app.model.repository.dataclasses.Report
import com.arpadfodor.stolenvehicledetector.android.app.viewmodel.utils.AppViewModel
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng

class MapViewModel : AppViewModel(){

    val zoomLevel = 15f

    /**
     * List of reports to show on map
     **/
    val reports: MutableLiveData<Array<Report>> by lazy {
        MutableLiveData<Array<Report>>()
    }

    /**
     * MapType code
     **/
    val mapType: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>(GoogleMap.MAP_TYPE_NORMAL)
    }

    fun getLocation() : LatLng{
        val locationArray = LocationService.getLocation()
        return LatLng(locationArray[0], locationArray[1])
    }

    fun getCurrentUserId() : String{
        return AccountService.userId
    }

    fun updateReports(callback: (Boolean) -> Unit){

        ReportRepository.updateFromApi {

            ReportRepository.getReports {reportList ->
                reports.postValue(reportList.toTypedArray())
            }

            callback(it)

        }

    }

    fun changeMapType(){

        val newType = when(mapType.value){
            GoogleMap.MAP_TYPE_NORMAL -> GoogleMap.MAP_TYPE_SATELLITE
            GoogleMap.MAP_TYPE_SATELLITE -> GoogleMap.MAP_TYPE_HYBRID
            GoogleMap.MAP_TYPE_HYBRID -> GoogleMap.MAP_TYPE_TERRAIN
            GoogleMap.MAP_TYPE_TERRAIN -> GoogleMap.MAP_TYPE_NORMAL
            else -> GoogleMap.MAP_TYPE_NORMAL
        }

        mapType.postValue(newType)

    }

}