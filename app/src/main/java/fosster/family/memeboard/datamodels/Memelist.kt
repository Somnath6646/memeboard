package fosster.family.memeboard.datamodels


import com.google.gson.annotations.SerializedName

data class Memelist(
    @SerializedName("posts")
    val posts: List<Post>
)