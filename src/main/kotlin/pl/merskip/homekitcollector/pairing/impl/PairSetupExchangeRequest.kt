package pl.merskip.homekitcollector.pairing.impl

import net.i2p.crypto.eddsa.EdDSAPrivateKey
import net.i2p.crypto.eddsa.EdDSAPublicKey
import pl.merskip.homekitcollector.crypto.Chacha20Poly1305
import pl.merskip.homekitcollector.crypto.EdDSA
import pl.merskip.homekitcollector.crypto.HKDFSHA512
import pl.merskip.homekitcollector.pairing.*
import pl.merskip.homekitcollector.srp.SRP6Client
import pl.merskip.homekitcollector.tlv.TLVBuilder
import pl.merskip.homekitcollector.tlv.TLVReader
import java.util.*

class PairSetupExchangeRequest(private val srp: SRP6Client): PairStepHandler<PairSetupExchangeRequest.Result> {

    data class Result(
            val srp: SRP6Client,
            val pairingID: UUID,
            val sharedKey: ByteArray,
            val longTermSecretKey: EdDSAPrivateKey,
            val longTermPublicKey: EdDSAPublicKey
    )

    override fun process(input: TLVReader, client: PairingClient): Pair<TLVReader, PairSetupExchangeRequest.Result> {
        if (!verifyServerProof(input))
            throw FailedServerProofVerificationException()

        val engineEdDSA = EdDSA.createWithRandomKeyPair()

        val iOSDeviceX = HKDFSHA512.encrypt(
                srp.sessionKey,
                "Pair-Setup-Controller-Sign-Salt",
                "Pair-Setup-Controller-Sign-Info"
        )

        val pairingID = UUID.randomUUID()

        val iOSDeviceInfo = iOSDeviceX + pairingID.toString().toByteArray() + engineEdDSA.publicKey.abyte

        val subTLV = TLVBuilder()
                .append(HomeKitTLVTag.Identifier.tag, pairingID.toString().toByteArray())
                .append(HomeKitTLVTag.PublicKey.tag, engineEdDSA.publicKey.abyte)
                .append(HomeKitTLVTag.Signature.tag, engineEdDSA.sign(iOSDeviceInfo))
                .build()

        val sharedKey = HKDFSHA512.encrypt(
                srp.sessionKey,
                "Pair-Setup-Encrypt-Salt",
                "Pair-Setup-Encrypt-Info"
        )

        val encryptedSubTLV = Chacha20Poly1305(sharedKey).encrypt(subTLV, nonce = "PS-Msg05")

        val response = client.request {
            it.append(HomeKitTLVTag.Sequence.tag, PairSetup.Step.ExchangeRequest.number)
                    .append(HomeKitTLVTag.EncryptedData.tag, encryptedSubTLV)

        }
        return Pair(response, Result(srp, pairingID, sharedKey, engineEdDSA.secretKey!!, engineEdDSA.publicKey))
    }

    private fun verifyServerProof(input: TLVReader): Boolean {
        val serverProof = input.getInt(HomeKitTLVTag.Proof.tag)
        return srp.verifyServerProof(serverProof)
    }
}