package pl.merskip.hapsolution.hapclient.pairing.impl

import org.apache.commons.io.IOUtils
import org.apache.http.HttpStatus
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ByteArrayEntity
import java.net.URL
import java.util.concurrent.TimeUnit

class HttpPairingTLVv8Client(
        private val url: URL
): PairingClient {

    private val logger = loggerFor(javaClass)

    override fun request(body: List<TLVData>): TLVReader {

        val httpPost = HttpPost(url.toURI())

        httpPost.addHeader("Content-Type", "application/pairing+tlv8")
        httpPost.entity = ByteArrayEntity(TLVEncoder().encode(body))

        logger.debug("Request HTTP POST to ${httpPost.uri}...")
        val httpResponse = HTTP.client.execute(httpPost, null).get(60, TimeUnit.SECONDS)

        val status = httpResponse.statusLine.statusCode
        logger.debug("Response status code: $status")
        if (status != HttpStatus.SC_OK)
            throw UnexpectedHTTPStatus(status, httpResponse.statusLine.reasonPhrase)

        val responseContent = IOUtils.toByteArray(httpResponse.entity.content)
        return TLVReader(responseContent)
    }
}