package pl.merskip.hapsolution.hapclient

import net.i2p.crypto.eddsa.EdDSAEngine
import net.i2p.crypto.eddsa.EdDSAPrivateKey
import net.i2p.crypto.eddsa.EdDSAPublicKey
import net.i2p.crypto.eddsa.spec.EdDSANamedCurveTable
import net.i2p.crypto.eddsa.spec.EdDSAPrivateKeySpec
import net.i2p.crypto.eddsa.spec.EdDSAPublicKeySpec
import java.security.MessageDigest


class EdsaSigner(privateKeyBytes: ByteArray) {

    private val publicKey: EdDSAPublicKey
    private val privateKey: EdDSAPrivateKey

    init {
        val spec = EdDSANamedCurveTable.getByName("ed25519-sha-512")
        val privateKeySpec = EdDSAPrivateKeySpec(privateKeyBytes, spec)
        val pubKeySpec = EdDSAPublicKeySpec(privateKeySpec.a, spec)
        publicKey = EdDSAPublicKey(pubKeySpec)
        privateKey = EdDSAPrivateKey(privateKeySpec)
    }

    fun getPublicKey(): ByteArray {
        return publicKey.abyte
    }


    fun sign(material: ByteArray): ByteArray {
        val sgr = EdDSAEngine(MessageDigest.getInstance("SHA-512"))
        sgr.initSign(privateKey)
        sgr.update(material)
        return sgr.sign()
    }

}