package pl.merskip.hapsolution.hapclient.response

import com.google.gson.annotations.SerializedName

data class AccessoriesResponse(

        @SerializedName("accessories")
        val accessories: List<Accessory>
)