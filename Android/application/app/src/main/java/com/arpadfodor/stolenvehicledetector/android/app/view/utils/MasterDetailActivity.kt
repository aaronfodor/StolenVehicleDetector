package com.arpadfodor.stolenvehicledetector.android.app.view.utils

import android.os.Bundle
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import com.arpadfodor.stolenvehicledetector.android.app.R
import com.arpadfodor.stolenvehicledetector.android.app.viewmodel.utils.MasterDetailViewModel
import com.google.android.material.navigation.NavigationView

abstract class MasterDetailActivity() : AppActivity() {

    open lateinit var viewModel: MasterDetailViewModel
    var twoPane = false

    var listName = ""
    var detailName = ""

    var sendSucceed = ""
    var sendFailed = ""
    var deleted = ""
    var deleteFailed = ""
    var alreadySent = ""
    var updateSucceed = ""
    var updateFailed = ""

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_master_detail)
        val drawer = findViewById<DrawerLayout>(R.id.recognitionActivityDrawerLayout)
        val navigation = findViewById<NavigationView>(R.id.recognitionNavigation)
        initUi(drawer, navigation)

        listName = getString(R.string.recognition_list)
        detailName = getString(R.string.recognition_details)

        if (findViewById<ConstraintLayout>(R.id.detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            twoPane = true
        }

    }

    override fun onResume() {

        viewModel.twoPane = twoPane

        super.onResume()
        // Prepare those fragments to listen to the appropriate ViewModel
        MasterFragment.setParams(viewModel, listName,
            sendSucceed, sendFailed, alreadySent, deleted, deleteFailed)
        DetailFragment.setParams(viewModel, detailName,
            sendSucceed, sendFailed, alreadySent, deleted, deleteFailed, updateSucceed, updateFailed)

    }

    override fun subscribeToViewModel() {

        if(viewModel.twoPane){
            showMasterAndDetailFragments()
        }
        else{

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

    }

    override fun subscribeListeners(){}
    override fun unsubscribe(){}

    override fun onBackPressed() {

        when {

            activityDrawerLayout.isDrawerOpen(GravityCompat.START) -> {
                activityDrawerLayout.closeDrawer(GravityCompat.START)
            }

            viewModel.showDetails.value == true -> {
                viewModel.deselectRecognition()
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
                .replace(R.id.master_detail_container, fragment, fragmentTag)
                .addToBackStack(null)
                .commit()
        }

    }

    private fun showMasterAndDetailFragments(){

        val masterFragment = MasterFragment()
        val detailFragment = DetailFragment()

        masterFragment.let {
            supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.nav_default_enter_anim, R.anim.nav_default_exit_anim)
                .replace(R.id.master_detail_container, it)
                .addToBackStack(null)
                .commit()
        }

        detailFragment.let {
            supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.nav_default_enter_anim, R.anim.nav_default_exit_anim)
                .replace(R.id.detail_container, it)
                .addToBackStack(null)
                .commit()
        }

    }

}
