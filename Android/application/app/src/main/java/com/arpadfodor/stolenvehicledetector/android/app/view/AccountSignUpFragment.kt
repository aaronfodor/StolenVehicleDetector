package com.arpadfodor.stolenvehicledetector.android.app.view

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.constraintlayout.widget.ConstraintLayout
import com.arpadfodor.stolenvehicledetector.android.app.R
import com.arpadfodor.stolenvehicledetector.android.app.model.AccountService
import com.arpadfodor.stolenvehicledetector.android.app.view.utils.AppFragment
import com.arpadfodor.stolenvehicledetector.android.app.view.utils.AppSnackBarBuilder
import com.arpadfodor.stolenvehicledetector.android.app.view.utils.overshootAppearingAnimation
import com.arpadfodor.stolenvehicledetector.android.app.viewmodel.AccountViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_account_sign_up.*

class AccountSignUpFragment : AppFragment() {

    companion object{

        const val TAG = "account sign up fragment"
        private lateinit var viewModel: AccountViewModel

        fun setParams(viewModel: AccountViewModel){
            this.viewModel = viewModel
        }

    }

    private lateinit var container: ConstraintLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_account_sign_up, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)
        container = view as ConstraintLayout
    }

    override fun appearingAnimations(){
        btnCreateAccount?.overshootAppearingAnimation(requireContext())
    }

    override fun subscribeToViewModel(){}

    override fun subscribeListeners() {

        btnCreateAccount?.setOnClickListener {

            val name = input_create_name.text.toString()
            val email = input_create_email.text.toString()
            val password = input_create_password.text.toString()
            val isRememberEnabled = cbSignUpRememberMe.isChecked

            val success = {
                val toStartActivity = CameraActivity::class.java
                val intent = Intent(this.context, toStartActivity)
                startActivity(intent)
            }

            val error = {
                AppSnackBarBuilder.buildAlertSnackBar(requireContext(), container,
                    "Creating account failed", Snackbar.LENGTH_SHORT).show()
            }

            AccountService.registerAccount(name, email, password, isRememberEnabled, success, error)

        }

        linkLogIn?.setOnClickListener {
            viewModel.fragmentTagToShow.postValue(AccountLoginFragment.TAG)
        }

    }

    override fun unsubscribe(){}

}
