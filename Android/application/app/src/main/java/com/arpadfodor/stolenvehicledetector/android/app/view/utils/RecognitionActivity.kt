package com.arpadfodor.stolenvehicledetector.android.app.view.utils

import android.os.Bundle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import com.arpadfodor.stolenvehicledetector.android.app.R
import com.arpadfodor.stolenvehicledetector.android.app.view.*
import com.arpadfodor.stolenvehicledetector.android.app.viewmodel.utils.RecognitionViewModel
import com.google.android.material.navigation.NavigationView

abstract class RecognitionActivity : AppActivity() {

    lateinit var viewModel: RecognitionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_recognition)
        val drawer = findViewById<DrawerLayout>(R.id.recognitionActivityDrawerLayout)
        val navigation = findViewById<NavigationView>(R.id.recognitionNavigation)
        initUi(drawer, navigation)

    }

    override fun subscribeToViewModel() {

        // Create the observer
        val showDetailsObserver = Observer<Boolean> { showDetails ->

            if(showDetails){
                removeFragment(RecognitionListFragment.TAG)
                showDetailFragment()
            }
            else{
                removeFragment(RecognitionDetailFragment.TAG)
                showListFragment()
            }

        }

        // Observe the LiveData, passing in this viewLifeCycleOwner as the LifecycleOwner and the observer
        viewModel.showDetails.observe(this, showDetailsObserver)

    }

    override fun subscribeListeners(){}
    override fun unsubscribeListeners(){}

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

    private fun showDetailFragment(){
        val fragmentTag =
            RecognitionDetailFragment.TAG
        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(R.anim.nav_default_enter_anim, R.anim.nav_default_exit_anim)
            .replace(R.id.recognition_container,
                RecognitionDetailFragment(viewModel), fragmentTag)
            .commit()
    }

    private fun showListFragment(){
        val fragmentTag =
            RecognitionListFragment.TAG
        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(R.anim.nav_default_enter_anim, R.anim.nav_default_exit_anim)
            .replace(R.id.recognition_container,
                RecognitionListFragment(viewModel), fragmentTag)
            .commit()
    }

    private fun removeFragment(fragmentTag: String){
        val toRemove = supportFragmentManager.findFragmentByTag(fragmentTag) ?: return
        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(R.anim.nav_default_enter_anim, R.anim.nav_default_exit_anim)
            .remove(toRemove)
            .commit()
    }

}
