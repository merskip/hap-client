package pl.merskip.homekitcollector.pairing.impl

import net.i2p.crypto.eddsa.EdDSAPublicKey
import pl.merskip.homekitcollector.crypto.Chacha20Poly1305
import pl.merskip.homekitcollector.crypto.EdDSA
import pl.merskip.homekitcollector.crypto.HKDFSHA512
import pl.merskip.homekitcollector.pairing.FailedExchangeVerificationException
import pl.merskip.homekitcollector.pairing.HomeKitTLVTag
import pl.merskip.homekitcollector.pairing.PairStepHandler
import pl.merskip.homekitcollector.pairing.PairingClient
import pl.merskip.homekitcollector.tlv.TLVReader

class PairSetupExchangeVerification(
        private val exchangeResult: PairSetupExchangeRequest.Result
) : PairStepHandler<PairSetupExchangeVerification.Result> {

    data class Result(
            val accessoryName: String,
            val accessoryLongTermPublicKey: EdDSAPublicKey
    )

    override fun process(input: TLVReader, client: PairingClient): Pair<TLVReader, PairSetupExchangeVerification.Result> {

        val encryptedData = input.getByteArray(HomeKitTLVTag.EncryptedData.tag)
        val message = Chacha20Poly1305(exchangeResult.sharedKey).verifyAndDecrypt(encryptedData, nonce = "PS-Msg06")
                ?: throw FailedExchangeVerificationException()

        val receivedTLC = TLVReader(message)
        val accessoryName = String(receivedTLC.getByteArray(HomeKitTLVTag.Identifier.tag))
        val accessoryLongTermPublicKey = receivedTLC.getByteArray(HomeKitTLVTag.PublicKey.tag)
        val accessorySignature = receivedTLC.getByteArray(HomeKitTLVTag.Signature.tag)

        val accessoryX = HKDFSHA512.encrypt(
                exchangeResult.srp.sessionKey,
                "Pair-Setup-Accessory-Sign-Salt",
                "Pair-Setup-Accessory-Sign-Info"
        )

        val accessoryInfo = accessoryX + accessoryName.toByteArray() + accessoryLongTermPublicKey

        val engineEdDSA = EdDSA.createWithPublicKey(accessoryLongTermPublicKey)
        if (!engineEdDSA.verify(accessoryInfo, accessorySignature))
            throw FailedExchangeVerificationException()

        return Pair(TLVReader(emptyList()), Result(accessoryName, engineEdDSA.publicKey))
    }
}