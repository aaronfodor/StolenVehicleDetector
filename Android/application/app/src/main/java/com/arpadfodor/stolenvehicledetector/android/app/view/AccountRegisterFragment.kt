package com.arpadfodor.stolenvehicledetector.android.app.view

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.constraintlayout.widget.ConstraintLayout
import com.arpadfodor.stolenvehicledetector.android.app.R
import com.arpadfodor.stolenvehicledetector.android.app.view.utils.AppFragment
import com.arpadfodor.stolenvehicledetector.android.app.view.utils.AppSnackBarBuilder
import com.arpadfodor.stolenvehicledetector.android.app.view.utils.overshootAppearingAnimation
import com.arpadfodor.stolenvehicledetector.android.app.viewmodel.LoginViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_account_register.*

class AccountRegisterFragment : AppFragment() {

    companion object{

        const val TAG = "account register fragment"
        private lateinit var viewModel: LoginViewModel

        fun setParams(viewModel: LoginViewModel){
            this.viewModel = viewModel
        }

    }

    private lateinit var container: ConstraintLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_account_register, container, false)
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

            if(input_create_name.text.toString().isEmpty()){
                input_create_name.requestFocus()
                input_create_name.error = getString(R.string.enter_your_name)
                return@setOnClickListener
            }
            else if(input_create_email.text.toString().isEmpty()){
                input_create_email.requestFocus()
                input_create_email.error = getString(R.string.enter_your_email)
                return@setOnClickListener
            }
            else if(input_create_password.text.toString().isEmpty()){
                input_create_password.requestFocus()
                input_create_password.error = getString(R.string.enter_your_password)
                return@setOnClickListener
            }

            val email = input_create_email.text.toString()
            val name = input_create_name.text.toString()
            val password = input_create_password.text.toString()
            val isRememberEnabled = cbSignUpRememberMe.isChecked

            val success = {
                val toStartActivity = CameraActivity::class.java
                val intent = Intent(this.context, toStartActivity)
                startActivity(intent)
            }

            val error = {
                AppSnackBarBuilder.buildAlertSnackBar(requireContext(), container,
                    getString(R.string.create_account_failed), Snackbar.LENGTH_SHORT).show()
            }

            viewModel.registerAccount(email, name, password, isRememberEnabled, success, error)

        }

        linkLogin?.setOnClickListener {
            viewModel.fragmentTagToShow.postValue(AccountLoginFragment.TAG)
        }

    }

    override fun unsubscribe(){}

}
