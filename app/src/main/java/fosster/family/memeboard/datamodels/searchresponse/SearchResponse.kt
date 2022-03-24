package fosster.family.memeboard.datamodels.searchresponse


import com.google.gson.annotations.SerializedName

data class SearchResponse(
    @SerializedName("memes")
    val memes: List<Meme>
)