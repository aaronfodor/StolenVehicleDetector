package com.arpadfodor.stolenvehicledetector.android.app.view.utils

import android.os.Bundle
import android.text.InputType
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import com.arpadfodor.stolenvehicledetector.android.app.R
import com.arpadfodor.stolenvehicledetector.android.app.viewmodel.utils.MasterDetailViewModel
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_detail.*

class DetailFragment : AppFragment(){

    companion object{

        const val TAG = "Detail fragment"

        lateinit var viewModel: MasterDetailViewModel
        var title = ""

        var sendSucceedSnackBarText = ""
        var sendFailedSnackBarText = ""
        var alreadySentSnackBarText = ""
        var deletedSnackBarText = ""
        var deleteFailedSnackBarText = ""
        var updateSucceedSnackBarText = ""
        var updateFailedSnackBarText = ""

        fun setParams(viewModel: MasterDetailViewModel, title: String,
                      sendSucceedSnackBarText: String, sendFailedSnackBarText: String, alreadySentSnackBarText: String,
                      deletedSnackBarText: String, deleteFailedSnackBarText: String,
                      updateSucceedSnackBarText: String, updateFailedSnackBarText: String){

            this.viewModel = viewModel
            this.title = title

            this.sendSucceedSnackBarText = sendSucceedSnackBarText
            this.sendFailedSnackBarText = sendFailedSnackBarText
            this.alreadySentSnackBarText = alreadySentSnackBarText
            this.deletedSnackBarText = deletedSnackBarText
            this.deleteFailedSnackBarText = deleteFailedSnackBarText
            this.updateSucceedSnackBarText = updateSucceedSnackBarText
            this.updateFailedSnackBarText = updateFailedSnackBarText

        }

    }

    private lateinit var container: ConstraintLayout
    private lateinit var recognition_detail_title: TextView
    private lateinit var recognitionDetailImage: ImageView
    private lateinit var recognitionDetailMessage: EditText
    private lateinit var recognitionDetailLicenseId: TextView
    private lateinit var recognitionDetailDate: TextView
    private lateinit var recognitionDetailLocation: TextView
    private lateinit var detail_back_button: FloatingActionButton
    private lateinit var detail_delete_button: FloatingActionButton
    private lateinit var detail_send_button: FloatingActionButton

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        container = view as ConstraintLayout
        recognition_detail_title = container.findViewById(R.id.recognition_detail_title)
        recognitionDetailImage = container.findViewById(R.id.recognitionDetailImage)
        recognitionDetailMessage = container.findViewById(R.id.recognitionDetailMessage)
        recognitionDetailLicenseId = container.findViewById(R.id.recognitionDetailLicenseId)
        recognitionDetailDate = container.findViewById(R.id.recognitionDetailDate)
        recognitionDetailLocation = container.findViewById(R.id.recognitionDetailLocation)
        detail_back_button = container.findViewById(R.id.detail_back_button)
        detail_delete_button = container.findViewById(R.id.detail_delete_button)
        detail_send_button = container.findViewById(R.id.detail_send_button)

        recognition_detail_title.text = title

    }

    override fun subscribeToViewModel() {

        // Create the observer
        val selectedRecognitionObserver = Observer<Int> { id ->

            val currentRecognition = viewModel.getRecognitionById(id)

            if(currentRecognition == null){
                fragment_detail_parent_layout?.visibility = View.GONE
            }

            currentRecognition?.let { recognition ->

                fragment_detail_parent_layout?.visibility = View.VISIBLE

                val image = recognition.image
                image?.let { bitmap ->

                    recognitionDetailImage.let {
                        it.disappearingAnimation(requireContext())
                        Glide
                            .with(this)
                            .load(bitmap)
                            .error(R.drawable.icon_image)
                            .into(it)
                        it.appearingAnimation(requireContext())
                    }

                }

                if(recognition.isAlert){
                    detail_send_button.setImageResource(android.R.drawable.ic_input_add)
                }
                else{
                    detail_send_button.setImageResource(android.R.drawable.ic_menu_send)
                }

                //force done button on keyboard instead of the new line button
                recognitionDetailMessage.setRawInputType(InputType.TYPE_CLASS_TEXT)
                recognitionDetailMessage.text = SpannableStringBuilder(recognition.message)

                recognitionDetailLicenseId.text = recognition.licenseId
                recognitionDetailDate.text = recognition.date
                recognitionDetailLocation.text =
                    requireContext().getString(R.string.recognition_item_location_long, recognition.longitude, recognition.latitude)

                detail_back_button.setOnClickListener {
                    viewModel.deselectRecognition()
                }

                detail_delete_button.setOnClickListener {

                    viewModel.deleteRecognition(recognition.artificialId){isSuccess ->

                        val currentContext = context
                        val currentView = view
                        currentContext ?: return@deleteRecognition
                        currentView ?: return@deleteRecognition

                        when(isSuccess){

                            true -> {
                                viewModel.deselectRecognition()

                                AppSnackBarBuilder.buildInfoSnackBar(
                                    currentContext,
                                    currentView,
                                    deletedSnackBarText,
                                    Snackbar.LENGTH_SHORT
                                ).show()
                            }
                            else -> {
                                AppSnackBarBuilder.buildAlertSnackBar(
                                    currentContext,
                                    currentView,
                                    deleteFailedSnackBarText,
                                    Snackbar.LENGTH_SHORT
                                ).show()
                            }
                        }

                    }

                }

                // if the recognition has been sent -> hide send button, disable editing message
                if(recognition.isSent){

                    detail_send_button.setOnClickListener {

                        val currentContext = context
                        val currentView = view
                        currentContext ?: return@setOnClickListener
                        currentView ?: return@setOnClickListener

                        AppSnackBarBuilder.buildInfoSnackBar(
                            currentContext,
                            currentView,
                            alreadySentSnackBarText,
                            Snackbar.LENGTH_SHORT
                        ).show()

                    }

                    recognitionDetailMessage.isFocusable = false
                    recognitionDetailMessage.isClickable = false
                    recognitionDetailMessage.setOnEditorActionListener { textView, actionId, event ->
                        true
                    }

                }
                else{

                    detail_send_button.setOnClickListener {

                        viewModel.sendRecognition(recognition.artificialId){ isSuccess ->

                            val currentContext = context
                            val currentView = view
                            currentContext ?: return@sendRecognition
                            currentView ?: return@sendRecognition

                            when(isSuccess){
                                true -> {

                                    viewModel.deselectRecognition()

                                    AppSnackBarBuilder.buildSuccessSnackBar(
                                        currentContext,
                                        currentView,
                                        sendSucceedSnackBarText,
                                        Snackbar.LENGTH_SHORT
                                    ).show()

                                }
                                else -> {

                                    AppSnackBarBuilder.buildAlertSnackBar(
                                        currentContext,
                                        currentView,
                                        sendFailedSnackBarText,
                                        Snackbar.LENGTH_SHORT
                                    ).show()

                                }
                            }

                        }

                    }

                    recognitionDetailMessage.isFocusable = true
                    recognitionDetailMessage.isClickable = true
                    recognitionDetailMessage.setOnEditorActionListener { textView, actionId, event ->

                        val message = textView.text.toString()

                        return@setOnEditorActionListener when (actionId){
                            EditorInfo.IME_ACTION_DONE ->{
                                viewModel.updateRecognitionMessage(id, message){ isSuccess ->

                                    val currentContext = context
                                    val currentView = view
                                    currentContext ?: return@updateRecognitionMessage
                                    currentView ?: return@updateRecognitionMessage

                                    when(isSuccess){
                                        true -> {}
                                        else -> {
                                            AppSnackBarBuilder.buildAlertSnackBar(
                                                currentContext,
                                                currentView,
                                                updateFailedSnackBarText,
                                                Snackbar.LENGTH_SHORT
                                            ).show()
                                        }
                                    }

                                }
                                recognitionDetailMessage.clearFocus()
                                false
                            }
                            else -> {
                                false
                            }
                        }

                    }

                }

            }

            fragment_detail_parent_layout?.invalidate()
            appearingAnimations()

        }

        // Observe the LiveData, passing in this viewLifeCycleOwner as the LifecycleOwner and the observer
        viewModel.selectedRecognitionId.observe(requireActivity(), selectedRecognitionObserver)

    }

    override fun appearingAnimations(){
        detail_send_button.overshootAppearingAnimation(this.requireContext())
        detail_delete_button.overshootAppearingAnimation(this.requireContext())
        detail_back_button.overshootAppearingAnimation(this.requireContext())
    }

    override fun subscribeListeners(){}
    override fun unsubscribe(){}

}