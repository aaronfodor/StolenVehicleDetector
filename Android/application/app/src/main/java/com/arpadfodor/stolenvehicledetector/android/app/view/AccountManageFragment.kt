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
    }

    override fun appearingAnimations(){
        btnLogout?.overshootAppearingAnimation(requireContext())
        btnEdit?.overshootAppearingAnimation(requireContext())
    }

    override fun subscribeToViewModel(){}

    override fun subscribeListeners() {

        account_name?.text = viewModel.getCurrentUserName()
        account_email?.text = viewModel.getCurrentUserEmail()

        btnLogout?.setOnClickListener {

            val success = {
                startActivity(Intent(this.requireActivity(), LoginActivity::class.java))
            }

            val error = {
                AppSnackBarBuilder.buildAlertSnackBar(requireContext(), container,
                    getString(R.string.logout_failed), Snackbar.LENGTH_SHORT).show()
            }

            viewModel.logout(success, error)

        }

        btnEdit?.setOnClickListener {
            viewModel.fragmentTagToShow.postValue(AccountEditFragment.TAG)
        }

    }

    override fun unsubscribe(){}

}
