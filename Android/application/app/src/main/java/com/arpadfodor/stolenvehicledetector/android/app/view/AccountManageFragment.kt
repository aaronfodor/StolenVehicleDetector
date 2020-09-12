package com.arpadfodor.stolenvehicledetector.android.app.view

import android.os.Bundle
import android.view.*
import androidx.constraintlayout.widget.ConstraintLayout
import com.arpadfodor.stolenvehicledetector.android.app.R
import com.arpadfodor.stolenvehicledetector.android.app.view.utils.AppFragment
import com.arpadfodor.stolenvehicledetector.android.app.view.utils.AppSnackBarBuilder
import com.arpadfodor.stolenvehicledetector.android.app.view.utils.overshootAppearingAnimation
import com.arpadfodor.stolenvehicledetector.android.app.viewmodel.AccountViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_account_manage.*

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
        btnUpdateName?.overshootAppearingAnimation(requireContext())
        btnUpdatePassword?.overshootAppearingAnimation(requireContext())
        btnDelete?.overshootAppearingAnimation(requireContext())
    }

    override fun subscribeToViewModel(){}

    override fun subscribeListeners() {

        btnLogout?.setOnClickListener {

            val success = {

                AppSnackBarBuilder.buildSuccessSnackBar(requireContext(), container,
                    getString(R.string.logged_out), Snackbar.LENGTH_SHORT).show()

                viewModel.fragmentTagToShow.postValue(AccountLoginFragment.TAG)

            }

            val error = {
                AppSnackBarBuilder.buildAlertSnackBar(requireContext(), container,
                    getString(R.string.logout_failed), Snackbar.LENGTH_SHORT).show()
            }

            viewModel.logout(success, error)

        }

        btnDelete?.setOnClickListener {

            //TODO: read password
            val password = ""

            val success = {

                viewModel.fragmentTagToShow.postValue(AccountLoginFragment.TAG)

                AppSnackBarBuilder.buildSuccessSnackBar(requireContext(), container,
                    getString(R.string.account_deleted), Snackbar.LENGTH_SHORT).show()

            }

            val error = {
                AppSnackBarBuilder.buildAlertSnackBar(requireContext(), container,
                    getString(R.string.account_delete_failed), Snackbar.LENGTH_SHORT).show()
            }

            viewModel.deleteAccount(password, success, error)

        }

        btnUpdateName?.setOnClickListener {

            //TODO: read new name
            val newName = ""

            val success = {
                AppSnackBarBuilder.buildSuccessSnackBar(requireContext(), container,
                    getString(R.string.name_changed), Snackbar.LENGTH_SHORT).show()
            }

            val error = {
                AppSnackBarBuilder.buildAlertSnackBar(requireContext(), container,
                    getString(R.string.name_change_failed), Snackbar.LENGTH_SHORT).show()
            }

            viewModel.changeAccountName(newName, success, error)

        }

        btnUpdatePassword?.setOnClickListener {

            //TODO: check old password, get new
            val currentPassword = ""
            val newPassword = ""

            val success = {
                AppSnackBarBuilder.buildSuccessSnackBar(requireContext(), container,
                    getString(R.string.password_changed), Snackbar.LENGTH_SHORT).show()
            }

            val error = {
                AppSnackBarBuilder.buildAlertSnackBar(requireContext(), container,
                    getString(R.string.password_change_failed), Snackbar.LENGTH_SHORT).show()
            }

            viewModel.changeAccountPassword(currentPassword, newPassword, success, error)

        }

    }

    override fun unsubscribe(){}

}
