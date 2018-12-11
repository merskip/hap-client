package pl.merskip.homekitcollector.http

import org.apache.http.util.ByteArrayBuffer
import org.bouncycastle.util.Pack
import pl.merskip.homekitcollector.crypto.Chacha20Poly1305
import pl.merskip.homekitcollector.hexDescription
import pl.merskip.homekitcollector.loggerFor
import java.io.OutputStream
import java.util.*

class SecuredOutputStream(private val outputStream: OutputStream): OutputStream() {

    private val logger = loggerFor(javaClass)

    var encryptKey: ByteArray? = null
    private var counter: Long = 0

    private val buffer = ArrayDeque<Byte>(1024)

    override fun write(b: Int) {
        buffer.add(b.toByte())
    }

    override fun flush() {
        if (buffer.isEmpty()) return
        logger.info("Flush")

        val buffer = getEncryptedBufferIfNeeded()
        outputStream.write(buffer)
        outputStream.flush()

        this.buffer.clear()
    }

    override fun close() {
        flush()
        outputStream.close()
    }

    private fun getEncryptedBufferIfNeeded(): ByteArray {
        val encryptKey = encryptKey
        return if (encryptKey != null)
            getEncodedBuffer(encryptKey, buffer.toByteArray())
        else
            buffer.toByteArray()
    }

    private fun getEncodedBuffer(key: ByteArray, data: ByteArray): ByteArray {
        println("Size: ${buffer.size}")
        check(buffer.size <= 1024)
        val lengthBytes = Pack.shortToLittleEndian(data.size.toShort())
        val counterBytes = Pack.longToLittleEndian(counter)
        counter += 1

        val out = ByteArrayBuffer(2 + data.size)
        out.append(lengthBytes, 0, lengthBytes.size)

        val nonce = counterBytes
        print("Nonce: ${nonce.hexDescription}")

        val encrypted = Chacha20Poly1305(key)
                .encrypt(data, lengthBytes, nonce)
        out.append(encrypted, 0, encrypted.size)

        return out.toByteArray()
    }
}