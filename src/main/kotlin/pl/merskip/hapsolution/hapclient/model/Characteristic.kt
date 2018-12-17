package pl.merskip.hapsolution.hapclient.model

import com.google.gson.annotations.SerializedName
import java.util.*

data class Characteristic(

        @SerializedName("iid")
        val id: Long,

        @SerializedName("type")
        val type: UUID,

        @SerializedName("perms")
        val permissions: List<String>,

        /**
         * Characteristic Value Formats:
         *  - bool
         *  - uint8
         *  - uint16
         *  - uint32
         *  - uint64
         *  - int
         *  - float
         *  - string
         *  - tlv8
         *  - data
         */
        @SerializedName("format")
        val format: String,

        @SerializedName("value")
        val value: Any?,

        @SerializedName("description")
        val description: String,

        /**
         * Characteristic Units
         *  - celsius
         *  - percentage
         *  - arcdegrees
         *  - lux
         *  - seconds
         */
        @SerializedName("unit")
        val unit: String?,

        @SerializedName("maxValue")
        val maxValue: Double?,

        @SerializedName("minValue")
        val minValue: Double?,

        @SerializedName("minStep")
        val minStep: Double?
)
