package fosster.family.memeboard.datamodels.searchresponse


import com.google.gson.annotations.SerializedName

data class Meme(
    @SerializedName("_id")
    val id: String,
    @SerializedName("memeUrl")
    val memeUrl: String
)