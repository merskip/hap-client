package pl.merskip.homekitcollector

import com.goterl.lazycode.lazysodium.LazySodium
import com.goterl.lazycode.lazysodium.LazySodiumJava
import com.goterl.lazycode.lazysodium.SodiumJava
import net.i2p.crypto.eddsa.EdDSAEngine
import net.i2p.crypto.eddsa.EdDSAPrivateKey
import net.i2p.crypto.eddsa.EdDSAPublicKey
import net.i2p.crypto.eddsa.spec.EdDSANamedCurveSpec
import net.i2p.crypto.eddsa.spec.EdDSANamedCurveTable
import net.i2p.crypto.eddsa.spec.EdDSAPrivateKeySpec
import net.i2p.crypto.eddsa.spec.EdDSAPublicKeySpec
import org.apache.commons.io.IOUtils
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ByteArrayEntity
import org.apache.http.impl.client.HttpClientBuilder
import org.bouncycastle.crypto.digests.SHA512Digest
import org.bouncycastle.crypto.generators.HKDFBytesGenerator
import org.bouncycastle.crypto.macs.Poly1305
import org.bouncycastle.crypto.params.HKDFParameters
import org.bouncycastle.crypto.params.KeyParameter
import org.bouncycastle.util.Pack
import pl.merskip.homekitcollector.srp.SRP6Client
import pl.merskip.homekitcollector.srp.SRP6GroupParams
import pl.merskip.homekitcollector.tlv.TLVBuilder
import pl.merskip.homekitcollector.tlv.TLVReader
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.*


class HomeKitClient {

    private val logger = loggerFor(javaClass)
    private val httpClient = HttpClientBuilder.create().build()


    fun pair(clientName: String, pinCode: String) {

        logger.info("Paring...")

        // 4.7.1 M1: iOS Device -> Accessory -- `SRP Start Request'

        val payload1 = post("/pair-setup",
                mapOf("Content-Type" to "application/pairing+tlv8"),
                TLVBuilder()
                        .append(0, 0) // PairingMethod
                        .append(6, 1) // Sequence = StartRequest
                        .build()
        )

        // 4.7.3 M3: iOS Device -> Accessory -- `SRP Verify Request'
        val response1 = TLVReader(payload1)
        check(response1.getInt(6) == 2)

        val salt = response1.get(2)!!
        val serverPublicKey = response1.get(3)!!

        check(salt.size == 16)
        check(serverPublicKey.size == 384)

        val srp = SRP6Client(
                "Pair-Setup", pinCode,
                salt.toBigInteger(), serverPublicKey.toBigInteger(),
                SRP6GroupParams.N3072_HAP
        )

        val payload2 = post("/pair-setup",
                mapOf("Content-Type" to "application/pairing+tlv8"),
                TLVBuilder()
                        .append(0, 0) // PairingMethod
                        .append(6, 3) // Sequence = VerifyRequest
                        .append(3, srp.publicClientKey.rawByteArray) // PublicKey
                        .append(4, srp.clientProof.rawByteArray) // Proof
                        .build()
        )

        // 4.7.5 M5: iOS Device -> Accessory -- `Exchange Request'
        val response2 = TLVReader(payload2)
        check(response2.getInt(6) == 4)

        // 4.7.5.1 <M4> Verification
        val serverProof = response2.get(4)!!.toBigInteger()
        if (!srp.verifyServerProof(serverProof)) error("Failed verify server proof (M2)")

        // 4.7.5.2 <M5> Request Generation

        // Step 1
        val spec = EdDSANamedCurveTable.getByName(EdDSANamedCurveTable.ED_25519)
        val sgr = EdDSAEngine(MessageDigest.getInstance(spec.hashAlgorithm))

        val privKeySpec = EdDSAPrivateKeySpec(generateSeed(spec), spec)
        val iOSDeviceLTSK = EdDSAPrivateKey(privKeySpec)

        val pubKeySpec = EdDSAPublicKeySpec(iOSDeviceLTSK.abyte, spec)
        val iOSDeviceLTPK = EdDSAPublicKey(pubKeySpec)

        // Step 2
        val iOSDeviceX =  hkdfSHA512(
                srp.sessionKey.rawByteArray,
                "Pair-Setup-Controller-Sign-Salt".toByteArray(),
                "Pair-Setup-Controller-Sign-Info".toByteArray(),
                32
        )

        // Step 3
        val iOSDevicePairingID = UUID.randomUUID()
        val iOSDeviceInfo = listOf(iOSDeviceX, iOSDevicePairingID.toString().toByteArray(), iOSDeviceLTPK.abyte)
                .flatMap { it.asIterable() }
                .toByteArray()

        // Step 4
        sgr.initSign(iOSDeviceLTSK)
        val iOSDeviceSignature = sgr.signOneShot(iOSDeviceInfo)

        // Step 5
        val subTLV = TLVBuilder()
                .append(1, iOSDevicePairingID.toString().toByteArray()) // Username
                .append(3, iOSDeviceLTPK.abyte) // Public key
                .append(10, iOSDeviceSignature) // Signature
                .build()

        logger.info("SubTLV: ${subTLV.hexDescription}")

        // Step 6
        // session key should be 16 or 32 bytes

        val sharedKey = hkdfSHA512(
                srp.sessionKey.rawByteArray,
                "Pair-Setup-Encrypt-Salt".toByteArray(),
                "Pair-Setup-Encrypt-Info".toByteArray(),
                32
        )

        logger.info("sharedKey: " + sharedKey.hexDescription)


        val sodium = LazySodiumJava(SodiumJava())
        val cipherText  = ByteArray(subTLV.size)
        sodium.cryptoStreamChacha20XorIc(cipherText, subTLV, subTLV.size.toLong(), "PS-Msg05".toByteArray(), 1, sharedKey)
        logger.info("CipherText: ${cipherText.hexDescription}")

        val hmac = computePoly1305(sodium, cipherText, ByteArray(0), "PS-Msg05".toByteArray(), sharedKey)
        logger.info("hmac: ${hmac.hexDescription}")

        // Step 7
        val tlv = TLVBuilder()
                .append(6, 5) // Sequence = KeyExchangeRequest
                .append(5, listOf(cipherText, hmac).flatMap { it.asIterable() }.toByteArray())
                .build()

        logger.info("TLV: ${tlv.hexDescription}")

        val payload3 = post("/pair-setup",
                mapOf("Content-Type" to "application/pairing+tlv8"),
                tlv
        )

        val response3 = TLVReader(payload3)
        check(response3.getInt(6) == 6)

        val receivedEncryptedData = response3.get(5)!!
        logger.info("receivedEncryptedData: ${receivedEncryptedData.hexDescription}")

        val encryptedMessage = receivedEncryptedData.sliceArray(0 until receivedEncryptedData.size - 16)
        val authTag = receivedEncryptedData.sliceArray(receivedEncryptedData.size - 16 until receivedEncryptedData.size)
        logger.info("encryptedMessage     : ${encryptedMessage.hexDescription}")
        logger.info("authTag              : ${authTag.hexDescription}")

        val wtf = verifyAndDecrypt(sodium, encryptedMessage, authTag, ByteArray(0), "PS-Msg06".toByteArray(), sharedKey)
                ?: error("No wtf")

        val message = TLVReader(wtf)


        val accessoryName = message.get(1)!!.toString(Charsets.UTF_8)
        logger.info("AccessoryNAme: $accessoryName")

        val accessoryPublicKey = message.get(3)!!
        val accessorySignature = message.get(10)!!

        val hash = hkdfSHA512(
                srp.sessionKey.rawByteArray,
                "Pair-Setup-Accessory-Sign-Salt".toByteArray(),
                "Pair-Setup-Accessory-Sign-Info".toByteArray(),
                32
        )

        val material = listOf(hash, accessoryName.toByteArray(), accessoryPublicKey)
                .flatMap { it.asIterable() }
                .toByteArray()


        val accPubKeySpec = EdDSAPublicKeySpec(accessoryPublicKey, spec)
        val accLTPK = EdDSAPublicKey(accPubKeySpec )

        sgr.initVerify(accLTPK)
        if (!sgr.verifyOneShot(material, accessorySignature)) {
            error("Veryfi failed")
        }

        logger.info("We are now pared!!!!!")
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


    fun generateSeed(spec: EdDSANamedCurveSpec): ByteArray {
        val sr = SecureRandom()
        val seed = ByteArray(spec.curve.field.getb() / 8)
        sr.nextBytes(seed)
        return seed
    }

    fun computePoly1305(sodium: LazySodium, cipherText: ByteArray, additinalData: ByteArray, nonce: ByteArray, key: ByteArray): ByteArray {


        val msg = listOf(
                additinalData,
                padding16(additinalData.size),
                cipherText,
                padding16(cipherText.size),
                Pack.longToLittleEndian(additinalData.size.toLong()),
                Pack.longToLittleEndian(cipherText.size.toLong())
        ).flatMap { it.asIterable() }.toByteArray()


        val polyKey = ByteArray(32)
        sodium.cryptoStreamChaCha20(polyKey, polyKey.size.toLong(), nonce, key)

        logger.info("polyKey: ${polyKey.hexDescription}")

        val poly1305 = Poly1305()
        poly1305.init(KeyParameter(polyKey))
        poly1305.update(msg, 0, msg.size)

        val hmac = ByteArray(16)
        poly1305.doFinal(hmac, 0)
        return hmac
    }

    private fun padding16(size: Int): ByteArray =
            if (size % 16 == 0) ByteArray(0)
            else ByteArray(16 - (size % 16))

    private fun verifyAndDecrypt(
            sodium: LazySodium,
            encrypted: ByteArray,
            hmac: ByteArray,
            additionalData: ByteArray,
            nonce: ByteArray,
            key: ByteArray
    ): ByteArray? {

        val calculatedHmac = computePoly1305(sodium, encrypted, additionalData, nonce, key)
        if (!calculatedHmac.contentEquals(hmac)) {
            return null
        }

        val cipherText  = ByteArray(encrypted.size)
        val res = sodium.cryptoStreamChacha20XorIc(cipherText, encrypted, encrypted.size.toLong(), nonce, 1, key)
        if (!res) return null

        return cipherText
    }

    private fun post(endpoint: String, headers: Map<String, String> = emptyMap(), payload: ByteArray): ByteArray {

        val post = HttpPost("http://192.168.1.8:51826/$endpoint")
        headers.forEach { post.addHeader(it.key, it.value) }
        post.entity = ByteArrayEntity(payload)
        logger.debug("Post to ${post.uri.toASCIIString()}")

        val response = httpClient.execute(post)
        logger.debug("Status code: ${response.statusLine.statusCode} ${response.statusLine.reasonPhrase}")

        return IOUtils.toByteArray(response.entity.content)
    }

}