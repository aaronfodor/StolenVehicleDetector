package com.arpadfodor.android.stolencardetector.view

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import com.arpadfodor.android.stolencardetector.R
import com.arpadfodor.android.stolencardetector.view.utils.PermissionsFragmentDirections

private const val PERMISSIONS_REQUEST_CODE = 10
private val PERMISSIONS_REQUIRED = arrayOf(Manifest.permission.CAMERA)

class PermissionFragment : Fragment() {

    companion object {
        /**
         * Convenience method used to check if all permissions required by the app are granted
         */
        fun hasPermissions(context: Context) = PERMISSIONS_REQUIRED.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        if(!hasPermissions(
                requireContext()
            )
        ){
            // Request required permissions
            requestPermissions(
                PERMISSIONS_REQUIRED,
                PERMISSIONS_REQUEST_CODE
            )
        }
        else{
            // If permissions have already been granted, proceed
            Navigation.findNavController(requireActivity(),
                R.id.fragment_container
            ).navigate(PermissionsFragmentDirections.actionPermissionsToCamera())
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == PERMISSIONS_REQUEST_CODE){

            if(PackageManager.PERMISSION_GRANTED == grantResults.firstOrNull()){
                // Take the user to the success fragment when permission is granted
                Toast.makeText(context, getString(R.string.permission_granted), Toast.LENGTH_LONG).show()
                Navigation.findNavController(requireActivity(),
                    R.id.fragment_container
                ).navigate(
                    PermissionsFragmentDirections.actionPermissionsToCamera()
                )
            }
            else{
                Toast.makeText(context, getString(R.string.permission_denied), Toast.LENGTH_LONG).show()
            }

        }

    }

}
