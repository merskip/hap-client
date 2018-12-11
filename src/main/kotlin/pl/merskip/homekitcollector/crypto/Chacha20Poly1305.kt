package pl.merskip.homekitcollector.crypto

import org.bouncycastle.crypto.macs.Poly1305
import org.bouncycastle.crypto.params.KeyParameter
import org.bouncycastle.util.Pack

class Chacha20Poly1305(private val key: ByteArray) {

    companion object {
        const val POLY_KEY_SIZE = 32
        const val POLY_OUT_BYTES = 16
    }

    fun encrypt(message: ByteArray, additionalData: ByteArray = ByteArray(0), nonce: String) =
            encrypt(message, additionalData, nonce.toByteArray())

    fun encrypt(message: ByteArray, additionalData: ByteArray = ByteArray(0), nonce: ByteArray): ByteArray {
        val cipherText = ByteArray(message.size)
        Sodium.cryptoStreamChacha20XorIc(cipherText, message, message.size.toLong(), nonce, 1, key)

        val authTag = computePoly1305(cipherText, additionalData, nonce)
        return cipherText + authTag
    }

    fun verifyAndDecrypt(cipherTextWithAuthTag: ByteArray, additionalData: ByteArray = ByteArray(0), nonce: String): ByteArray? =
            verifyAndDecrypt(cipherTextWithAuthTag, additionalData, nonce.toByteArray())

    fun verifyAndDecrypt(cipherTextWithAuthTag: ByteArray, additionalData: ByteArray = ByteArray(0), nonce: ByteArray): ByteArray? =
            verifyAndDecrypt(
                    cipherTextWithAuthTag.sliceArray(0 until cipherTextWithAuthTag.size - 16),
                    cipherTextWithAuthTag.sliceArray(cipherTextWithAuthTag.size - 16 until cipherTextWithAuthTag.size),
                    additionalData,
                    nonce
            )

    fun verifyAndDecrypt(cipherText: ByteArray, authTag: ByteArray, additionalData: ByteArray = ByteArray(0), nonce: ByteArray): ByteArray? {
        val calculatedAuthTag = computePoly1305(cipherText, additionalData, nonce)
        if (!calculatedAuthTag.contentEquals(authTag))
            return null

        val message = ByteArray(cipherText.size)
        val res = Sodium.cryptoStreamChacha20XorIc(message, cipherText, cipherText.size.toLong(), nonce, 1, key)
        if (!res) return null

        return message
    }

    private fun computePoly1305(cipherText: ByteArray, additionalData: ByteArray, nonce: ByteArray): ByteArray {

        val msg = listOf(
                additionalData,
                padding16(additionalData.size),
                cipherText,
                padding16(cipherText.size),
                Pack.longToLittleEndian(additionalData.size.toLong()),
                Pack.longToLittleEndian(cipherText.size.toLong())
        ).flatMap { it.asIterable() }.toByteArray()

        val polyKey = ByteArray(POLY_KEY_SIZE)
        Sodium.cryptoStreamChaCha20(polyKey, polyKey.size.toLong(), nonce, key)

        val poly1305 = Poly1305()
        poly1305.init(KeyParameter(polyKey))
        poly1305.update(msg, 0, msg.size)

        val mac = ByteArray(POLY_OUT_BYTES)
        poly1305.doFinal(mac, 0)
        return mac
    }

    private fun padding16(size: Int): ByteArray =
            if (size % 16 == 0) ByteArray(0)
            else ByteArray(16 - (size % 16))


}