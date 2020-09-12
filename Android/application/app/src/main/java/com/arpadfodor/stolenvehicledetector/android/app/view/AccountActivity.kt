package com.arpadfodor.stolenvehicledetector.android.app.view

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.arpadfodor.stolenvehicledetector.android.app.R
import com.arpadfodor.stolenvehicledetector.android.app.view.utils.AppDialog
import com.arpadfodor.stolenvehicledetector.android.app.viewmodel.AccountViewModel

class AccountActivity : AppCompatActivity() {

    private lateinit var viewModel: AccountViewModel

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        val showManageFragment = intent.getBooleanExtra("show account manage", false)

        viewModel = ViewModelProvider(this).get(AccountViewModel::class.java)

        if(showManageFragment){

            if(viewModel.isCurrentAccountGuest()){
                viewModel.fragmentTagToShow.postValue(AccountLoginFragment.TAG)
            }
            else{
                viewModel.fragmentTagToShow.postValue(AccountManageFragment.TAG)
            }

        }
        else{
            showPermissionFragment()
        }

    }

    override fun onResume() {

        super.onResume()
        subscribeToViewModel()

        AccountLoginFragment.setParams(viewModel)
        AccountRegisterFragment.setParams(viewModel)
        AccountManageFragment.setParams(viewModel)

    }

    fun subscribeToViewModel() {

        // Create the observer which updates the UI in case of value change
        val fragmentTagToShowObserver = Observer<String> { fragmentTag ->

            val fragment = when(fragmentTag){
                AccountLoginFragment.TAG -> {
                    AccountLoginFragment()
                }
                AccountManageFragment.TAG -> {
                    AccountManageFragment()
                }
                AccountRegisterFragment.TAG -> {
                    AccountRegisterFragment()
                }
                else -> null
            }

            fragment?.let {
                supportFragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.nav_default_enter_anim, R.anim.nav_default_exit_anim)
                    .replace(R.id.account_container, fragment, fragmentTag)
                    .addToBackStack(null)
                    .commit()
            }

        }

        // Observe the LiveData, passing in this viewLifeCycleOwner as the LifecycleOwner and the observer
        viewModel.fragmentTagToShow.observe(this, fragmentTagToShowObserver)

    }

    private fun showPermissionFragment(){
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.account_container, PermissionsFragment {
                tryAutoLogin()
            })
            .commit()
    }

    private fun tryAutoLogin(){

        val success = {
            val toStartActivity = CameraActivity::class.java
            val intent = Intent(this, toStartActivity)
            startActivity(intent)
        }

        val error = {
            viewModel.fragmentTagToShow.postValue(AccountLoginFragment.TAG)
        }

        viewModel.tryAutoLogin(success, error)

    }

    override fun onBackPressed() {

        if(viewModel.fragmentTagToShow.value == AccountManageFragment.TAG){
            this.finish()
        }
        else{
            exitDialog()
        }

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
