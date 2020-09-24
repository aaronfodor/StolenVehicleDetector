package com.arpadfodor.stolenvehicledetector.android.app.view

import android.content.Intent
import android.os.Bundle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.arpadfodor.stolenvehicledetector.android.app.R
import com.arpadfodor.stolenvehicledetector.android.app.view.utils.AppActivity
import com.arpadfodor.stolenvehicledetector.android.app.view.utils.overshootAppearingAnimation
import com.arpadfodor.stolenvehicledetector.android.app.viewmodel.AccountViewModel
import com.arpadfodor.stolenvehicledetector.android.app.viewmodel.utils.AppViewModel
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.content_account.*

class AccountActivity : AppActivity() {

    override lateinit var viewModel: AccountViewModel

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)
        val drawer = findViewById<DrawerLayout>(R.id.accountActivityDrawerLayout)
        val navigation = findViewById<NavigationView>(R.id.account_navigation)
        viewModel = ViewModelProvider(this).get(AccountViewModel::class.java)
        initUi(drawer, navigation)

        if(viewModel.isCurrentAccountGuest()){
            startActivity(Intent(this, LoginActivity::class.java))
        }
        else{
            viewModel.fragmentTagToShow.postValue(AccountManageFragment.TAG)
        }

    }

    override fun onResume() {

        super.onResume()
        subscribeToViewModel()

        AccountManageFragment.setParams(viewModel)
        AccountEditFragment.setParams(viewModel)

    }

    override fun appearingAnimations() {
        accountLogo.overshootAppearingAnimation(this)
    }

    override fun subscribeToViewModel() {

        // Create the observer which updates the UI in case of value change
        val fragmentTagToShowObserver = Observer<String> { fragmentTag ->

            val fragment = when(fragmentTag){
                AccountManageFragment.TAG -> {
                    AccountManageFragment()
                }
                AccountEditFragment.TAG -> {
                    AccountEditFragment()
                }
                else -> null
            }

            fragment?.let {
                supportFragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.nav_default_enter_anim, R.anim.nav_default_exit_anim)
                    .replace(R.id.account_fragment_container, fragment, fragmentTag)
                    .addToBackStack(null)
                    .commit()
            }

        }

        // Observe the LiveData, passing in this viewLifeCycleOwner as the LifecycleOwner and the observer
        viewModel.fragmentTagToShow.observe(this, fragmentTagToShowObserver)

    }

    override fun subscribeListeners() {}

    override fun unsubscribe() {}

    override fun onBackPressed() {

        if(activityDrawerLayout.isDrawerOpen(GravityCompat.START)){
            activityDrawerLayout.closeDrawer(GravityCompat.START)
        }
        else{

            if(viewModel.fragmentTagToShow.value == AccountEditFragment.TAG) {
                viewModel.fragmentTagToShow.postValue(AccountManageFragment.TAG)
            }
            else{
                this.finish()
            }

        }

    }

}
