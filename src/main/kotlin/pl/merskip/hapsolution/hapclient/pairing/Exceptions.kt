package pl.merskip.hapsolution.hapclient.pairing

class UnexpectedHTTPStatus(status: Int, text: String):
        Exception("Unexpected HTTP Status: $status $text")

class TLVResponseErrorException(err: HomeKitTLVTag.ErrorCodes):
        Exception("TLV Error: ${err.name} (${err.value})")

class FailedServerProofVerificationException: Exception()

class FailedExchangeVerificationException: Exception()