package com.arpadfodor.stolenvehicledetector.android.app.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.arpadfodor.stolenvehicledetector.android.app.databinding.ReportItemBinding
import com.arpadfodor.stolenvehicledetector.android.app.viewmodel.utils.Report

class ReportListAdapter(context: Context, clickListener: ReportListener?)
    : ListAdapter<Report, ReportListAdapter.ReportViewHolder>(RecognitionDiffCallback()) {

    private val context = context
    private val clickListener = clickListener

    /**
     * Nested class to hold the element's view
     **/
    class ReportViewHolder private constructor(binding: ReportItemBinding) : RecyclerView.ViewHolder(binding.root){

        val binding = binding

        companion object{
            fun from(parent: ViewGroup): ReportViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ReportItemBinding.inflate(layoutInflater, parent, false)
                return ReportViewHolder(binding)
            }
        }

        fun bind(item: Report, clickListener: ReportListener?) {
            binding.report = item
            binding.executePendingBindings()
        }

    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, clickListener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        return ReportViewHolder.from(parent)
    }

    private fun appearAnimation(viewToAnimate: View) {
        val animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left)
        viewToAnimate.startAnimation(animation)
    }

}

class RecognitionDiffCallback : DiffUtil.ItemCallback<Report>() {

    override fun areItemsTheSame(oldItem: Report, newItem: Report): Boolean {
        return oldItem.licenseId == newItem.licenseId
    }

    override fun areContentsTheSame(oldItem: Report, newItem: Report): Boolean {
        return oldItem == newItem
    }

}

class ReportListener(val clickListener: (id: Int) -> Unit){
    fun onClick(recognition: Report) = clickListener(recognition.id)
}