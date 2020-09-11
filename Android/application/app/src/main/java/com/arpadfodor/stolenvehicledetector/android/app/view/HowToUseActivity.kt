package com.arpadfodor.stolenvehicledetector.android.app.view

import android.os.Bundle
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.arpadfodor.stolenvehicledetector.android.app.R
import com.arpadfodor.stolenvehicledetector.android.app.view.utils.AppActivity
import com.arpadfodor.stolenvehicledetector.android.app.view.utils.AppSnackBarBuilder
import com.arpadfodor.stolenvehicledetector.android.app.view.utils.overshootAppearingAnimation
import com.arpadfodor.stolenvehicledetector.android.app.viewmodel.HowToUseViewModel
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.content_about.*
import kotlinx.android.synthetic.main.content_how_to_use.*

class HowToUseActivity : AppActivity() {

    private lateinit var viewModel: HowToUseViewModel

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_how_to_use)
        val drawer = findViewById<DrawerLayout>(R.id.howToUseActivityDrawerLayout)
        val navigation = findViewById<NavigationView>(R.id.how_to_use_navigation)
        initUi(drawer, navigation)

        viewModel = ViewModelProvider(this).get(HowToUseViewModel::class.java)

    }

    override fun subscribeToViewModel(){

        // Create the Boolean observer which updates the UI in case of speaking state change
        val speakObserver = Observer<Boolean> { isSpeaking ->
            // Update the UI, in this case, the FAB
            if(isSpeaking){
                fabRead.setImageResource(R.drawable.icon_stop)
            }
            else{
                fabRead.setImageResource(R.drawable.icon_play)
            }
        }
        // Observe the LiveData, passing in this viewLifeCycleOwner as the LifecycleOwner and the observer
        viewModel.isTextToSpeechSpeaking.observe(this, speakObserver)

        viewModel.subscribeTextToSpeechListeners(
            errorCallback = {
                AppSnackBarBuilder.buildAlertSnackBar(
                    this,
                    window.decorView.rootView,
                    getString(R.string.text_to_speech_error),
                    Snackbar.LENGTH_SHORT
                )
            }
        )

    }

    override fun appearingAnimations() {
        fabRead.overshootAppearingAnimation(this)
    }

    override fun subscribeListeners(){

        fabRead.setOnClickListener {
            viewModel.textToSpeechButtonClicked(howToUseContent.text.toString())
        }

    }

    override fun unsubscribe(){}

}
