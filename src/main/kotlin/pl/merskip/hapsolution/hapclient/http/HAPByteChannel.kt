package pl.merskip.hapsolution.hapclient.http

import org.apache.http.util.ByteArrayBuffer
import org.bouncycastle.util.Pack
import java.nio.ByteBuffer
import java.nio.channels.ByteChannel
import java.util.*

class HAPByteChannel(private val sessionKeys: SessionKeys, channel: ByteChannel): ByteChannelProxy(channel) {

    var writeCounter: Long = 0
    var readCounter: Long = 0

    private val decodedBuffer: Queue<Int> = ArrayDeque<Int>(1024)

    init {
        println("New channel - HAPByteChannel")
    }

    override fun write(src: ByteBuffer?): Int {
        return if (src != null) {

            val source = ByteArray(src.remaining())
            src.get(source)
            val encodedData = getEncodedBuffer(sessionKeys.controllerToAccessoryKey, source)

            super.write(ByteBuffer.wrap(encodedData))
            source.size
        } else {
            0
        }
    }

    override fun read(dst: ByteBuffer?): Int {
        var readBytes = 0
        while (dst!!.remaining() > 0) {
            val decodedByte = getDecodedNextByte(sessionKeys.accessoryToControllerKey)
            if (decodedByte != null) {
                readBytes += 1
                dst.put(decodedByte.toByte())
            }
            else {
                break
            }
        }
        return readBytes
    }


    private fun getEncodedBuffer(key: ByteArray, data: ByteArray): ByteArray {
        println("Size: ${data.size}")
        check(data.size <= 1024)
        val lengthBytes = Pack.shortToLittleEndian(data.size.toShort())
        val counterBytes = Pack.longToLittleEndian(writeCounter)
        writeCounter += 1

        val out = ByteArrayBuffer(2 + data.size)
        out.append(lengthBytes, 0, lengthBytes.size)

        val nonce = counterBytes
        print("Nonce: ${nonce.hexDescription}")

        val encrypted = Chacha20Poly1305(key)
                .encrypt(data, lengthBytes, nonce)
        out.append(encrypted, 0, encrypted.size)

        return out.toByteArray()
    }

    private fun getDecodedNextByte(key: ByteArray): Int? {
        if (decodedBuffer.isEmpty()) {
            val decoded = decode(key) ?: return null
            decodedBuffer.addAll(decoded)
        }

        return decodedBuffer.poll()
    }

    private fun decode(key: ByteArray): List<Int>? {
        val dataLength = readDataLength() ?: return null
        println("DataLength: $dataLength")

        val encryptedBytes = ByteBuffer.allocate(dataLength.toInt() + 16)
        super.read(encryptedBytes)

        val nonce = Pack.longToLittleEndian(readCounter)
        readCounter += 1

        val decrypted = Chacha20Poly1305(key)
                .verifyAndDecrypt(encryptedBytes.array(), Pack.shortToLittleEndian(dataLength), nonce)
                ?: throw Exception("Failed decrypt data")
        check(decrypted.size == dataLength.toInt())

        return decrypted.map { it.toInt() }
    }

    private fun readDataLength(): Short? {
        val buffer = ByteBuffer.allocate(2)
        val readBytes = super.read(buffer)
        if (readBytes < 2) return null

        val bytes = listOf(buffer[0], buffer[1]).toByteArray()
        return Pack.littleEndianToShort(bytes, 0)
    }
}