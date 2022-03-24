package fosster.family.memeboard

import android.util.Log
import fosster.family.memeboard.datamodels.Memelist
import fosster.family.memeboard.service.MemeService
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.Interceptor

import okhttp3.OkHttpClient


import okhttp3.logging.HttpLoggingInterceptor





class MemesRetriever {
    private val service: MemeService

    companion object {
        //1
        const val BASE_URL = "https://api-memeboard.herokuapp.com/api/"
    }

    init {
        val interceptor = HttpLoggingInterceptor()
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        val client: OkHttpClient = OkHttpClient.Builder().addInterceptor(interceptor).build()

        // 2
        val retrofit = Retrofit.Builder()
            // 1
            .baseUrl(BASE_URL)
            //3
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

            // `githubToken`: Access token for GitHub


        service = retrofit.create(MemeService::class.java)
    }

    fun getMemes(callback: Callback<Memelist>) { //5
        val call = service.getALlMemes()
        Log.i("12447 Meme", "getMemes: ")
        call.enqueue(callback)
    }
}
