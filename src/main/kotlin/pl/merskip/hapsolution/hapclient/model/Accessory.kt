package pl.merskip.hapsolution.hapclient.model

import com.google.gson.annotations.SerializedName
import java.util.*

data class Accessory(

        @SerializedName("aid")
        val id: Long,

        @SerializedName("services")
        val services: List<Service>
) {

        fun findService(service: ServiceIdentifier) = findService(service.uuid)

        fun findService(uuid: UUID): Service? = services.find { it.type == uuid }
}