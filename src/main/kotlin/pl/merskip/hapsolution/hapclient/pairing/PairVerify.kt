package pl.merskip.hapsolution.hapclient.pairing

import pl.merskip.hapsolution.hapclient.pairing.impl.HttpPairingTLVv8Client
import pl.merskip.hapsolution.hapclient.pairing.impl.PairVerifyFinishRequest
import pl.merskip.hapsolution.hapclient.pairing.impl.PairVerifyStartRequest
import pl.merskip.hapsolution.hapclient.pairing.impl.PairVerifyStartResponseVerification
import pl.merskip.hapsolution.hapclient.tlv.TLVReader
import java.net.URL

class PairVerify(private val pairCredentials: PairCredentials){

    enum class Step(override val number: Int): Sequence {
        VerifyStartRequest(1),
        VerifyStartResponse(2),
        VerifyFinishRequest(3),
        VerifyFinishResponse(4)
    }

    private val pairingClient: PairingClient = HttpPairingTLVv8Client(
            URL("http", pairCredentials.host, pairCredentials.port, "/pair-verify")
    )

    fun verify(): SessionKeys {

        val (startResponse, keyPair) = PairVerifyStartRequest()
                .process(TLVReader(emptyList()), pairingClient)
        checkError(startResponse)

        val (verificationResponse, result) = PairVerifyStartResponseVerification(pairCredentials, keyPair)
                .process(startResponse, pairingClient)
        checkError(verificationResponse)

        val (finishResponse, _) = PairVerifyFinishRequest(pairCredentials, keyPair, result.encryptionKey)
                .process(verificationResponse, pairingClient)
        checkError(finishResponse)

        return SessionKeys.createFromSharedKey(result.sharedKey)
    }

    // TODO: Remove duplication
    private fun checkError(tlv: TLVReader) {
        val error = tlv.getIntOrNull(HomeKitTLVTag.Error.tag)?.toInt()
        if (error != null) {
            val errorCode = HomeKitTLVTag.ErrorCodes.values().find { it.value == error }
                    ?: HomeKitTLVTag.ErrorCodes.Unknown
            throw TLVResponseErrorException(errorCode)
        }
    }
}