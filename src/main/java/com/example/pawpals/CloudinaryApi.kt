package com.example.pawpals.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface CloudinaryApi {

    @Multipart
    @POST("v1_1/{cloud_name}/image/upload")
    fun uploadImage(
        @Path("cloud_name") cloudName: String, // <== ici c'est le nom du champ dans l’URL
        @Part image: MultipartBody.Part,
        @Part("upload_preset") uploadPreset: RequestBody
    ): Call<CloudinaryResponse>
}
