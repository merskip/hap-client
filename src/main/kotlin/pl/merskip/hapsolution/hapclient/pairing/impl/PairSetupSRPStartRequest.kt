package pl.merskip.hapsolution.hapclient.pairing.impl

import pl.merskip.hapsolution.hapclient.pairing.HomeKitTLVTag
import pl.merskip.hapsolution.hapclient.pairing.PairSetup
import pl.merskip.hapsolution.hapclient.pairing.PairStepHandler
import pl.merskip.hapsolution.hapclient.pairing.PairingClient
import pl.merskip.hapsolution.hapclient.tlv.TLVReader

class PairSetupSRPStartRequest: PairStepHandler<Unit> {

    override fun process(input: TLVReader, client: PairingClient): Pair<TLVReader, Unit> {
        val response = client.request {
            it.append(HomeKitTLVTag.Method.tag, HomeKitTLVTag.Methods.PairSetup.value)
                    .append(HomeKitTLVTag.Sequence.tag, PairSetup.Step.SRPStartRequest.number)
        }

        return Pair(response, Unit)
    }
}