package fosster.family.memeboard.datamodels


import com.google.gson.annotations.SerializedName

data class Post(
    @SerializedName("_id")
    val id: String,
    @SerializedName("memeUrl")
    val memeUrl: String
)