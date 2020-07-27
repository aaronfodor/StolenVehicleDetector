package com.arpadfodor.stolenvehicledetector.android.app.view

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.arpadfodor.stolenvehicledetector.android.app.R
import com.arpadfodor.stolenvehicledetector.android.app.viewmodel.AlertViewModel
import com.arpadfodor.stolenvehicledetector.android.app.viewmodel.utils.Recognition
import kotlinx.android.synthetic.main.fragment_recognition_detail.*

class RecognitionDetailFragment() : Fragment(){

    companion object{
        const val TAG = "Recognition detail fragment"
    }

    private val viewModel: AlertViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_recognition_detail, container, false)
    }

    override fun onResume() {
        super.onResume()
        subscribeToViewModel()
    }

    private fun subscribeToViewModel() {

        // Create the observer
        val selectedAlertObserver = Observer<Int> { id ->

            val recognition = viewModel.getAlertById(id)
            recognition?.let {

                recognitionDetailLicenseId?.text = recognition.licenseId
                recognitionDetailImage?.setImageBitmap(recognition.image)
                recognitionDetailDate?.text = requireContext().getString(R.string.recognition_item_timestamp,
                    recognition.date)
                recognitionDetailLocation?.text = requireContext().getString(R.string.recognition_item_location,
                    recognition.longitude, recognition.latitude)

                recognition_back_button?.setOnClickListener {
                    viewModel.deselectRecognition()
                }

                recognition_delete_button?.setOnClickListener {
                    viewModel.deleteRecognition(recognition.artificialId)
                    viewModel.deselectRecognition()
                }

                recognition_send_button?.setOnClickListener {
                    viewModel.sendRecognition(recognition.artificialId)
                    viewModel.deselectRecognition()
                }

                recognitionDetailMessage?.addTextChangedListener {
                    it?.let{
                        val message = it.toString()
                        viewModel.updateAlertMessageById(id, it.toString())
                    }
                }

            }

        }

        // Observe the LiveData, passing in this viewLifeCycleOwner as the LifecycleOwner and the observer
        viewModel.selectedAlertId.observe(requireActivity(), selectedAlertObserver)

    }

}