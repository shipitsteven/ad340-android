package com.stevenswang.funfact

import com.stevenswang.funfact.model.Camera
import retrofit2.Call
import retrofit2.http.GET

interface ApiRequest {

    @GET("Data?zoomId=13&type=2")
    fun getCamData(): Call<Camera>
}