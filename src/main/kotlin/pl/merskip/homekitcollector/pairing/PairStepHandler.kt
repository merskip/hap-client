package pl.merskip.homekitcollector.pairing

import pl.merskip.homekitcollector.tlv.TLVReader

interface PairStepHandler<ResponseType> {

    fun process(input: TLVReader, client: PairingClient): Pair<TLVReader, ResponseType>
}