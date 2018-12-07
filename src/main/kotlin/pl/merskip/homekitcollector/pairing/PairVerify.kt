package pl.merskip.homekitcollector.pairing

import pl.merskip.homekitcollector.pairing.impl.HttpPairingTLVv8Client
import pl.merskip.homekitcollector.pairing.impl.PairVerifyFinishRequest
import pl.merskip.homekitcollector.pairing.impl.PairVerifyStartRequest
import pl.merskip.homekitcollector.pairing.impl.PairVerifyStartResponseVerification
import pl.merskip.homekitcollector.tlv.TLVReader
import java.net.URL

class PairVerify(val pairCredentials: PairCredentials){

    enum class Step(override val number: Int): Sequence {
        VerifyStartRequest(1),
        VerifyStartResponse(2),
        VerifyFinishRequest(3),
        VerifyFinishResponse(4)
    }

    private val pairingClient: PairingClient = HttpPairingTLVv8Client(
            URL("http", pairCredentials.host, pairCredentials.post, "/pair-verify")
    )

    fun verify() {

        val (startResponse, keyPair) = PairVerifyStartRequest()
                .process(TLVReader(emptyList()), pairingClient)
        checkError(startResponse)

        val (verificationResponse, encryptionKey) = PairVerifyStartResponseVerification(pairCredentials, keyPair)
                .process(startResponse, pairingClient)
        checkError(verificationResponse)

        val (finishResponse, _) = PairVerifyFinishRequest(pairCredentials, keyPair, encryptionKey)
                .process(verificationResponse, pairingClient)
        checkError(finishResponse)
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