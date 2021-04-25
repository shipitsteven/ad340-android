package com.stevenswang.funfact

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class Movies : AppCompatActivity(), MoviesAdapter.OnItemClickListener {

    private lateinit var moviesList : Array<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movies)
        moviesList = MoviesData().listOfMovies
        val recyclerMovies = findViewById<RecyclerView>(R.id.recycler_movies)

        recyclerMovies.adapter = MoviesAdapter(moviesList, this)
        recyclerMovies.adapter?.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        recyclerMovies.layoutManager = LinearLayoutManager(this)
        recyclerMovies.setHasFixedSize(true)


    }

    override fun onItemClick(position: Int) {
        val clickedMovie = moviesList[position]
        val intent = Intent(this, MovieDetail::class.java)
        intent.putExtra("movieInfo", clickedMovie)
        startActivity(intent)
        onPause()
    }
}