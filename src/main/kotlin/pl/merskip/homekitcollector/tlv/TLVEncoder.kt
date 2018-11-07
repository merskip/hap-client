package pl.merskip.homekitcollector.tlv

class TLVEncoder {

    fun encode(vararg data: TLVData): ByteArray = data
            .map { it.toDataChunks() }
            .flatMap { it }
            .map { byteArrayOf(it.tag, it.length.toByte(), *it.value) }
            .flatMap { it.asIterable() }
            .toByteArray()
}