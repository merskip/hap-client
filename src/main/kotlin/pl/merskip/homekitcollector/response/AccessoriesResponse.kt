package pl.merskip.homekitcollector.response

import com.google.gson.annotations.SerializedName
import pl.merskip.homekitcollector.model.Accessory

data class AccessoriesResponse(

        @SerializedName("accessories")
        val accessories: List<Accessory>
)