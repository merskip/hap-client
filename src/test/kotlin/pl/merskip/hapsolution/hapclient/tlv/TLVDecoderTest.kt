package pl.merskip.hapsolution.hapclient.tlv

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.*

class TLVDecoderTest {

    private var decoder = TLVDecoder()

    @Test
    fun decode_emptyData() {
        val data = decoder.decode(byteArrayOf())

        assertEquals(0, data.size)
    }

    @Test
    fun decode_oneData() {
        val data = decoder.decode(byteArrayOf(0, 1, 2))

        assertEquals(1, data.size)
        assertEquals(0.toByte(), data[0].tag)
        assertEquals(1, data[0].length)
        assertEquals(2.toByte(), data[0].value[0])
    }

    @Test
    fun decode_twoData() {
        val data = decoder.decode(byteArrayOf(
                0, 1, 2,
                1, 2, 3, 4
        ))

        assertEquals(2, data.size)

        assertEquals(0.toByte(), data[0].tag)
        assertEquals(1, data[0].length)
        assertEquals(2.toByte(), data[0].value[0])

        assertEquals(1.toByte(), data[1].tag)
        assertEquals(2, data[1].length)
        assertEquals(3.toByte(), data[1].value[0])
        assertEquals(4.toByte(), data[1].value[1])
    }

    @Test
    fun decode_largeData() {
        val value = ByteArray(269)

        value[0] = 0 // Tag
        value[1] = 255.toByte() // Length of first chunk
        Arrays.fill(value, 2, 257, 2)

        value[257] = 0 // The same tag
        value[258] = 10 // Length of second chunk
        Arrays.fill(value, 259, 269, 3)

        val data = decoder.decode(value)

        assertEquals(1, data.size)

        assertEquals(0.toByte(), data[0].tag)
        assertEquals(265, data[0].length)
        for (i in 0..254) assertEquals(2.toByte(), data[0].value[i]) // Check first chunk
        for (i in 255..264) assertEquals(3.toByte(), data[0].value[i]) // Check second chunk
    }
}