package com.arpadfodor.stolenvehicledetector.android.app.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.arpadfodor.stolenvehicledetector.android.app.R
import com.arpadfodor.stolenvehicledetector.android.app.viewmodel.AlertViewModel
import com.arpadfodor.stolenvehicledetector.android.app.viewmodel.utils.Recognition
import kotlinx.android.synthetic.main.fragment_recognition_list.*

class RecognitionListFragment : Fragment(){

    companion object{
        const val TAG = "Recognition list fragment"
    }

    private val viewModel: AlertViewModel by activityViewModels()
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
        viewModel.alerts.observe(requireActivity(), listObserver)

    }

    override fun onResume() {
        super.onResume()
        subscribeToViewModel()
    }

    private fun createEventListener() : RecognitionEventListener{

        return RecognitionEventListener(

            editClickListener = { id ->
                viewModel.selectRecognition(id)
            },

            sendClickListener = { id ->
                viewModel.sendRecognition(id)
            },

            deleteClickListener = { id ->
                viewModel.deleteRecognition(id)
            }

        )

    }

}