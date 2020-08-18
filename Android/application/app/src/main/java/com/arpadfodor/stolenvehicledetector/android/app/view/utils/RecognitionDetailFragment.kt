package com.arpadfodor.stolenvehicledetector.android.app.view.utils

import android.os.Bundle
import android.text.InputType
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.arpadfodor.stolenvehicledetector.android.app.R
import com.arpadfodor.stolenvehicledetector.android.app.viewmodel.utils.RecognitionViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_recognition_detail.*

class RecognitionDetailFragment(viewModel: RecognitionViewModel, title: String) : Fragment(){

    companion object{
        const val TAG = "Recognition detail fragment"
    }

    private val viewModel = viewModel

    private val title = title

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_recognition_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recognition_detail_title.text = title
    }

    override fun onResume() {
        super.onResume()
        subscribeToViewModel()
    }

    private fun subscribeToViewModel() {

        // Create the observer
        val selectedAlertObserver = Observer<Int> { id ->

            val recognition = viewModel.getRecognitionById(id)
            recognition?.let {

                recognitionDetailImage?.setImageBitmap(recognition.image)

                //force done button on keyboard instead of the new line button
                recognitionDetailMessage?.setRawInputType(InputType.TYPE_CLASS_TEXT)
                recognitionDetailMessage?.text = SpannableStringBuilder(it.message)

                recognitionDetailLicenseId?.text = recognition.licenseId
                recognitionDetailDate?.text = recognition.date
                recognitionDetailLocation?.text =
                    requireContext().getString(R.string.recognition_item_location, recognition.longitude, recognition.latitude)

                recognition_back_button?.setOnClickListener {
                    viewModel.deselectRecognition()
                }

                recognitionDeleteButton?.setOnClickListener {

                    viewModel.deleteRecognition(recognition.artificialId){

                        viewModel.deselectRecognition()

                        val currentContext = context
                        val currentView = view
                        currentContext ?: return@deleteRecognition
                        currentView ?: return@deleteRecognition

                        AppSnackBarBuilder.buildInfoSnackBar(
                            currentContext,
                            currentView,
                            getString(R.string.report_deleted),
                            Snackbar.LENGTH_SHORT
                        ).show()

                    }

                }

                // if the recognition has been sent -> hide send button, disable editing message
                if(recognition.isActive){

                    recognitionSendButton?.setImageResource(android.R.drawable.ic_menu_send)
                    recognitionDetailMessage?.isFocusable = true
                    recognitionDetailMessage?.isClickable = true

                    recognitionSendButton?.setOnClickListener {

                        viewModel.sendRecognition(recognition.artificialId){ isSuccess ->

                            val currentContext = context
                            val currentView = view
                            currentContext ?: return@sendRecognition
                            currentView ?: return@sendRecognition

                            when(isSuccess){
                                true -> {
                                    AppSnackBarBuilder.buildSuccessSnackBar(
                                        currentContext,
                                        currentView,
                                        getString(R.string.report_sent),
                                        Snackbar.LENGTH_SHORT
                                    ).show()
                                }
                                else -> {
                                    AppSnackBarBuilder.buildAlertSnackBar(
                                        currentContext,
                                        currentView,
                                        getString(R.string.report_sending_failed),
                                        Snackbar.LENGTH_SHORT
                                    ).show()
                                }
                            }

                        }

                        viewModel.deselectRecognition()

                    }

                    recognitionDetailMessage?.setOnEditorActionListener { view, actionId, event ->

                        val message = view.text.toString()

                        return@setOnEditorActionListener when (actionId){
                            EditorInfo.IME_ACTION_DONE ->{
                                viewModel.updateRecognitionMessageById(id, message)
                                recognitionDetailMessage.clearFocus()
                                false
                            }
                            else -> {
                                false
                            }
                        }

                    }

                }
                else{

                    recognitionSendButton?.setImageResource(R.drawable.icon_tick)
                    recognitionDetailMessage?.isFocusable = false
                    recognitionDetailMessage?.isClickable = false

                    recognitionSendButton?.setOnClickListener {}
                    recognitionDetailMessage?.setOnEditorActionListener { view, actionId, event ->
                        true
                    }

                }

            }

        }

        // Observe the LiveData, passing in this viewLifeCycleOwner as the LifecycleOwner and the observer
        viewModel.selectedRecognitionId.observe(requireActivity(), selectedAlertObserver)

    }

}