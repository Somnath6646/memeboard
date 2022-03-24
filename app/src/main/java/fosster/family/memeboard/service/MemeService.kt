package fosster.family.memeboard.service

import fosster.family.memeboard.datamodels.Memelist
import fosster.family.memeboard.datamodels.searchresponse.SearchResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface MemeService {
    @GET("meme/all")
    fun getALlMemes(): Call<Memelist>

    @GET("meme/search")
//sample search
    fun searchRepositories(@Query("searchQuery" ) searchQuery: String): Call<SearchResponse>
}