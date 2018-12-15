package pl.merskip.hapsolution.hapclient.tlv

class TLVEncoder {

    fun encode() = encode(emptyList())

    fun encode(data: TLVData) = encode(listOf(data))

    fun encode(data: List<TLVData>): ByteArray = data
            .map { it.toDataChunks() }
            .flatMap { it }
            .map { byteArrayOf(it.tag, it.length.toByte(), *it.value) }
            .flatMap { it.asIterable() }
            .toByteArray()
}