package com.arpadfodor.android.stolencardetector.view.utils

import android.os.Bundle
import androidx.navigation.ActionOnlyNavDirections
import androidx.navigation.NavDirections
import com.arpadfodor.android.stolencardetector.R
import java.util.*

object CameraFragmentDirections {

    fun actionCameraToGallery(rootDirectory: String): ActionCameraToGallery {
        return ActionCameraToGallery(
            rootDirectory
        )
    }

    fun actionCameraToPermissions(): NavDirections {
        return ActionOnlyNavDirections(R.id.action_camera_to_permissions)
    }

    class ActionCameraToGallery constructor(rootDirectory: String) : NavDirections {

        private val arguments: HashMap<String, String> = HashMap()

        private val rootDirectory: String
            get() = arguments["root_directory"] as String

        init {
            arguments["root_directory"] = rootDirectory
        }

        fun setRootDirectory(rootDirectory: String): ActionCameraToGallery {
            arguments["root_directory"] = rootDirectory
            return this
        }

        override fun getArguments(): Bundle {
            val args = Bundle()
            if (arguments.containsKey("root_directory")) {
                val rootDirectory =
                    arguments["root_directory"] as String?
                args.putString("root_directory", rootDirectory)
            }
            return args
        }

        override fun getActionId(): Int {
            return R.id.action_camera_to_gallery
        }

        override fun equals(`object`: Any?): Boolean {

            if (this === `object`) {
                return true
            }
            if (`object` == null || javaClass != `object`.javaClass) {
                return false
            }
            val that = `object` as ActionCameraToGallery
            if (arguments.containsKey("root_directory") != that.arguments.containsKey("root_directory")) {
                return false
            }
            if (rootDirectory != that.rootDirectory) {
                return false
            }

            return actionId == that.actionId

        }

        override fun hashCode(): Int {

            var result = 1
            result = 31 * result + rootDirectory.hashCode()
            result = 31 * result + actionId

            return result

        }

        override fun toString(): String {
            return "ActionCameraToGallery(actionId=$actionId){rootDirectory=$rootDirectory}"
        }

    }

}