package pl.merskip.homekitcollector.model

import com.google.gson.annotations.SerializedName
import java.util.*

data class Service(

        @SerializedName("iid")
        val id: Long,

        @SerializedName("type")
        val type: UUID,

        @SerializedName("characteristics")
        val characteristics: List<Characteristic>,

        @SerializedName("primary")
        val isPrimary: Boolean,

        @SerializedName("hidden")
        val isHidden: Boolean
)
