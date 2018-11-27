package pl.merskip.homekitcollector

import net.i2p.crypto.eddsa.spec.EdDSANamedCurveTable
import org.bouncycastle.crypto.digests.SHA512Digest
import org.bouncycastle.crypto.generators.HKDFBytesGenerator
import org.bouncycastle.crypto.params.HKDFParameters
import java.security.SecureRandom


fun main(args : Array<String>) {

    val homeKit = HomeKitClient()
    homeKit.pair("My Client Name", "031-45-154")

}

fun hkdfSHA512(inputKeyingMaterial: ByteArray, salt: ByteArray, info: ByteArray, size: Int): ByteArray {
    val generator = HKDFBytesGenerator(SHA512Digest())
    generator.init(HKDFParameters(
            inputKeyingMaterial,
            salt,
            info
    ))

    val bytes = ByteArray(size)
    generator.generateBytes(bytes, 0, size)

    return bytes
}

fun generateKey(): ByteArray {
    val sr = SecureRandom()
    val spec = EdDSANamedCurveTable.getByName("ed25519-sha-512")
    val seed = ByteArray(spec.curve.field.getb() / 8)
    sr.nextBytes(seed)
    return seed
}