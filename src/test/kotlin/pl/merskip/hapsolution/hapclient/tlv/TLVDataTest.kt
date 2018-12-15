package pl.merskip.hapsolution.hapclient.tlv

import org.junit.Assert.assertEquals
import org.junit.Test

class TLVDataTest {

    @Test
    fun toDataChunks_sizeOfValueLessThan255() {
        val chunks = TLVData(0, byteArrayOf(2)).toDataChunks()

        assertEquals(1, chunks.size)
        assertEquals(1, chunks[0].length)
        assertEquals(2.toByte(), chunks[0].value[0])
    }

    @Test
    fun toDataChunks_largeValue() {
        val value = mutableListOf<Byte>()
        for (i in 0..254) value.add(1)
        for (i in 0..9) value.add(2)

        val chunks = TLVData(0, value.toByteArray()).toDataChunks()

        assertEquals(2, chunks.size)
        assertEquals(255, chunks[0].length)
        for (i in 0..254) assertEquals(1.toByte(), chunks[0].value[i])
        assertEquals(10, chunks[1].length)
        for (i in 0..9) assertEquals(2.toByte(), chunks[1].value[i])
    }
}