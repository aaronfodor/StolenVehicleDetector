package com.arpadfodor.stolenvehicledetector.android.app.view.utils

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.arpadfodor.stolenvehicledetector.android.app.R
import com.arpadfodor.stolenvehicledetector.android.app.view.utils.AppSnackBarBuilder
import com.arpadfodor.stolenvehicledetector.android.app.view.utils.RecognitionEventListener
import com.arpadfodor.stolenvehicledetector.android.app.view.utils.RecognitionListAdapter
import com.arpadfodor.stolenvehicledetector.android.app.viewmodel.utils.RecognitionViewModel
import com.arpadfodor.stolenvehicledetector.android.app.viewmodel.utils.Recognition
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_recognition_list.*

class RecognitionListFragment(viewModel: RecognitionViewModel) : Fragment(){

    companion object{
        const val TAG = "Recognition list fragment"
    }

    private val viewModel = viewModel
    private lateinit var adapter: RecognitionListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_recognition_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){

        super.onViewCreated(view, savedInstanceState)

        adapter = RecognitionListAdapter(requireContext(), createEventListener())
        alert_list.adapter = adapter
        alert_list.layoutManager = LinearLayoutManager(requireContext())

    }

    private fun subscribeToViewModel() {

        // Create the observer
        val listObserver = Observer<List<Recognition>> { list ->
            adapter.submitList(list)
        }

        // Observe the LiveData, passing in this viewLifeCycleOwner as the LifecycleOwner and the observer
        viewModel.recognitions.observe(requireActivity(), listObserver)

    }

    override fun onResume() {
        super.onResume()
        subscribeToViewModel()
    }

    private fun createEventListener() : RecognitionEventListener {

        return RecognitionEventListener(

            editClickListener = { id ->
                viewModel.selectRecognition(id)
            },

            sendClickListener = { id ->

                viewModel.sendRecognition(id) { isSuccess ->

                    val currentContext = context
                    val currentView = view
                    currentContext ?: return@sendRecognition
                    currentView ?: return@sendRecognition

                    when (isSuccess) {
                        true -> {
                            AppSnackBarBuilder.buildSuccessSnackBar(
                                currentContext,
                                currentView, getString(R.string.report_sent),
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                        else -> {
                            AppSnackBarBuilder.buildAlertSnackBar(
                                currentContext,
                                currentView, getString(R.string.report_sending_failed),
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                    }

                }

            },

            deleteClickListener = { id ->

                viewModel.deleteRecognition(id){isSuccess ->

                    val currentContext = context
                    val currentView = view
                    currentContext ?: return@deleteRecognition
                    currentView ?: return@deleteRecognition

                    AppSnackBarBuilder.buildInfoSnackBar(
                        currentContext,
                        currentView, getString(R.string.report_deleted),
                        Snackbar.LENGTH_SHORT
                    ).show()

                }

            }

        )

    }

}