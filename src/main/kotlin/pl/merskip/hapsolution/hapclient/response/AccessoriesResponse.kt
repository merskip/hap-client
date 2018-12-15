package pl.merskip.hapsolution.hapclient.response

import com.google.gson.annotations.SerializedName
import pl.merskip.hapsolution.hapclient.model.Accessory

data class AccessoriesResponse(

        @SerializedName("accessories")
        val accessories: List<Accessory>
)