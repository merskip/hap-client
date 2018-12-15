package pl.merskip.hapsolution.hapclient.tlv

data class TLVDataChunk(
        val tag: Byte,
        val value: ByteArray
) {

    init {
        if (value.size > 255)
            throw IllegalArgumentException("Max size for the value is 255")
    }

    val length: Int
        get() = value.size
}