package com.arpadfodor.stolenvehicledetector.android.app.view

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
import kotlinx.android.synthetic.main.fragment_account_manage.*
import kotlinx.android.synthetic.main.fragment_account_sign_up.*

class AccountManageFragment : AppFragment() {

    companion object{

        const val TAG = "account manage fragment"
        private lateinit var viewModel: AccountViewModel

        fun setParams(viewModel: AccountViewModel){
            this.viewModel = viewModel
        }

    }

    private lateinit var container: ConstraintLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_account_manage, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){

        super.onViewCreated(view, savedInstanceState)
        container = view as ConstraintLayout

        account_name?.text = viewModel.getAccountName()
        account_email?.text = viewModel.getAccountEmail()

    }

    override fun appearingAnimations(){
        btnLogout?.overshootAppearingAnimation(requireContext())
        btnChangePassword?.overshootAppearingAnimation(requireContext())
        btnDelete?.overshootAppearingAnimation(requireContext())
    }

    override fun subscribeToViewModel(){}

    override fun subscribeListeners() {

        btnLogout?.setOnClickListener {

            val success = {

                viewModel.fragmentTagToShow.postValue(AccountLoginFragment.TAG)

                AppSnackBarBuilder.buildSuccessSnackBar(requireContext(), container,
                    "Logged out", Snackbar.LENGTH_SHORT).show()

            }

            val error = {
                AppSnackBarBuilder.buildAlertSnackBar(requireContext(), container,
                    "Logout failed", Snackbar.LENGTH_SHORT).show()
            }

            AccountService.logout(success, error)

        }

        btnChangePassword?.setOnClickListener {

            //TODO: check old password, get new
            val currentPassword = ""
            val newPassword = ""

            val success = {
                AppSnackBarBuilder.buildSuccessSnackBar(requireContext(), container,
                    "Password changed", Snackbar.LENGTH_SHORT).show()
            }

            val error = {
                AppSnackBarBuilder.buildAlertSnackBar(requireContext(), container,
                    "Changing password failed", Snackbar.LENGTH_SHORT).show()
            }

            AccountService.changeAccountPassword(currentPassword, newPassword, success, error)

        }

        btnDelete?.setOnClickListener {

            //TODO: check password
            val password = ""

            val success = {

                viewModel.fragmentTagToShow.postValue(AccountLoginFragment.TAG)

                AppSnackBarBuilder.buildSuccessSnackBar(requireContext(), container,
                    "Account deleted", Snackbar.LENGTH_SHORT).show()

            }

            val error = {
                AppSnackBarBuilder.buildAlertSnackBar(requireContext(), container,
                    "Deleting account failed", Snackbar.LENGTH_SHORT).show()
            }

            AccountService.deleteAccount(password, success, error)

        }

    }

    override fun unsubscribe(){}

}
