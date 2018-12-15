package pl.merskip.hapsolution.hapclient.pairing.impl

import pl.merskip.hapsolution.hapclient.crypto.DiffieHellman
import pl.merskip.hapsolution.hapclient.pairing.HomeKitTLVTag
import pl.merskip.hapsolution.hapclient.pairing.PairStepHandler
import pl.merskip.hapsolution.hapclient.pairing.PairVerify
import pl.merskip.hapsolution.hapclient.pairing.PairingClient
import pl.merskip.hapsolution.hapclient.tlv.TLVReader

class PairVerifyStartRequest: PairStepHandler<DiffieHellman> {

    override fun process(input: TLVReader, client: PairingClient): Pair<TLVReader, DiffieHellman> {

        val diffieHellman = DiffieHellman.generateWithRandomCurve25519Key()

        val response = client.request {
            it.append(HomeKitTLVTag.Sequence.tag, PairVerify.Step.VerifyStartRequest.number)
                    .append(HomeKitTLVTag.PublicKey.tag, diffieHellman.publicKey.asBytes)
        }

        return Pair(response, diffieHellman)
    }
}