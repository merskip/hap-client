package pl.merskip.homekitcollector.pairing.impl

import pl.merskip.homekitcollector.crypto.DiffieHellman
import pl.merskip.homekitcollector.pairing.HomeKitTLVTag
import pl.merskip.homekitcollector.pairing.PairStepHandler
import pl.merskip.homekitcollector.pairing.PairVerify
import pl.merskip.homekitcollector.pairing.PairingClient
import pl.merskip.homekitcollector.tlv.TLVReader

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