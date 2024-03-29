package pl.merskip.hapsolution.hapclient.srp

import pl.merskip.hapsolution.hapclient.hexDescription
import pl.merskip.hapsolution.hapclient.loggerFor
import pl.merskip.hapsolution.hapclient.rawByteArray
import pl.merskip.hapsolution.hapclient.toBigInteger
import java.math.BigInteger
import java.util.*

/**
 * @param username I
 * @param password P
 * @param salt s
 * @param serverPublicKey B
 * @param clientPrivateKey a
 */
class SRP6Client(
        private val username: String,
        private val password: String,
        private val salt: BigInteger,
        private val serverPublicKey: BigInteger,
        private val params: SRP6GroupParams,
        private val clientPrivateKey: BigInteger = randomClientPrivateKey(32)
) {

    private val logger = loggerFor(javaClass)

    /** x */
    val hashedUserAndPassword: BigInteger

    /** v */
    val verifier: BigInteger

    /** u */
    val randomScramblingParameter: BigInteger

    /** S */
    val premasterSecret: BigInteger

    /** A */
    val publicClientKey: BigInteger

    /** M1 */
    val clientProof: BigInteger

    /** K */
    val sessionKey: BigInteger

    init {
        logger.trace("privateClientKey (a) = ${clientPrivateKey.rawByteArray.hexDescription}")

        hashedUserAndPassword = hash(salt, hash("$username:$password"))
        logger.trace("hashedUserAndPassword (x) = ${hashedUserAndPassword.rawByteArray.hexDescription}")

        publicClientKey = params.generator.modPow(clientPrivateKey, params.modulus)
        logger.trace("publicClientKey (A) = ${publicClientKey.rawByteArray.hexDescription}")

        randomScramblingParameter = hash(padToN(publicClientKey), padToN(serverPublicKey))
        logger.trace("randomScramblingParameter (u) = ${randomScramblingParameter.rawByteArray.hexDescription}")

        verifier = params.generator.modPow(hashedUserAndPassword, params.modulus)
        logger.trace("verifier (v) = ${verifier.rawByteArray.hexDescription}")

        val hashedModulusAndGenerator = hash(params.modulus.rawByteArray, padToN(params.generator))
        logger.trace("hashedModulusAndGenerator (k) = ${hashedModulusAndGenerator.rawByteArray.hexDescription}")

        val premasterSecretBase = serverPublicKey.minus(hashedModulusAndGenerator.multiply(verifier))
        val premasterSecretExponent = clientPrivateKey.plus(randomScramblingParameter.multiply(hashedUserAndPassword))
        premasterSecret = premasterSecretBase.modPow(premasterSecretExponent, params.modulus)
        logger.trace("premasterSecret (S) = ${premasterSecret.rawByteArray.hexDescription}")

        sessionKey = hash(premasterSecret)
        logger.trace("sessionKey (K) = ${sessionKey.rawByteArray.hexDescription}")

        val paramsHash = hash(params.modulus).xor(hash(params.generator))
        val usernameHash = hash(username)
        clientProof = hash(paramsHash, usernameHash, salt, publicClientKey, serverPublicKey, sessionKey)
        logger.trace("clientProof (M1) = ${clientProof.rawByteArray.hexDescription}")

    }

    companion object {
        fun randomClientPrivateKey(bytes: Int): BigInteger {
            val byteArray = ByteArray(bytes)
            Random().nextBytes(byteArray)
            return byteArray.toBigInteger()
        }
    }

    fun verifyServerProof(serverProof: BigInteger): Boolean {
        logger.trace("Verifying server proof (M2) = ${serverProof.rawByteArray.hexDescription}")

        return serverProof == hash(publicClientKey, clientProof, sessionKey)
    }

    private fun hash(vararg strings: String): BigInteger =
            hash(strings.joinToString().toByteArray(Charsets.US_ASCII))

    private fun hash(vararg chunks: BigInteger): BigInteger =
            hash(*chunks.map { it.rawByteArray }.toTypedArray())

    private fun hash(vararg chunks: ByteArray): BigInteger {
        val result = params.hasher.digest(chunks.flatMap { it.asIterable() }.toByteArray())
        return result.toBigInteger()
    }

    private fun padToN(number: BigInteger) = pad(number, params.modulus.bitLength() / 8)

    private fun pad(number: BigInteger, size: Int): ByteArray {
        val numberBytes = number.rawByteArray
        val paddingLength = size - numberBytes.size
        check(paddingLength >= 0) { "Negative padding!"}

        val result = ByteArray(size)
        result.fill(0, 0, paddingLength)
        System.arraycopy(numberBytes, 0, result, paddingLength, numberBytes.size)

        return result
    }
}