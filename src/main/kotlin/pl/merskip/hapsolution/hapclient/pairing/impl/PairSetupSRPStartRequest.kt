package pl.merskip.hapsolution.hapclient.pairing.impl

class PairSetupSRPStartRequest: PairStepHandler<Unit> {

    override fun process(input: TLVReader, client: PairingClient): Pair<TLVReader, Unit> {
        val response = client.request {
            it.append(HomeKitTLVTag.Method.tag, HomeKitTLVTag.Methods.PairSetup.value)
                    .append(HomeKitTLVTag.Sequence.tag, PairSetup.Step.SRPStartRequest.number)
        }

        return Pair(response, Unit)
    }
}