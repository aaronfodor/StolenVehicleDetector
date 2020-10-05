package com.arpadfodor.stolenvehicledetector.android.app.view

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.arpadfodor.stolenvehicledetector.android.app.ApplicationRoot
import com.arpadfodor.stolenvehicledetector.android.app.view.utils.AppFragment

private const val PERMISSIONS_REQUEST_CODE = 10
private val PERMISSIONS_REQUIRED = ApplicationRoot.requiredPermissions

/**
 * The only purpose of this fragment is to request permissions.
 * Once granted, proceed.
 */
class PermissionsFragment(finished: () -> Unit = {}) : AppFragment() {

    companion object {
        /** Convenience method used to check if all permissions required by this app are granted */
        fun hasPermissions(context: Context) = PERMISSIONS_REQUIRED.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    val actionFinished = finished

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        if (!hasPermissions(requireContext())) {
            // Request permissions
            requestPermissions(PERMISSIONS_REQUIRED, PERMISSIONS_REQUEST_CODE)
        }
        else {
            removeThisFragment()
        }

    }

    override fun appearingAnimations(){}
    override fun subscribeToViewModel(){}
    override fun subscribeListeners(){}
    override fun unsubscribe(){}

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSIONS_REQUEST_CODE) {

            if(grantResults.isEmpty()){
                //not granted
            }
            if (grantResults.contains(PackageManager.PERMISSION_DENIED)) {
                //not granted
            }
            else {
                //granted
            }

            removeThisFragment()

        }

    }

    private fun removeThisFragment(){
        requireActivity().supportFragmentManager.beginTransaction().remove(this).commit()
        actionFinished()
    }

}
