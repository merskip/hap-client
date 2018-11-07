package pl.merskip.homekitcollector.tlv

class TLVBuilder {

    private val dataList = mutableListOf<TLVData>()

    fun append(tag: Int, data: ByteArray): TLVBuilder {
        dataList.add(TLVData(tag.toByte(), data))
        return this
    }

    fun append(tag: Int, text: String): TLVBuilder {
        dataList.add(TLVData(tag.toByte(), text.toByteArray(Charsets.ISO_8859_1)))
        return this
    }

    fun append(tag: Int, byte: Byte): TLVBuilder {
        val data = ByteArray(1) { byte }
        dataList.add(TLVData(tag.toByte(), data))
        return this
    }

    fun build(): ByteArray = TLVEncoder().encode(dataList)
}