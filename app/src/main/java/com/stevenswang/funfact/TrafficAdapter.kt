package com.stevenswang.funfact

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.stevenswang.funfact.model.Camera

const val IMAGE_BASE_URL = "https://www.seattle.gov/trafficcams/images/"

class TrafficAdapter(private val apiResponse: Camera) :
    RecyclerView.Adapter<TrafficAdapter.TrafficViewHolder>() {

    inner class TrafficViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.text_traffic_title)
        val camImage: ImageView = itemView.findViewById(R.id.image_traffic)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrafficViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.traffic_item, parent, false)
        return TrafficViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TrafficViewHolder, position: Int) {
        val fullImageUrl = IMAGE_BASE_URL + apiResponse.Features[position].Cameras[0].ImageUrl

        // Only shows the first camera if there are multiple cams at one location
        holder.title.text = apiResponse.Features[position].Cameras[0].Description
        Picasso.get().load(fullImageUrl).into(holder.camImage)
        holder.camImage.contentDescription = apiResponse.Features[position].Cameras[0].Description
    }

    override fun getItemCount(): Int {
        return apiResponse.Features.size;
    }
}