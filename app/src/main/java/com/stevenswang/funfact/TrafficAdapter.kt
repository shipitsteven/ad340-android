package com.stevenswang.funfact

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONObject

class TrafficAdapter : RecyclerView.Adapter<TrafficAdapter.TrafficViewHolder>() {

    private lateinit var response: JSONObject

    inner class TrafficViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.text_traffic_title)
        val cam: ImageView = itemView.findViewById(R.id.image_traffic)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrafficViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.traffic_item, parent, false)
        return TrafficViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TrafficViewHolder, position: Int) {
        holder.title.text = "Testing title"
    }

    override fun getItemCount(): Int {
        return 5;
    }
}