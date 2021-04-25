package com.stevenswang.funfact

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class MoviesAdapter(private val moviesList: Array<Array<String>>) : RecyclerView.Adapter<MoviesAdapter.MoviesViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoviesViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.movie_item, parent, false)

        return MoviesViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MoviesViewHolder, position: Int) {
        val currentItem = moviesList[position]

        holder.title.text = currentItem[0]
        holder.year.text = currentItem[1]
//        Picasso.get().setLoggingEnabled(true)
//        Picasso.get().setIndicatorsEnabled(true)
        Picasso.get().load(currentItem[3]).into(holder.image)
        holder.image.contentDescription = currentItem[0]
    }

    override fun getItemCount() = moviesList.size

    class MoviesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.text_movie_title)
        val year: TextView = itemView.findViewById(R.id.text_movie_year)
        val image: ImageView = itemView.findViewById(R.id.image_movie)
    }
}