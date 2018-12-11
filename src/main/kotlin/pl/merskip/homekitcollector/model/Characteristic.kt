package pl.merskip.homekitcollector.model

import com.google.gson.annotations.SerializedName
import java.util.*

data class Characteristic(

        @SerializedName("iid")
        val id: Long,

        @SerializedName("type")
        val type: UUID,

        @SerializedName("perms")
        val permissions: List<String>,

        @SerializedName("format")
        val format: String,

        @SerializedName("value")
        val value: Any?,

        @SerializedName("description")
        val description: String,

        @SerializedName("unit")
        val unit: String?,

        @SerializedName("maxValue")
        val maxValue: Double?,

        @SerializedName("minValue")
        val minValue: Double?,

        @SerializedName("minStep")
        val minStep: Double?
)