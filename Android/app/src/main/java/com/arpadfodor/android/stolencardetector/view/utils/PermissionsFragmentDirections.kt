package com.arpadfodor.android.stolencardetector.view.utils

import androidx.navigation.ActionOnlyNavDirections
import androidx.navigation.NavDirections
import com.arpadfodor.android.stolencardetector.R

object PermissionsFragmentDirections {

    fun actionPermissionsToCamera(): NavDirections {
        return ActionOnlyNavDirections(R.id.action_permissions_to_camera)
    }

}