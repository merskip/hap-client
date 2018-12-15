package pl.merskip.hapsolution.hapclient.pairing

interface PairingClient {

    fun request(body: List<TLVData>): TLVReader

    fun request(build: (TLVBuilder) -> Unit): TLVReader {
        val builder = TLVBuilder()
        build(builder)
        return request(builder.toDataList())
    }
}
