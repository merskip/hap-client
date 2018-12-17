package pl.merskip.hapsolution.hapclient.model

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
) {

        fun findCharacteristic(characteristic: CharacteristicIdentifier) = findCharacteristic(characteristic.uuid)

        fun findCharacteristic(uuid: UUID): Characteristic? = characteristics.find { it.type == uuid }
}
