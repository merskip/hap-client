package pl.merskip.homekitcollector

import org.apache.commons.io.IOUtils
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ByteArrayEntity
import org.apache.http.impl.client.HttpClientBuilder
import pl.merskip.homekitcollector.srp.SRP6Client
import pl.merskip.homekitcollector.srp.SRP6GroupParams
import pl.merskip.homekitcollector.tlv.TLVBuilder
import pl.merskip.homekitcollector.tlv.TLVReader

class HomeKitClient {

    private val logger = loggerFor(javaClass)
    private val httpClient = HttpClientBuilder.create().build()


    fun pair(pinCode: String) {

        logger.info("Paring...")

        val payload1 = post("/pair-setup",
                mapOf("Content-Type" to "application/pairing+tlv8"),
                TLVBuilder()
                        .append(0, 0) // PairingMethod
                        .append(6, 1) // Sequence = StartRequest
                        .build()
        )
        val response1 = TLVReader(payload1)
        check(response1.getInt(6) == 2)

        val salt = response1.get(2)!!
        val serverPublicKey = response1.get(3)!!

        logger.debug("salt= ${salt.hexDescription}")
        logger.debug("publicKey= ${serverPublicKey.hexDescription}")

        check(salt.size == 16)
        check(serverPublicKey.size == 384)

        val srp = SRP6Client(
                "Pair-Setup", pinCode,
                salt.toBigInteger(), serverPublicKey.toBigInteger(),
                SRP6GroupParams.N3072_HAP
        )

        logger.debug("publicClientKey= ${srp.publicClientKey.rawByteArray.hexDescription}")
        logger.debug("proof= ${srp.proof.rawByteArray.hexDescription}")

        val payload2 = post("/pair-setup",
                mapOf("Content-Type" to "application/pairing+tlv8"),
                TLVBuilder()
                        .append(0, 0) // PairingMethod
                        .append(6, 3) // Sequence = VerifyRequest
                        .append(3, srp.publicClientKey.rawByteArray) // PublicKey
                        .append(4, srp.proof.rawByteArray) // Proof
                        .build()
        )

        val response2 = TLVReader(payload2)
        check(response2.getInt(6) == 4)

        logger.debug(response2.toString())
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