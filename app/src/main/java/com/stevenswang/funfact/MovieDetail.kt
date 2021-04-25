package com.stevenswang.funfact

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.Picasso


class MovieDetail : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie_detail)

        val info = intent.extras?.getStringArray("movieInfo")

        val image = findViewById<ImageView>(R.id.image_detail_image)

        findViewById<TextView>(R.id.text_detail_title).text = info?.get(0)
        findViewById<TextView>(R.id.text_detail_year).text = info?.get(1)
//        Picasso.get().isLoggingEnabled = true
        Picasso.get().load(info?.get(3)).into(image)
        image.contentDescription = info?.get(0)
        findViewById<TextView>(R.id.text_detail_description).text = info?.get(4)

        supportActionBar?.title = info?.get(0)
    }
}