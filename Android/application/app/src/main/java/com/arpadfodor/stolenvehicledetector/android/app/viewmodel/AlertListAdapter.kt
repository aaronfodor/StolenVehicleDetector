package com.arpadfodor.stolenvehicledetector.android.app.viewmodel

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.*
import com.arpadfodor.stolenvehicledetector.android.app.R

class AlertListAdapter(context: Context, ids: Array<String>, date: String, location: String)
    : Adapter<AlertListAdapter.AlertViewHolder>() {

    var list = arrayListOf<AlertListElement>()
    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private val context = context

    init {
        for(i in ids.indices){
            this.list.add(AlertListElement(ids[i], date, location, true))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertViewHolder {
        val elementView = inflater.inflate(R.layout.report_item, parent, false)
        return AlertViewHolder(elementView)
    }

    override fun onBindViewHolder(holder: AlertViewHolder, position: Int) {

        val data = list[position]

        holder.tvId.text = data.licenseId
        holder.tvDate.text = data.time
        holder.tvLocation.text = data.location

        holder.tvActionButton.setOnClickListener {
            val currentIndex = list.indexOf(data)
            list.remove(data)
            this.notifyItemRemoved(currentIndex)
        }

        appearAnimation(holder.itemView)

    }

    private fun appearAnimation(viewToAnimate: View) {
        val animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left)
        viewToAnimate.startAnimation(animation)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    /**
     * Nested class to hold the element's view
     */
    class AlertViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val tvId: TextView = itemView.findViewById(R.id.reportId)
        val tvDate: TextView = itemView.findViewById(R.id.reportDate)
        val tvLocation: TextView = itemView.findViewById(R.id.reportLocation)
        val tvActionButton: ImageButton = itemView.findViewById(R.id.report_action_button)
    }

    /**
     * Element data class
     */
    data class AlertListElement(
        val licenseId: String,
        val time: String,
        val location: String,
        var enabled: Boolean
    )

}