package pl.merskip.hapsolution.hapclient.archive

import com.google.gson.*
import java.lang.reflect.Type
import java.util.*

class GsonByteArrayTypeAdapter : JsonSerializer<ByteArray>, JsonDeserializer<ByteArray> {

    override fun serialize(src: ByteArray?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return JsonPrimitive(Base64.getEncoder().withoutPadding().encodeToString(src))
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): ByteArray {
        return Base64.getDecoder().decode(json?.asString)
    }
}