package pl.merskip.homekitcollector.pairing

import pl.merskip.homekitcollector.tlv.TLVBuilder
import pl.merskip.homekitcollector.tlv.TLVData
import pl.merskip.homekitcollector.tlv.TLVReader

interface PairingClient {

    fun request(body: List<TLVData>): TLVReader

    fun request(build: (TLVBuilder) -> Unit): TLVReader {
        val builder = TLVBuilder()
        build(builder)
        return request(builder.toDataList())
    }
}
