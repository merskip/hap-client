package pl.merskip.hapsolution.hapclient.tlv

data class TLVData(
        val tag: Byte,
        val value: ByteArray
) {

    val length: Int
        get() = value.size

    fun toDataChunks(): List<TLVDataChunk> =
            value.toList().chunked(255).map { TLVDataChunk(tag, it.toByteArray()) }
}