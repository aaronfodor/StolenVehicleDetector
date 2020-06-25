package com.arpadfodor.android.stolenvehicledetector.view

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.arpadfodor.android.stolenvehicledetector.ApplicationRoot
import com.arpadfodor.android.stolenvehicledetector.R
import com.arpadfodor.android.stolenvehicledetector.viewmodel.CameraViewModel

private const val PERMISSIONS_REQUEST_CODE = 10
private val PERMISSIONS_REQUIRED = ApplicationRoot.requiredPermissions

/**
 * The only purpose of this fragment is to request permissions.
 * Once granted, proceed.
 */
class PermissionsFragment : Fragment() {

    private val viewModel: CameraViewModel by activityViewModels()

    companion object {
        /** Convenience method used to check if all permissions required by this app are granted */
        fun hasPermissions(context: Context) = PERMISSIONS_REQUIRED.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        if (!hasPermissions(requireContext())) {
            // Request permissions
            requestPermissions(PERMISSIONS_REQUIRED, PERMISSIONS_REQUEST_CODE)
        }
        else {
            proceedToNextFragment()
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSIONS_REQUEST_CODE) {

            if (PackageManager.PERMISSION_GRANTED == grantResults.firstOrNull()) {
                viewModel.hasPermissionsGranted.postValue(true)
                // Take the user to the success fragment when permission is granted
                proceedToNextFragment()
            }
            else {
                viewModel.hasPermissionsGranted.postValue(false)
            }

        }

    }

    private fun proceedToNextFragment(){
        // If permissions have already been granted, proceed
        val nextFragment = CameraFragment()
        activity?.supportFragmentManager?.beginTransaction()
            ?.replace(R.id.camera_container, nextFragment, "cameraFragment")
            ?.addToBackStack(null)
            ?.commit()
    }

}
