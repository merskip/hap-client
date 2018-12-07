package pl.merskip.homekitcollector.crypto

import net.i2p.crypto.eddsa.EdDSAEngine
import net.i2p.crypto.eddsa.EdDSAPrivateKey
import net.i2p.crypto.eddsa.EdDSAPublicKey
import net.i2p.crypto.eddsa.KeyPairGenerator
import net.i2p.crypto.eddsa.spec.EdDSANamedCurveTable
import net.i2p.crypto.eddsa.spec.EdDSAPrivateKeySpec
import net.i2p.crypto.eddsa.spec.EdDSAPublicKeySpec
import java.security.MessageDigest
import java.security.SecureRandom

class EdDSA(
        val secretKey: EdDSAPrivateKey?,
        val publicKey: EdDSAPublicKey
) {

    fun sign(data: ByteArray): ByteArray {
        if (secretKey != null) {
            val engine = EdDSAEngine(MessageDigest.getInstance(spec.hashAlgorithm))
            engine.initSign(secretKey)
            return engine.signOneShot(data)
        } else {
            throw Exception("No secretKey")
        }
    }

    fun verify(data: ByteArray, signature: ByteArray): Boolean {
        val engine = EdDSAEngine(MessageDigest.getInstance(spec.hashAlgorithm))
        engine.initVerify(publicKey)
        return engine.verifyOneShot(data, signature)
    }

    companion object {

        private val spec = EdDSANamedCurveTable.getByName(EdDSANamedCurveTable.ED_25519)

        fun createWithRandomKeyPair(): EdDSA {
            val keyPair = EdDSA.generateKeyPair()
            return EdDSA(keyPair.first, keyPair.second)
        }

        fun createWithPublicKey(publicKey: ByteArray) =
                EdDSA(null, publicKey(publicKey))

        fun createWithKeys(secretKey: ByteArray, publicKey: ByteArray) =
                EdDSA(secretKey(secretKey), publicKey(publicKey))

        private fun publicKey(bytes: ByteArray) =
                EdDSAPublicKey(EdDSAPublicKeySpec(bytes, spec))

        private fun secretKey(bytes: ByteArray) =
                EdDSAPrivateKey(EdDSAPrivateKeySpec(spec, bytes))

        private fun generateKeyPair(): Pair<EdDSAPrivateKey, EdDSAPublicKey> {
            val generator = KeyPairGenerator()
            generator.initialize(spec, SecureRandom.getInstanceStrong())
            val keyPair = generator.generateKeyPair()

            return Pair(keyPair.private as EdDSAPrivateKey, keyPair.public as EdDSAPublicKey)
        }
    }

}