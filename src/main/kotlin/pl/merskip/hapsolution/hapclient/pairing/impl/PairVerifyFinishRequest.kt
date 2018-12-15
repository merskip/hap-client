package pl.merskip.hapsolution.hapclient.pairing.impl

import pl.merskip.hapsolution.hapclient.crypto.Chacha20Poly1305
import pl.merskip.hapsolution.hapclient.crypto.DiffieHellman
import pl.merskip.hapsolution.hapclient.crypto.EdDSA
import pl.merskip.hapsolution.hapclient.pairing.*
import pl.merskip.hapsolution.hapclient.tlv.TLVBuilder
import pl.merskip.hapsolution.hapclient.tlv.TLVReader

class PairVerifyFinishRequest(
    private val pairCredentials: PairCredentials,
    private val diffieHellman: DiffieHellman,
    private val encryptionKey: ByteArray
) : PairStepHandler<Unit> {


    override fun process(input: TLVReader, client: PairingClient): Pair<TLVReader, Unit> {

        val accessoryPublicKey = input.getByteArray(HomeKitTLVTag.PublicKey.tag)

        val deviceInfo = diffieHellman.publicKey.asBytes + pairCredentials.pairingID.toString().toByteArray() + accessoryPublicKey

        val engineEdDSA = EdDSA.createWithKeys(pairCredentials.longTermSecretKeyBytes, pairCredentials.longTermPublicKeyBytes)
        val signature = engineEdDSA.sign(deviceInfo)

        val subTLV = TLVBuilder()
                .append(HomeKitTLVTag.Identifier.tag, pairCredentials.pairingID.toString().toByteArray())
                .append(HomeKitTLVTag.Signature.tag, signature)
                .build()

        val encryptedSubTLV = Chacha20Poly1305(encryptionKey).encrypt(subTLV, nonce = "PV-Msg03")

        val response = client.request {
            it.append(HomeKitTLVTag.Sequence.tag, PairVerify.Step.VerifyFinishRequest.number)
                    .append(HomeKitTLVTag.EncryptedData.tag, encryptedSubTLV)
        }

        return Pair(response, Unit)
    }
}