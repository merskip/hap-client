package pl.merskip.homekitcollector.http

import org.bouncycastle.util.Pack
import pl.merskip.homekitcollector.crypto.Chacha20Poly1305
import pl.merskip.homekitcollector.loggerFor
import java.io.InputStream
import java.util.*

class SecuredInputStream(private val inputStream: InputStream) : InputStream() {

    private val logger = loggerFor(javaClass)

    var decryptionKey: ByteArray? = null
    private val buffer: Queue<Int> = ArrayDeque<Int>(1024)
    var counter: Long = 0

    override fun read(): Int {
        return getNextByte()
    }

    //    override fun read(b: ByteArray?): Int = read(b, 0, b?.size ?: 0)
//
    override fun read(b: ByteArray?, off: Int, len: Int): Int {
//        logger.info("ReadByte to ByteArray, off, len")
//        return inputStream.read(b, off, len)
        var index = off
        while (true) {
            val byte = getNextByte()
//            println("Read: $byte ${byte.toChar()}")
            if (byte != -1) {
                b?.set(index, byte.toByte())
                index++
            } else {
//                println("Break")
                break
            }
        }
        return index - off
    }

    override fun close() = inputStream.close()

    override fun available() = inputStream.available()

    private fun getNextByte(): Int {
        val decryptionKey = decryptionKey
        return when {
            decryptionKey != null -> getDecodedNextByte(decryptionKey) ?: -1
            inputStream.available() > 0 -> inputStream.read()
            else -> -1
        }
    }

    private fun getDecodedNextByte(key: ByteArray): Int? {
        if (buffer.isEmpty()) {
            val decoded = decode(key) ?: return null
            buffer.addAll(decoded)
        }

        return buffer.poll()
    }

    private fun decode(key: ByteArray): List<Int>? {
        val dataLength = readDataLength() ?: return null
        println("DataLength: $dataLength")

        val encryptedBytes = ByteArray(dataLength.toInt() + 16)
        inputStream.read(encryptedBytes)

        val nonce = Pack.longToLittleEndian(counter)
        counter += 1

        val decrypted = Chacha20Poly1305(key)
                .verifyAndDecrypt(encryptedBytes, Pack.shortToLittleEndian(dataLength), nonce)
                ?: throw Exception("Failed decrypt data")
        check(decrypted.size == dataLength.toInt())

        return decrypted.map { it.toInt() }
    }

    private fun readDataLength(): Short? {
        if (inputStream.available() < 2) return null

        val firstByte = inputStream.read()
        val secondByte = inputStream.read()
        val bytes = listOf(firstByte.toByte(), secondByte.toByte()).toByteArray()

        return Pack.littleEndianToShort(bytes, 0)
    }
}