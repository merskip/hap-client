package pl.merskip.hapsolution.hapclient.pairing

interface PairStepHandler<ResponseType> {

    fun process(input: TLVReader, client: PairingClient): Pair<TLVReader, ResponseType>
}