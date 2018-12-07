package pl.merskip.homekitcollector

import java.math.BigInteger

var ByteArray.hexDescription: String
    get() {
        return joinToString(separator = "", prefix = "[ ", postfix = " ]") { byte ->
            String.format("%02x", (byte.toInt() and 0xff))
        }
    }
    set(_) {
        error("This property is read-only")
    }

fun ByteArray.toBigInteger(): BigInteger {
    return if (this[0] < 0) {
        val bytesWithZeroLeading = ByteArray(size + 1)
        bytesWithZeroLeading[0] = 0 // Make sure that first byte is zero
        System.arraycopy(this, 0, bytesWithZeroLeading, 1, size)
        BigInteger(bytesWithZeroLeading)
    } else {
        BigInteger(this)
    }
}

fun fromHex(hex: String): BigInteger = BigInteger(hex.filter { it.isLetterOrDigit() }, 16)

fun hex2bytes(hex: String): ByteArray = ByteArray(hex.length / 2) { hex.substring(it * 2, it * 2 + 2).toInt(16).toByte() }

var BigInteger.rawByteArray: ByteArray
    get() {
        val bytes = toByteArray()

        return if (bytes[0] != 0.toByte()) {
            bytes
        } else {
            val rawBytes = ByteArray(bytes.size - 1)
            System.arraycopy(bytes, 1, rawBytes, 0, rawBytes.size)
            rawBytes
        }
    }
    set(_) {
        error("This property is read-only")
    }
