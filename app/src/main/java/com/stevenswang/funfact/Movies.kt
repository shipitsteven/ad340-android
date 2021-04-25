package com.stevenswang.funfact

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.stevenswang.funfact.databinding.ActivityMoviesBinding

class Movies : AppCompatActivity() {

    private lateinit var binding: ActivityMoviesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMoviesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val moviesList = MoviesData().listOfMovies
        val recyclerMovies = findViewById<RecyclerView>(R.id.recycler_movies)

        recyclerMovies.adapter = MoviesAdapter(moviesList)
        recyclerMovies.layoutManager = LinearLayoutManager(this)
        recyclerMovies.setHasFixedSize(true)

    }
}