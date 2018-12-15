package pl.merskip.hapsolution.hapclient.pairing

import pl.merskip.hapsolution.hapclient.tlv.TLVReader

interface PairStepHandler<ResponseType> {

    fun process(input: TLVReader, client: PairingClient): Pair<TLVReader, ResponseType>
}