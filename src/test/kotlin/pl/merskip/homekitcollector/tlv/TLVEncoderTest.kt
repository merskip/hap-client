package pl.merskip.homekitcollector.tlv

import org.junit.Assert
import org.junit.Test
import kotlin.test.assertEquals

class TLVEncoderTest {

    private var encoder = TLVEncoder()

    @Test
    fun encode_noMessage() {
        val output = encoder.encode()

        assert(output.isEmpty())
    }

    @Test
    fun encode_oneByteValue() {
        val output = encoder.encode(TLVData(0, byteArrayOf(2)))

        assertEquals(3, output.size)
        assertEquals(0, output[0]) // Tag
        assertEquals(1, output[1]) // Length of value
        assertEquals(2, output[2]) // Value
    }

    @Test
    fun encode_largeValue() {
        val value = mutableListOf<Byte>()
        for (i in 0..254) value.add(1)
        for (i in 0..9) value.add(2)
        val output = encoder.encode(TLVData(0, value.toByteArray()))

        assertEquals(257 + 12, output.size)

        assertEquals(0, output[0]) // Tag
        assertEquals(255.toByte(), output[1]) // Length of value of first chunk
        for (i in 2..256) Assert.assertEquals(1.toByte(), output[i]) // Check values in first chunk

        assertEquals(0, output[257]) // Tag
        assertEquals(10.toByte(), output[258]) // Length of value of second chunk
        for (i in 259..268) Assert.assertEquals(2.toByte(), output[i]) // Check values in second chunk
    }

}
