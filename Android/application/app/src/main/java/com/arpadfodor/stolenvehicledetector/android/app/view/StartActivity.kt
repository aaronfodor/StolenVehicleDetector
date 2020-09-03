package com.arpadfodor.stolenvehicledetector.android.app.view

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.arpadfodor.stolenvehicledetector.android.app.R
import com.arpadfodor.stolenvehicledetector.android.app.view.utils.AppDialog
import com.arpadfodor.stolenvehicledetector.android.app.view.utils.AppSnackBarBuilder
import com.arpadfodor.stolenvehicledetector.android.app.viewmodel.StartViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_start.*

class StartActivity : AppCompatActivity() {

    private lateinit var viewModel: StartViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        viewModel = ViewModelProvider(this).get(StartViewModel::class.java)
        showPermissionFragment()

    }

    override fun onResume() {
        super.onResume()
        subscribeToViewModel()
    }

    fun subscribeToViewModel() {

        // Create the observer which updates the UI in case of value change
        val hasPermissionsGranted = Observer<Boolean> { permissionsGranted ->
            if(!permissionsGranted){
                showMissingPermissionNotification()
            }
        }

        // Observe the LiveData, passing in this viewLifeCycleOwner as the LifecycleOwner and the observer
        viewModel.hasPermissionsGranted.observe(this, hasPermissionsGranted)

    }

    private fun showPermissionFragment(){
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.start_container, PermissionsFragment())
            .commit()
    }

    private fun showMissingPermissionNotification(){
        AppSnackBarBuilder.buildInfoSnackBar(this.applicationContext, start_activity_layout,
            getString(R.string.permission_denied), Snackbar.LENGTH_LONG).show()
    }

    override fun onBackPressed() {
        exitDialog()
    }

    /**
     * Asks for exit confirmation
     **/
    private fun exitDialog(){

        val exitDialog = AppDialog(this, getString(R.string.exit_title),
            getString(R.string.exit_dialog), R.drawable.warning)
        exitDialog.setPositiveButton {
            //showing the home screen - app is not visible but running
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_HOME)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
        }
        exitDialog.show()

    }

}
