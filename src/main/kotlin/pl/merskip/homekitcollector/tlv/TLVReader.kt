package pl.merskip.homekitcollector.tlv

import pl.merskip.homekitcollector.toBigInteger
import java.math.BigInteger

class TLVReader(
        private val data: List<TLVData>
) {

    constructor(data: ByteArray)
            : this(TLVDecoder().decode(data))

    fun getByteArray(tag: Int) =
            getByteArrayOrNull(tag)
                    ?: throw NotFoundTagException(tag)

    fun getInt(tag: Int) =
            getIntOrNull(tag)
                    ?: throw NotFoundTagException(tag)

    fun getByteArrayOrNull(tag: Int): ByteArray? =
            data.find { it.tag == tag.toByte() }?.value

    fun getIntOrNull(tag: Int): BigInteger? =
            getByteArrayOrNull(tag)?.toBigInteger()

    class NotFoundTagException(tag: Int): Exception("Not found tag: $tag")
}
