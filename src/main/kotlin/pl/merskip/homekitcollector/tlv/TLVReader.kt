package pl.merskip.homekitcollector.tlv

import pl.merskip.homekitcollector.toBigInteger

class TLVReader(
        val data: List<TLVData>
) {

    constructor(data: ByteArray)
            : this(TLVDecoder().decode(data))

    fun get(tag: Int): ByteArray? =
            data.find { it.tag == tag.toByte() }?.value

    fun getInt(tag: Int): Int? =
            get(tag)?.toBigInteger()?.toInt()
}
