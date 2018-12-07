package pl.merskip.homekitcollector.crypto

import com.goterl.lazycode.lazysodium.utils.Key
import com.goterl.lazycode.lazysodium.utils.KeyPair


class DiffieHellman(private val keyPair: KeyPair) {

    val publicKey: Key
        get() = keyPair.publicKey

    fun calculateSharedKey(publicKey: Key): Key = Sodium.cryptoScalarMult(keyPair.secretKey, publicKey)

    companion object {

        private const val CURVE25519_BYTES = 32

        fun generateWithRandomCurve25519Key(): DiffieHellman =
                DiffieHellman(generatePairKeyWithCurve25519())

        fun generatePairKeyWithCurve25519(): KeyPair {
            val secretKey = Key.generate(Sodium, CURVE25519_BYTES)
            val publicKey = Sodium.cryptoScalarMultBase(secretKey)
            return KeyPair(publicKey, secretKey)
        }
    }
}