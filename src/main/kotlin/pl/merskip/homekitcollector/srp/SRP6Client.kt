package pl.merskip.homekitcollector.srp

import pl.merskip.homekitcollector.hexDescription
import pl.merskip.homekitcollector.loggerFor
import pl.merskip.homekitcollector.rawByteArray
import pl.merskip.homekitcollector.toBigInteger
import java.math.BigInteger
import java.security.MessageDigest
import java.util.*


@Suppress("PropertyName", "LocalVariableName", "FunctionName")
/**
 * @param I identity
 * @param P password
 * @param s salt
 * @param B publicServerKey
 */
class SRP6Client(
        val I: String,
        val P: String,
        val s: BigInteger,
        val B: BigInteger,
        a: BigInteger
) {

    private val logger = loggerFor(javaClass)

    val N: BigInteger = BigInteger("""
        FFFFFFFF FFFFFFFF C90FDAA2 2168C234 C4C6628B 80DC1CD1 29024E08 8A67CC74
        020BBEA6 3B139B22 514A0879 8E3404DD EF9519B3 CD3A431B 302B0A6D F25F1437
        4FE1356D 6D51C245 E485B576 625E7EC6 F44C42E9 A637ED6B 0BFF5CB6 F406B7ED
        EE386BFB 5A899FA5 AE9F2411 7C4B1FE6 49286651 ECE45B3D C2007CB8 A163BF05
        98DA4836 1C55D39A 69163FA8 FD24CF5F 83655D23 DCA3AD96 1C62F356 208552BB
        9ED52907 7096966D 670C354E 4ABC9804 F1746C08 CA18217C 32905E46 2E36CE3B
        E39E772C 180E8603 9B2783A2 EC07A28F B5C55DF0 6F4C52C9 DE2BCBF6 95581718
        3995497C EA956AE5 15D22618 98FA0510 15728E5A 8AAAC42D AD33170D 04507A33
        A85521AB DF1CBA64 ECFB8504 58DBEF0A 8AEA7157 5D060C7D B3970F85 A6E1E4C7
        ABF5AE8C DB0933D7 1E8C94E0 4A25619D CEE3D226 1AD2EE6B F12FFA06 D98A0864
        D8760273 3EC86A64 521F2B18 177B200C BBE11757 7A615D6C 770988C0 BAD946E2
        08E24FA0 74E5AB31 43DB5BFC E0FD108E 4B82D120 A93AD2CA FFFFFFFF FFFFFFFF
            """.filter { it.isLetterOrDigit() },
            16
    )
    val g: BigInteger = BigInteger.valueOf(5)
    val hasher = MessageDigest.getInstance("SHA-512")

    val x: BigInteger
    val v: BigInteger
    val u: BigInteger
    val premasterSecret: BigInteger

    val A: BigInteger
    val M1: BigInteger
    val K: BigInteger

    init {
        check(N.bitLength() == 3072)

        x = hash(s, hash("$I:$P"))
        logger.debug("x= ${x.rawByteArray.hexDescription}")

        A = g.modPow(a, N)
        logger.debug("A = ${A.rawByteArray.hexDescription}")

        u = hash(padToN(A), padToN(B))
        logger.debug("u = ${u.rawByteArray.hexDescription}")

        v = g.modPow(x, N)
        logger.debug("v = ${v.rawByteArray.hexDescription}")

        val k = hash(N.rawByteArray, padToN(g))
        logger.debug("k = ${k.rawByteArray.hexDescription}")

        val S_base = B.minus(k.multiply(v))
        val S_exp = a.plus(u.multiply(x))
        premasterSecret = S_base.modPow(S_exp, N)
        logger.debug("S = ${premasterSecret.rawByteArray.hexDescription}")

        M1 = hash(A, B, premasterSecret)
        logger.debug("M1 = ${M1.rawByteArray.hexDescription}")

        K = hash(premasterSecret)
        logger.debug("K = ${K.rawByteArray.hexDescription}")
    }

    companion object {
        fun random_a(bytes: Int): BigInteger {
            val byteArray = ByteArray(bytes)
            Random().nextBytes(byteArray)
            return byteArray.toBigInteger()
        }
    }

    private fun hash(vararg strings: String): BigInteger =
            hash(strings.joinToString().toByteArray(Charsets.US_ASCII))

    private fun hash(vararg chunks: BigInteger): BigInteger =
            hash(*chunks.map { it.rawByteArray }.toTypedArray())

    private fun hash(vararg chunks: ByteArray): BigInteger {
        val result = hasher.digest(chunks.flatMap { it.asIterable() }.toByteArray())
        return result.toBigInteger()
    }

    private fun padToN(number: BigInteger) = pad(number, N.bitLength() / 8)

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