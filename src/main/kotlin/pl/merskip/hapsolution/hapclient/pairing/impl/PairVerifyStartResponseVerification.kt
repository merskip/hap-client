package pl.merskip.hapsolution.hapclient.pairing.impl

import com.goterl.lazycode.lazysodium.utils.Key
import pl.merskip.hapsolution.hapclient.crypto.Chacha20Poly1305
import pl.merskip.hapsolution.hapclient.crypto.DiffieHellman
import pl.merskip.hapsolution.hapclient.crypto.EdDSA
import pl.merskip.hapsolution.hapclient.crypto.HKDFSHA512
import pl.merskip.hapsolution.hapclient.pairing.*
import pl.merskip.hapsolution.hapclient.tlv.TLVReader

class PairVerifyStartResponseVerification(
    private val pairCredentials: PairCredentials,
    private val diffieHellman: DiffieHellman
) : PairStepHandler<PairVerifyStartResponseVerification.Result> {

    data class Result(
        val sharedKey: ByteArray,
        val encryptionKey: ByteArray
    )

    override fun process(input: TLVReader, client: PairingClient): Pair<TLVReader, PairVerifyStartResponseVerification.Result> {

        val accessoryPublicKey = input.getByteArray(HomeKitTLVTag.PublicKey.tag)
        val accessoryEncryptedData = input.getByteArray(HomeKitTLVTag.EncryptedData.tag)

        val sharedKey = diffieHellman.calculateSharedKey(Key.fromBytes(accessoryPublicKey))

        val encryptionKey = HKDFSHA512.encrypt(
                sharedKey.asBytes,
                "Pair-Verify-Encrypt-Salt",
                "Pair-Verify-Encrypt-Info"
        )

        val message = Chacha20Poly1305(encryptionKey).verifyAndDecrypt(accessoryEncryptedData, nonce = "PV-Msg02")
                ?: throw FailedExchangeVerificationException()
        val messageTLV = TLVReader(message)

        val accessoryName = String(messageTLV.getByteArray(HomeKitTLVTag.Identifier.tag))
        val accessorySignature = messageTLV.getByteArray(HomeKitTLVTag.Signature.tag)

        val accessoryInfo = accessoryPublicKey + accessoryName.toByteArray() + diffieHellman.publicKey.asBytes

        val engineEdDSA = EdDSA.createWithPublicKey(pairCredentials.accessoryLongTermPublicKeyBytes)
        if (!engineEdDSA.verify(accessoryInfo, accessorySignature))
            throw FailedExchangeVerificationException()

        return Pair(input, Result(sharedKey.asBytes, encryptionKey))
    }
}