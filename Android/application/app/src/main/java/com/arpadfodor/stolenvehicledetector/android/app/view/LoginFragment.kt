package com.arpadfodor.stolenvehicledetector.android.app.view

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.constraintlayout.widget.ConstraintLayout
import com.arpadfodor.stolenvehicledetector.android.app.R
import kotlinx.android.synthetic.main.fragment_login.*

class LoginFragment : Fragment() {

    companion object{
        private const val TAG = "login fragment"
    }

    private lateinit var container: ConstraintLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)
        container = view as ConstraintLayout
    }

    override fun onResume() {

        super.onResume()

        btnSkipLogin.setOnClickEvent {
            val toStartActivity = CameraActivity::class.java
            val intent = Intent(this.context, toStartActivity)
            startActivity(intent)
        }

    }

}
