package com.arpadfodor.stolenvehicledetector.android.app.view

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.constraintlayout.widget.ConstraintLayout
import com.arpadfodor.stolenvehicledetector.android.app.R
import com.arpadfodor.stolenvehicledetector.android.app.view.utils.AppFragment
import com.arpadfodor.stolenvehicledetector.android.app.view.utils.AppSnackBarBuilder
import com.arpadfodor.stolenvehicledetector.android.app.view.utils.overshootAppearingAnimation
import com.arpadfodor.stolenvehicledetector.android.app.viewmodel.AccountViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_account_login.*

class AccountLoginFragment : AppFragment() {

    companion object{

        const val TAG = "account login fragment"
        private lateinit var viewModel: AccountViewModel

        fun setParams(viewModel: AccountViewModel){
            this.viewModel = viewModel
        }

    }

    private lateinit var container: ConstraintLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_account_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)
        container = view as ConstraintLayout
    }

    override fun appearingAnimations(){
        btnUserLogin?.overshootAppearingAnimation(requireContext())
        btnForgotPassword?.overshootAppearingAnimation(requireContext())
        btnGuestLogin?.overshootAppearingAnimation(requireContext())
    }

    override fun subscribeToViewModel(){}

    override fun subscribeListeners() {

        btnUserLogin?.setOnClickListener {

            val email = input_login_email.text.toString()
            val password = input_login_password.text.toString()
            val isRememberEnabled = cbLoginRememberMe.isChecked

            val success = {
                val toStartActivity = CameraActivity::class.java
                val intent = Intent(this.context, toStartActivity)
                startActivity(intent)
            }

            val error = {
                AppSnackBarBuilder.buildAlertSnackBar(requireContext(), container,
                    getString(R.string.login_failed), Snackbar.LENGTH_SHORT).show()
            }

            viewModel.login(email, "", password, isRememberEnabled, success, error)

        }

        btnForgotPassword?.setOnClickListener {

            //TODO: read email
            val email = "a@b.com"

            val success = {
                AppSnackBarBuilder.buildSuccessSnackBar(requireContext(), container,
                    getString(R.string.new_password_sent, email), Snackbar.LENGTH_SHORT).show()
            }

            val error = {
                AppSnackBarBuilder.buildAlertSnackBar(requireContext(), container,
                    getString(R.string.new_password_failed, email), Snackbar.LENGTH_SHORT).show()
            }

            viewModel.forgotPassword(email, success, error)

        }

        btnGuestLogin?.setOnClickListener {

            val success = {
                val toStartActivity = CameraActivity::class.java
                val intent = Intent(this.context, toStartActivity)
                startActivity(intent)
            }

            val error = {
                AppSnackBarBuilder.buildAlertSnackBar(requireContext(), container,
                    getString(R.string.login_failed), Snackbar.LENGTH_SHORT).show()
            }

            viewModel.loginAsGuest(success, error)

        }

        linkSignUp?.setOnClickListener {
            viewModel.fragmentTagToShow.postValue(AccountRegisterFragment.TAG)
        }

    }

    override fun unsubscribe(){}

}
