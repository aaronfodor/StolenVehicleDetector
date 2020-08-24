package com.arpadfodor.stolenvehicledetector.android.app.view.utils

import android.os.Bundle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import com.arpadfodor.stolenvehicledetector.android.app.R
import com.arpadfodor.stolenvehicledetector.android.app.viewmodel.utils.RecognitionViewModel
import com.google.android.material.navigation.NavigationView

abstract class MasterDetailActivity() : AppActivity() {

    open lateinit var viewModel: RecognitionViewModel

    var listName = ""
    var detailName = ""

    var sendSucceed = ""
    var sendFailed = ""
    var deleted = ""
    var alreadySent = ""
    var updateSucceed = ""
    var updateFailed = ""

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_recognition)
        val drawer = findViewById<DrawerLayout>(R.id.recognitionActivityDrawerLayout)
        val navigation = findViewById<NavigationView>(R.id.recognitionNavigation)
        initUi(drawer, navigation)

        listName = getString(R.string.recognition_list)
        detailName = getString(R.string.recognition_details)

    }

    override fun onResume() {
        super.onResume()
        // Prepare those fragments to listen to the appropriate ViewModel
        MasterFragment.setParams(viewModel, listName,
            sendSucceed, sendFailed, deleted, alreadySent)
        DetailFragment.setParams(viewModel, detailName,
            sendSucceed, sendFailed, deleted, alreadySent, updateSucceed, updateFailed)
    }

    override fun subscribeToViewModel() {

        // Create the observer
        val showDetailsObserver = Observer<Boolean> { showDetails ->

            if(showDetails){
                showFragmentByTag(DetailFragment.TAG)
            }
            else{
                showFragmentByTag(MasterFragment.TAG)
            }

        }

        // Observe the LiveData, passing in this viewLifeCycleOwner as the LifecycleOwner and the observer
        viewModel.showDetails.observe(this, showDetailsObserver)

    }

    override fun subscribeListeners(){}
    override fun unsubscribe(){}

    override fun onBackPressed() {

        when {

            activityDrawerLayout.isDrawerOpen(GravityCompat.START) -> {
                activityDrawerLayout.closeDrawer(GravityCompat.START)
            }

            viewModel.showDetails.value == true -> {
                viewModel.showDetails.postValue(false)
            }

            else -> {
                this.finish()
            }

        }

    }

    private fun showFragmentByTag(fragmentTag: String){

        var fragment = supportFragmentManager.findFragmentByTag(fragmentTag)

        if(fragment == null){

            fragment = when(fragmentTag){
                MasterFragment.TAG -> {
                    MasterFragment()
                }
                DetailFragment.TAG -> {
                    DetailFragment()
                }
                else -> null
            }

        }

        fragment?.let {
            supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.nav_default_enter_anim, R.anim.nav_default_exit_anim)
                .replace(R.id.recognition_container, fragment, fragmentTag)
                .addToBackStack(null)
                .commit()
        }

    }

}
