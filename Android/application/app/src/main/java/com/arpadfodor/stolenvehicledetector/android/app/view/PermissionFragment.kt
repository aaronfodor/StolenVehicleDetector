package com.arpadfodor.stolenvehicledetector.android.app.view

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.arpadfodor.stolenvehicledetector.android.app.ApplicationRoot
import com.arpadfodor.stolenvehicledetector.android.app.R
import com.arpadfodor.stolenvehicledetector.android.app.viewmodel.CameraViewModel

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

            if(grantResults.isEmpty()){
                viewModel.hasPermissionsGranted.postValue(false)
            }
            if (grantResults.contains(PackageManager.PERMISSION_DENIED)) {
                viewModel.hasPermissionsGranted.postValue(false)
            }
            else {
                viewModel.hasPermissionsGranted.postValue(true)
            }

            proceedToNextFragment()

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
