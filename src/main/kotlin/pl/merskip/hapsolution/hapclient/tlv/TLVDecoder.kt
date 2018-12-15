package pl.merskip.hapsolution.hapclient.tlv

import pl.merskip.hapsolution.hapclient.hexDescription
import pl.merskip.hapsolution.hapclient.loggerFor

class TLVDecoder {

    private val logger = loggerFor(javaClass)

    fun decode(data: ByteArray): List<TLVData> {

        val chunks = mutableListOf<TLVDataChunk>()
        val iterator = data.iterator()

        logger.trace("Decoding data: ${data.hexDescription}")

        while (iterator.hasNext()) {
            val tag = iterator.nextByte()
            val length = iterator.next().toInt() and 0xff

            logger.trace("Found chunk (tag=$tag, length=$length)")

            val value = ByteArray(length)
            for (i in 0 until length)
                value[i] = iterator.next()

            chunks.add(TLVDataChunk(tag, value))
        }

        return chunks.groupBy { it.tag }
                .mapValues { (_, values) ->
                    values.map { it.value }.flatMap { it.asIterable() }.toByteArray()
                }
                .map { return@map TLVData(it.key, it.value) }
    }
}