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
import kotlinx.android.synthetic.main.fragment_account_edit.*

class AccountEditFragment : AppFragment() {

    companion object{

        const val TAG = "account edit fragment"
        private lateinit var viewModel: AccountViewModel

        fun setParams(viewModel: AccountViewModel){
            this.viewModel = viewModel
        }

    }

    private lateinit var container: ConstraintLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_account_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)
        container = view as ConstraintLayout
    }

    override fun appearingAnimations(){
        btnCancel?.overshootAppearingAnimation(requireContext())
        btnApply?.overshootAppearingAnimation(requireContext())
    }

    override fun subscribeToViewModel(){}

    override fun subscribeListeners() {

        btnCancel?.setOnClickListener {
            viewModel.fragmentTagToShow.postValue(AccountManageFragment.TAG)
        }

        btnApply?.setOnClickListener {

            val newName = input_change_name.text.toString()
            val newPassword = input_change_password.text.toString()

            if(newName.isEmpty() && newPassword.isEmpty()){
                AppSnackBarBuilder.buildInfoSnackBar(requireContext(), container,
                    getString(R.string.empty_change), Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val success = {

                AppSnackBarBuilder.buildSuccessSnackBar(requireContext(), container,
                    getString(R.string.change_applied), Snackbar.LENGTH_SHORT).show()

                viewModel.fragmentTagToShow.postValue(AccountManageFragment.TAG)

            }

            val error = {
                AppSnackBarBuilder.buildAlertSnackBar(requireContext(), container,
                    getString(R.string.change_failed), Snackbar.LENGTH_SHORT).show()
            }

            viewModel.editAccount(newName, newPassword, success, error)

        }

        linkDelete?.setOnClickListener {

            val success = {
                startActivity(Intent(requireActivity(), LoginActivity::class.java))
            }

            val error = {
                AppSnackBarBuilder.buildAlertSnackBar(requireContext(), container,
                    getString(R.string.account_delete_failed), Snackbar.LENGTH_SHORT).show()
            }

            viewModel.deleteAccount(success, error)

        }

    }

    override fun unsubscribe(){}

}
