package pl.merskip.homekitcollector.pairing

import pl.merskip.homekitcollector.pairing.impl.*
import pl.merskip.homekitcollector.tlv.TLVReader
import java.net.URL

class PairSetup(val host: String, val port: Int) {

    enum class Step(override val number: Int) : Sequence {
        SRPStartRequest(1),
        SRPStartResponse(2),
        SRPVerifyRequest(3),
        SRPVerifyResponse(4),
        ExchangeRequest(5),
        ExchangeResponse(6)
    }

    private val pairingClient: PairingClient = HttpPairingTLVv8Client(
            URL("http", host, port, "/pair-setup")
    )

    fun pair(pinCode: String): PairCredentials {

        val (startRequestResponse, _) = PairSetupSRPStartRequest()
                .process(TLVReader(emptyList()), pairingClient)
        checkError(startRequestResponse)

        val (SRPVerifyRequestResponse, srp) = PairSetupSRPVerifyRequest(pinCode)
                .process(startRequestResponse, pairingClient)
        checkError(SRPVerifyRequestResponse)

        val (exchangeRequestResponse, exchangeResult) = PairSetupExchangeRequest(srp)
                .process(SRPVerifyRequestResponse, pairingClient)
        checkError(exchangeRequestResponse)

        val (_, verificationResult) = PairSetupExchangeVerification(exchangeResult)
                .process(exchangeRequestResponse, pairingClient)

        return PairCredentials(
                host, port,
                exchangeResult.pairingID,
                exchangeResult.longTermSecretKey.h,
                exchangeResult.longTermPublicKey.abyte,
                verificationResult.accessoryLongTermPublicKey.abyte
        )
    }

    private fun checkError(tlv: TLVReader) {
        val error = tlv.getIntOrNull(HomeKitTLVTag.Error.tag)?.toInt()
        if (error != null) {
            val errorCode = HomeKitTLVTag.ErrorCodes.values().find { it.value == error }
                    ?: HomeKitTLVTag.ErrorCodes.Unknown
            throw TLVResponseErrorException(errorCode)
        }
    }
}