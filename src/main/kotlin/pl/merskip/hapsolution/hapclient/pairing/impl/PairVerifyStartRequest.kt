package pl.merskip.hapsolution.hapclient.pairing.impl

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