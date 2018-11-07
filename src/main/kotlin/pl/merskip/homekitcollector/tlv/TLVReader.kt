package pl.merskip.homekitcollector.tlv

class TLVReader(
        val data: List<TLVData>
) {

    constructor(data: ByteArray)
            : this(TLVDecoder().decode(data))

    fun get(tag: Int): ByteArray? =
            data.find { it.tag == tag.toByte() }?.value
}
