package pl.merskip.hapsolution.hapclient.crypto

import org.bouncycastle.crypto.digests.SHA512Digest
import org.bouncycastle.crypto.generators.HKDFBytesGenerator
import org.bouncycastle.crypto.params.HKDFParameters
import pl.merskip.hapsolution.hapclient.rawByteArray
import java.math.BigInteger

object HKDFSHA512 {


    fun encrypt(inputKeyingMaterial: BigInteger, salt: String, info: String, size: Int = 32) =
            encrypt(inputKeyingMaterial.rawByteArray, salt.toByteArray(), info.toByteArray(), size)

    fun encrypt(inputKeyingMaterial: ByteArray, salt: String, info: String, size: Int = 32) =
            encrypt(inputKeyingMaterial, salt.toByteArray(), info.toByteArray(), size)

    fun encrypt(inputKeyingMaterial: ByteArray, salt: ByteArray, info: ByteArray, size: Int): ByteArray {

        val parameters = HKDFParameters(inputKeyingMaterial, salt, info)
        val generator = HKDFBytesGenerator(SHA512Digest())
        generator.init(parameters)

        val bytes = ByteArray(size)
        generator.generateBytes(bytes, 0, size)
        return bytes
    }
}