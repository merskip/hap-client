package pl.merskip.hapsolution.hapclient.pairing

import pl.merskip.hapsolution.hapclient.tlv.TLVBuilder
import pl.merskip.hapsolution.hapclient.tlv.TLVData
import pl.merskip.hapsolution.hapclient.tlv.TLVReader

interface PairingClient {

    fun request(body: List<TLVData>): TLVReader

    fun request(build: (TLVBuilder) -> Unit): TLVReader {
        val builder = TLVBuilder()
        build(builder)
        return request(builder.toDataList())
    }
}
