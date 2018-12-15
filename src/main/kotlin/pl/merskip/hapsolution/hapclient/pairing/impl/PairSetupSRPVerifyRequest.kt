package pl.merskip.hapsolution.hapclient.pairing.impl

import pl.merskip.hapsolution.hapclient.pairing.HomeKitTLVTag
import pl.merskip.hapsolution.hapclient.pairing.PairSetup
import pl.merskip.hapsolution.hapclient.pairing.PairStepHandler
import pl.merskip.hapsolution.hapclient.pairing.PairingClient
import pl.merskip.hapsolution.hapclient.rawByteArray
import pl.merskip.hapsolution.hapclient.srp.SRP6Client
import pl.merskip.hapsolution.hapclient.srp.SRP6GroupParams
import pl.merskip.hapsolution.hapclient.tlv.TLVReader
import pl.merskip.hapsolution.hapclient.toBigInteger

class PairSetupSRPVerifyRequest(private val pinCode: String): PairStepHandler<SRP6Client> {

    override fun process(input: TLVReader, client: PairingClient): Pair<TLVReader, SRP6Client> {

        val salt = input.getByteArray(HomeKitTLVTag.Salt.tag)
        val serverPublicKey = input.getByteArray(HomeKitTLVTag.PublicKey.tag)

        check(salt.size == 16)
        check(serverPublicKey.size == 384)

        val srp = SRP6Client(
                "Pair-Setup", pinCode,
                salt.toBigInteger(), serverPublicKey.toBigInteger(),
                SRP6GroupParams.N3072_HAP
        )

        val response = client.request {
            it.append(HomeKitTLVTag.Sequence.tag, PairSetup.Step.SRPVerifyRequest.number)
                    .append(HomeKitTLVTag.PublicKey.tag, srp.publicClientKey.rawByteArray)
                    .append(HomeKitTLVTag.Proof.tag, srp.clientProof.rawByteArray)
        }

        return Pair(response, srp)
    }
}