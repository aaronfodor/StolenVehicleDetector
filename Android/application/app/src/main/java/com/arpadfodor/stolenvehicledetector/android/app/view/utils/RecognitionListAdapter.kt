package com.arpadfodor.stolenvehicledetector.android.app.view.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.arpadfodor.stolenvehicledetector.android.app.databinding.RecognitionItemBinding
import com.arpadfodor.stolenvehicledetector.android.app.viewmodel.utils.Recognition

class RecognitionListAdapter(context: Context, clickListener: RecognitionEventListener)
    : ListAdapter<Recognition, RecognitionListAdapter.RecognitionViewHolder>(
    RecognitionDiffCallback()
) {

    private val context = context
    private val clickListener = clickListener

    /**
     * Nested class to hold the element's view
     **/
    class RecognitionViewHolder private constructor(binding: RecognitionItemBinding) : RecyclerView.ViewHolder(binding.root){

        val binding = binding

        companion object{
            fun from(parent: ViewGroup): RecognitionViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RecognitionItemBinding.inflate(layoutInflater, parent, false)
                return RecognitionViewHolder(
                    binding
                )
            }
        }

        fun bind(item: Recognition, clickListener: RecognitionEventListener?) {
            binding.recognitionItem = item
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }

    }

    override fun onBindViewHolder(holder: RecognitionViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, clickListener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecognitionViewHolder {
        return RecognitionViewHolder.from(
            parent
        )
    }

    private fun appearAnimation(viewToAnimate: View) {
        val animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left)
        viewToAnimate.startAnimation(animation)
    }

    private fun disappearAnimation(viewToAnimate: View) {
        val animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_out_right)
        viewToAnimate.startAnimation(animation)
    }

}

class RecognitionDiffCallback : DiffUtil.ItemCallback<Recognition>() {

    override fun areItemsTheSame(oldItem: Recognition, newItem: Recognition): Boolean {
        return oldItem.licenseId == newItem.licenseId
    }

    override fun areContentsTheSame(oldItem: Recognition, newItem: Recognition): Boolean {
        return oldItem == newItem
    }

}

class RecognitionEventListener(
    val editClickListener: (id: Int) -> Unit,
    val sendClickListener: (id: Int) -> Unit,
    val deleteClickListener: (id: Int) -> Unit
){
    fun onEditClick(recognition: Recognition) = editClickListener(recognition.artificialId)
    fun onSendClick(recognition: Recognition) = sendClickListener(recognition.artificialId)
    fun onDeleteClick(recognition: Recognition) = deleteClickListener(recognition.artificialId)
}