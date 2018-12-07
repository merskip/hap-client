package pl.merskip.homekitcollector.pairing.impl

import pl.merskip.homekitcollector.pairing.HomeKitTLVTag
import pl.merskip.homekitcollector.pairing.PairSetup
import pl.merskip.homekitcollector.pairing.PairStepHandler
import pl.merskip.homekitcollector.pairing.PairingClient
import pl.merskip.homekitcollector.tlv.TLVReader

class PairSetupSRPStartRequest: PairStepHandler<Unit> {

    override fun process(input: TLVReader, client: PairingClient): Pair<TLVReader, Unit> {
        val response = client.request {
            it.append(HomeKitTLVTag.Method.tag, HomeKitTLVTag.Methods.PairSetup.value)
                    .append(HomeKitTLVTag.Sequence.tag, PairSetup.Step.SRPStartRequest.number)
        }

        return Pair(response, Unit)
    }
}