package pl.merskip.homekitcollector.pairing.impl

import com.goterl.lazycode.lazysodium.utils.Key
import pl.merskip.homekitcollector.crypto.Chacha20Poly1305
import pl.merskip.homekitcollector.crypto.DiffieHellman
import pl.merskip.homekitcollector.crypto.EdDSA
import pl.merskip.homekitcollector.crypto.HKDFSHA512
import pl.merskip.homekitcollector.pairing.*
import pl.merskip.homekitcollector.tlv.TLVReader

class PairVerifyStartResponseVerification(
        private val pairCredentials: PairCredentials,
        private val diffieHellman: DiffieHellman
) : PairStepHandler<ByteArray> {

    override fun process(input: TLVReader, client: PairingClient): Pair<TLVReader, ByteArray> {

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

        return Pair(input, encryptionKey)
    }
}