package pl.merskip.homekitcollector.model

import com.google.gson.annotations.SerializedName

data class Accessory(

        @SerializedName("aid")
        val id: Long,

        @SerializedName("services")
        val services: List<Service>

)